package cofh.lib.init.data.loot;

import cofh.lib.common.loot.TileNBTSync;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public abstract class BlockLootSubProviderCoFH extends BlockLootSubProvider {

    private final Set<Block> knownBlocks = new ReferenceOpenHashSet<>();

    protected BlockLootSubProviderCoFH(HolderLookup.Provider registries) {

        super(Collections.emptySet(), FeatureFlags.VANILLA_SET, registries);
    }

    @Override
    protected void add(@NotNull Block block, @NotNull LootTable.Builder table) {

        super.add(block, table);
        knownBlocks.add(block);
    }

    @NotNull
    @Override
    protected Iterable<Block> getKnownBlocks() {

        return knownBlocks;
    }

    protected void createSimpleDropTable(Block block) {

        add(block, getSimpleDropTable(block));
    }

    protected void createSyncDropTable(Block block) {

        add(block, getSyncDropTable(block));
    }

    // region TABLE HELPERS
    protected Holder<Enchantment> fortune() {

        return this.registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
    }

    protected LootTable.Builder getSilkTouchTable(String name, Block block, Item lootItem, float min, float max, int bonus) {

        LootPool.Builder builder = LootPool.lootPool()
                .name(name)
                .setRolls(ConstantValue.exactly(1))
                .add(AlternativesEntry.alternatives(LootItem.lootTableItem(block)
                        .when(hasSilkTouch()), LootItem.lootTableItem(lootItem)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                        .apply(ApplyBonusCount.addUniformBonusCount(fortune(), bonus))
                        .apply(ApplyExplosionDecay.explosionDecay())));
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder getSilkTouchOreTable(Block block, Item lootItem) {

        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(AlternativesEntry.alternatives(LootItem.lootTableItem(block)
                        .when(hasSilkTouch()), LootItem.lootTableItem(lootItem)
                        .apply(ApplyBonusCount.addOreBonusCount(fortune()))
                        .apply(ApplyExplosionDecay.explosionDecay())));
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder getCropTable(Block block, Item crop, Item seed, IntegerProperty ageProp, int age) {

        LootItemCondition.Builder harvestAge = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ageProp, age));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(crop)
                                .when(harvestAge)
                                .otherwise(LootItem.lootTableItem(seed))))
                .withPool(LootPool.lootPool()
                        .when(harvestAge)
                        .add(LootItem.lootTableItem(seed)
                                // These are Mojang's numbers. No idea.
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(fortune(), 0.5714286F, 3))))
                .apply(ApplyExplosionDecay.explosionDecay());
    }

    protected LootTable.Builder getTuberTable(Block block, Item crop, IntegerProperty ageProp, int age) {

        LootItemCondition.Builder harvestAge = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ageProp, age));
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(crop)))
                .withPool(LootPool.lootPool()
                        .when(harvestAge)
                        .add(LootItem.lootTableItem(crop)
                                .apply(ApplyBonusCount.addBonusBinomialDistributionCount(fortune(), 0.5714286F, 3)))
                        .apply(ApplyExplosionDecay.explosionDecay()));
    }

    protected LootTable.Builder getSimpleDropTable(Block block) {

        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block))
                .when(ExplosionCondition.survivesExplosion());
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder getEmptyTable() {

        return LootTable.lootTable();
    }

    protected LootTable.Builder getStandardTileTable(BlockEntityType<?> type, String name, Block block) {

        LootPool.Builder builder = LootPool.lootPool()
                .name(name)
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block)
                        .apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
                        // Copies into custom data, not block entity data; no BlockEntityTag prefix.
                        .apply(CopyCustomDataFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                                .copy("Info", "Info", CopyCustomDataFunction.MergeStrategy.REPLACE)
                                .copy("Items", "Items", CopyCustomDataFunction.MergeStrategy.REPLACE)
                                .copy("Energy", "Energy", CopyCustomDataFunction.MergeStrategy.REPLACE))
                        .apply(SetContainerContents.setContents(ContainerComponentManipulators.CONTAINER)
                                .withEntry(DynamicLoot.dynamicEntry(Identifier.withDefaultNamespace("contents")))));
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder getSyncDropTable(Block block) {

        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block)
                        .apply(TileNBTSync.builder()))
                .when(ExplosionCondition.survivesExplosion());
        return LootTable.lootTable().withPool(builder);
    }
    // endregion
}
