package cofh.core.init;

import cofh.core.common.block.entity.EnderAirBlockEntity;
import cofh.core.common.block.entity.GlowAirBlockEntity;
import cofh.core.common.block.entity.LightningAirBlockEntity;
import cofh.core.common.block.entity.SignalAirTile;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

import static cofh.core.CoFHCore.TILE_ENTITIES;
import static cofh.core.init.CoreBlocks.*;
import static cofh.core.util.references.CoreIDs.*;

public class CoreBlockEntities {

    private CoreBlockEntities() {

    }

    public static void register() {


    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SignalAirTile>> SIGNAL_AIR_TILE = TILE_ENTITIES.register(ID_SIGNAL_AIR, () -> new BlockEntityType<>(SignalAirTile::new, SIGNAL_AIR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlowAirBlockEntity>> GLOW_AIR_TILE = TILE_ENTITIES.register(ID_GLOW_AIR, () -> new BlockEntityType<>(GlowAirBlockEntity::new, GLOW_AIR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnderAirBlockEntity>> ENDER_AIR_TILE = TILE_ENTITIES.register(ID_ENDER_AIR, () -> new BlockEntityType<>(EnderAirBlockEntity::new, ENDER_AIR.get()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightningAirBlockEntity>> LIGHTNING_AIR_TILE = TILE_ENTITIES.register(ID_LIGHTNING_AIR, () -> new BlockEntityType<>(LightningAirBlockEntity::new, LIGHTNING_AIR.get()));

}
