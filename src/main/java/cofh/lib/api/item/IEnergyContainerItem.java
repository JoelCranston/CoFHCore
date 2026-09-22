package cofh.lib.api.item;

import cofh.lib.util.helpers.MathHelper;
import cofh.core.util.helpers.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static cofh.lib.api.ContainerType.ENERGY;
import static cofh.lib.util.constants.NBTTags.TAG_ENERGY;

/**
 * Implement this interface on Item classes that support external manipulation of their internal energy storage.
 * <p>
 * NOTE: Use of NBT data on the containing ItemStack is encouraged.
 *
 * @author King Lemming
 */
public interface IEnergyContainerItem extends IContainerItem {

    /**
     * The NBT the stored energy lives in, as a copy - 1.20.5+ item data is immutable
     * {@link net.minecraft.world.item.component.CustomData}, so writes go through
     * {@link #mutateEnergyTag}. Implementations that keep energy somewhere else (a block item's
     * block entity data, say) override both.
     */
    default CompoundTag getEnergyTag(ItemStack container) {

        return ItemHelper.getCustomData(container);
    }

    default void mutateEnergyTag(ItemStack container, Consumer<CompoundTag> mutator) {

        ItemHelper.mutateCustomData(container, mutator);
    }

    default int getSpace(ItemStack container) {

        return getMaxEnergyStored(container) - getEnergyStored(container);
    }

    default int getScaledEnergyStored(ItemStack container, int scale) {

        return MathHelper.round((double) getEnergyStored(container) * scale / getMaxEnergyStored(container));
    }

    /**
     * Get the amount of energy currently stored in the container item.
     */
    default int getEnergyStored(ItemStack container) {

        return Math.min(getEnergyTag(container).getInt(TAG_ENERGY), getMaxEnergyStored(container));
    }

    int getExtract(ItemStack container);

    int getReceive(ItemStack container);

    /**
     * Get the max amount of energy that can be stored in the container item.
     */
    int getMaxEnergyStored(ItemStack container);

    default void setEnergyStored(ItemStack container, int energy) {

        mutateEnergyTag(container, tag -> tag.putInt(TAG_ENERGY, MathHelper.clamp(energy, 0, getMaxEnergyStored(container))));
    }

    /**
     * Adds energy to a container item. Returns the quantity of energy that was accepted. This should always return 0
     * if the item cannot be externally charged.
     *
     * @param container  ItemStack to be charged.
     * @param maxReceive Maximum amount of energy to be sent into the item.
     * @param simulate   If TRUE, the charge will only be simulated.
     * @return Amount of energy that was (or would have been, if simulated) received by the item.
     */
    default int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

        if (isCreative(container, ENERGY)) {
            return 0;
        }
        int stored = getEnergyStored(container);
        int receive = Math.min(Math.min(maxReceive, getReceive(container)), getSpace(container));

        if (!simulate) {
            int total = stored + receive;
            mutateEnergyTag(container, tag -> tag.putInt(TAG_ENERGY, total));
        }
        return receive;
    }

    /**
     * Removes energy from a container item. Returns the quantity of energy that was removed. This should always
     * return 0 if the item cannot be externally discharged.
     *
     * @param container  ItemStack to be discharged.
     * @param maxExtract Maximum amount of energy to be extracted from the item.
     * @param simulate   If TRUE, the discharge will only be simulated.
     * @return Amount of energy that was (or would have been, if simulated) extracted from the item.
     */
    default int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {

        if (isCreative(container, ENERGY)) {
            return maxExtract;
        }
        int stored = getEnergyStored(container);
        int extract = Math.min(Math.min(maxExtract, getExtract(container)), stored);

        if (!simulate) {
            int total = stored - extract;
            mutateEnergyTag(container, tag -> tag.putInt(TAG_ENERGY, total));
        }
        return extract;
    }

}
