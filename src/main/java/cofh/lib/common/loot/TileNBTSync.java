package cofh.lib.common.loot;

import cofh.lib.api.block.entity.ITileCallback;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;
import static net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY;

public class TileNBTSync extends LootItemConditionalFunction {

    public static final MapCodec<TileNBTSync> CODEC = RecordCodecBuilder.mapCodec(
            instance -> commonFields(instance)
                    .apply(instance, TileNBTSync::new)
    );

    private static boolean registered;

    public static void setup() {

        if (registered) {
            return;
        }
        Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, Identifier.parse(ID_COFH_CORE + ":nbt_sync"), CODEC);
        registered = true;
    }

    protected TileNBTSync(List<LootItemCondition> conditionsIn) {

        super(conditionsIn);
    }

    @Override
    public MapCodec<TileNBTSync> codec() {

        return CODEC;
    }

    @Override
    public ItemStack run(ItemStack stack, LootContext context) {

        return applyToStack(stack, context.getOptionalParameter(BLOCK_ENTITY));
    }

    public static ItemStack applyToStack(ItemStack stack, BlockEntity tile) {

        if (tile instanceof ITileCallback) {
            return ((ITileCallback) tile).createItemStackTag(stack);
        }
        return stack;
    }

    public static LootItemConditionalFunction.Builder<?> builder() {

        return simpleBuilder(TileNBTSync::new);
    }

}
