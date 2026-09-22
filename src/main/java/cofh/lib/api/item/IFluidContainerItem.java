package cofh.lib.api.item;

import cofh.lib.util.helpers.MathHelper;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import static cofh.lib.api.ContainerType.FLUID;
import static cofh.lib.util.constants.NBTTags.TAG_AMOUNT;
import static cofh.lib.util.constants.NBTTags.TAG_FLUID;

/**
 * Implement this interface on Item classes that support external manipulation of their internal fluid storage.
 * <p>
 * NOTE: Use of NBT data on the containing ItemStack is encouraged.
 *
 * @author King Lemming
 */
public interface IFluidContainerItem extends IContainerItem {

    /**
     * The NBT the tank contents live in, as a copy - 1.20.5+ item data is immutable
     * {@link net.minecraft.world.item.component.CustomData}, so writes go through
     * {@link #mutateTankTag}. Implementations keeping fluid elsewhere override both.
     */
    default CompoundTag getTankTag(ItemStack container) {

        return ItemHelper.getCustomData(container);
    }

    default void mutateTankTag(ItemStack container, Consumer<CompoundTag> mutator) {

        ItemHelper.mutateCustomData(container, mutator);
    }

    /**
     * FluidStack persistence needs a registry lookup since 1.20.5 (components can reference
     * registries); this API is ItemStack-only, so the running world's registries are used.
     */
    default FluidStack loadFluid(CompoundTag tankTag) {

        return FluidStack.parseOptional(ProxyUtils.registryAccess(), tankTag.getCompound(TAG_FLUID));
    }

    default CompoundTag saveFluid(FluidStack stack) {

        return (CompoundTag) stack.save(ProxyUtils.registryAccess(), new CompoundTag());
    }

    default int getSpace(ItemStack container) {

        return getCapacity(container) - getFluidAmount(container);
    }

    default int getScaledFluidStored(ItemStack container, int scale) {

        return MathHelper.round((double) getFluidAmount(container) * scale / getCapacity(container));
    }

    default int getFluidAmount(ItemStack container) {

        return getFluid(container).getAmount();
    }

    /**
     * @param container ItemStack which is the fluid container.
     * @return FluidStack representing the fluid in the container, EMPTY if the container is empty.
     */
    default FluidStack getFluid(ItemStack container) {

        return loadFluid(getTankTag(container));
    }

    /**
     * @param container ItemStack which is the fluid container.
     * @param resource  FluidStack being queried.
     * @return TRUE if the fluid is valid in this particular container.
     */
    default boolean isFluidValid(ItemStack container, FluidStack resource) {

        return true;
    }

    /**
     * @param container ItemStack which is the fluid container.
     * @return Capacity of this fluid container.
     */
    int getCapacity(ItemStack container);

    /**
     * @param container ItemStack which is the fluid container.
     * @param resource  FluidStack attempting to fill the container.
     * @param action    If SIMULATE, the fill will only be simulated.
     * @return Amount of fluid that was (or would have been, if simulated) filled into the container.
     */
    default int fill(ItemStack container, FluidStack resource, FluidAction action) {

        if (resource.isEmpty() || !isFluidValid(container, resource)) {
            return 0;
        }
        int capacity = getCapacity(container);
        FluidStack stored = getFluid(container);

        if (isCreative(container, FLUID)) {
            if (action.execute()) {
                FluidStack full = resource.copyWithAmount(capacity);
                mutateTankTag(container, tag -> tag.put(TAG_FLUID, saveFluid(full)));
            }
            return resource.getAmount();
        }
        if (stored.isEmpty()) {
            int filled = Math.min(capacity, resource.getAmount());
            if (action.execute()) {
                FluidStack toStore = resource.copyWithAmount(filled);
                mutateTankTag(container, tag -> tag.put(TAG_FLUID, saveFluid(toStore)));
            }
            return filled;
        }
        if (!FluidStack.isSameFluidSameComponents(stored, resource)) {
            return 0;
        }
        int filled = Math.min(capacity - stored.getAmount(), resource.getAmount());
        if (action.execute() && filled > 0) {
            FluidStack toStore = stored.copyWithAmount(stored.getAmount() + filled);
            mutateTankTag(container, tag -> tag.put(TAG_FLUID, saveFluid(toStore)));
        }
        return filled;
    }

    /**
     * @param container ItemStack which is the fluid container.
     * @param maxDrain  Maximum amount of fluid to be removed from the container.
     * @param action    If SIMULATE, the drain will only be simulated.
     * @return Fluidstack holding the amount of fluid that was (or would have been, if simulated) drained from the
     * container.
     */
    default FluidStack drain(ItemStack container, int maxDrain, FluidAction action) {

        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        FluidStack stored = getFluid(container);
        if (stored.isEmpty()) {
            return FluidStack.EMPTY;
        }
        boolean creative = isCreative(container, FLUID);
        int drained = creative ? maxDrain : Math.min(stored.getAmount(), maxDrain);
        if (action.execute() && !creative) {
            if (maxDrain >= stored.getAmount()) {
                mutateTankTag(container, tag -> tag.remove(TAG_FLUID));
                return stored;
            }
            FluidStack remaining = stored.copyWithAmount(stored.getAmount() - drained);
            mutateTankTag(container, tag -> tag.put(TAG_FLUID, saveFluid(remaining)));
        }
        return stored.copyWithAmount(drained);
    }

}
