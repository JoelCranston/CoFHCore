package cofh.core.init.data.tables;

import cofh.lib.init.data.loot.BlockLootSubProviderCoFH;
import net.minecraft.core.HolderLookup;

import static cofh.core.init.CoreBlocks.GLOSSED_MAGMA;

public class CoreBlockLootTables extends BlockLootSubProviderCoFH {

    public CoreBlockLootTables(HolderLookup.Provider registries) {

        super(registries);
    }

    @Override
    protected void generate() {

        add(GLOSSED_MAGMA.get(), getEmptyTable());
    }

}
