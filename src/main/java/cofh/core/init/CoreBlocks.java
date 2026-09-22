package cofh.core.init;

import cofh.core.common.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;

import static cofh.core.CoFHCore.BLOCKS;
import static cofh.core.util.references.CoreIDs.*;
import static cofh.lib.util.helpers.BlockHelper.lightValue;
import static net.minecraft.world.level.block.state.BlockBehaviour.Properties.ofFullCopy;

public class CoreBlocks {

    private CoreBlocks() {

    }

    public static void register() {

    }

    public static final DeferredHolder<Block, Block> GLOSSED_MAGMA = BLOCKS.register(ID_GLOSSED_MAGMA, id -> new GlossedMagmaBlock(ofFullCopy(Blocks.MAGMA_BLOCK).lightLevel(lightValue(6)).setId(ResourceKey.create(Registries.BLOCK, id))));
    public static final DeferredHolder<Block, Block> SIGNAL_AIR = BLOCKS.register(ID_SIGNAL_AIR, id -> new SignalAirBlock(ofFullCopy(Blocks.AIR).lightLevel(lightValue(7)).setId(ResourceKey.create(Registries.BLOCK, id))));
    public static final DeferredHolder<Block, Block> GLOW_AIR = BLOCKS.register(ID_GLOW_AIR, id -> new GlowAirBlock(ofFullCopy(Blocks.AIR).lightLevel(lightValue(15)).setId(ResourceKey.create(Registries.BLOCK, id))));
    public static final DeferredHolder<Block, Block> ENDER_AIR = BLOCKS.register(ID_ENDER_AIR, id -> new EnderAirBlock(ofFullCopy(Blocks.AIR).lightLevel(lightValue(3)).setId(ResourceKey.create(Registries.BLOCK, id))));
    public static final DeferredHolder<Block, Block> LIGHTNING_AIR = BLOCKS.register(ID_LIGHTNING_AIR, id -> new LightningAirBlock(ofFullCopy(Blocks.AIR).setId(ResourceKey.create(Registries.BLOCK, id))));

}
