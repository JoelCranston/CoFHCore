package cofh.core.util.filter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Predicate;

public interface IFilter extends INBTSerializable<CompoundTag> {

    Predicate<ItemStack> ALWAYS_ALLOW_ITEM = (item) -> true;
    Predicate<FluidStack> ALWAYS_ALLOW_FLUID = (fluid) -> true;

    default Predicate<ItemStack> getItemRules() {

        return ALWAYS_ALLOW_ITEM;
    }

    default Predicate<FluidStack> getFluidRules() {

        return ALWAYS_ALLOW_FLUID;
    }

    default boolean valid(ItemStack item) {

        return getItemRules().test(item);
    }

    default boolean valid(FluidStack fluid) {

        return getFluidRules().test(fluid);
    }

    IFilter read(HolderLookup.Provider provider, CompoundTag nbt);

    CompoundTag write(HolderLookup.Provider provider, CompoundTag nbt);

    @Override
    default CompoundTag serializeNBT(HolderLookup.Provider provider) {

        return write(provider, new CompoundTag());
    }

    @Override
    default void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {

        read(provider, nbt);
    }

}
