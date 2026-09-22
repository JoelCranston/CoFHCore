package cofh.lib.init.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LootTableProviderCoFH extends LootTableProvider {

    protected LootTableProviderCoFH(PackOutput output, List<LootTableProvider.SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> registries) {

        this(output, Collections.emptySet(), subProviders, registries);
    }

    protected LootTableProviderCoFH(PackOutput output, Set<ResourceKey<LootTable>> requiredTables, List<SubProviderEntry> subProviders, CompletableFuture<HolderLookup.Provider> registries) {

        super(output, requiredTables, subProviders, registries);
    }

}


