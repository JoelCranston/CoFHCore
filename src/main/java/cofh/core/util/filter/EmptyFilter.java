package cofh.core.util.filter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class EmptyFilter implements IFilter {

    public static final EmptyFilter INSTANCE = new EmptyFilter();

    @Override
    public IFilter read(HolderLookup.Provider provider, CompoundTag nbt) {

        return INSTANCE;
    }

    @Override
    public CompoundTag write(HolderLookup.Provider provider, CompoundTag nbt) {

        return nbt;
    }

}
