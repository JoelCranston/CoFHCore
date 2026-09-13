package cofh.core.common.network;

import cofh.core.common.network.data.client.*;
import cofh.core.common.network.data.server.*;
import cofh.core.common.network.packet.client.*;
import cofh.core.common.network.packet.server.*;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class PacketHandler {

    public static void registerNetworking(final RegisterPayloadHandlersEvent event) {

        final PayloadRegistrar registrar = event.registrar("1");

        // SERVER
        registrar.playToServer(ClaimXPPayload.TYPE, ClaimXPPayload.STREAM_CODEC, ClaimXPPacket.get()::handle);
        registrar.playToServer(ContainerConfigPayload.TYPE, ContainerConfigPayload.STREAM_CODEC, ContainerConfigPacket.get()::handle);
        registrar.playToServer(FilterableGuiTogglePayload.TYPE, FilterableGuiTogglePayload.STREAM_CODEC, FilterableGuiTogglePacket.get()::handle);
        registrar.playToServer(GhostItemPayload.TYPE, GhostItemPayload.STREAM_CODEC, GhostItemPacket.get()::handle);
        registrar.playToServer(ItemLeftClickPayload.TYPE, ItemLeftClickPayload.STREAM_CODEC, ItemLeftClickPacket.get()::handle);
        registrar.playToServer(ItemModeChangePayload.TYPE, ItemModeChangePayload.STREAM_CODEC, ItemModeChangePacket.get()::handle);
        registrar.playToServer(ItemRayTraceBlockPayload.TYPE, ItemRayTraceBlockPayload.STREAM_CODEC, ItemRayTraceBlockPacket.get()::handle);
        registrar.playToServer(ItemRayTraceEntityPayload.TYPE, ItemRayTraceEntityPayload.STREAM_CODEC, ItemRayTraceEntityPacket.get()::handle);
        registrar.playToServer(RedstoneControlPayload.TYPE, RedstoneControlPayload.STREAM_CODEC, RedstoneControlPacket.get()::handle);
        registrar.playToServer(SecurityControlPayload.TYPE, SecurityControlPayload.STREAM_CODEC, SecurityControlPacket.get()::handle);
        registrar.playToServer(SecurityPayload.TYPE, SecurityPayload.STREAM_CODEC, SecurityPacket.get()::handle);
        registrar.playToServer(SideConfigPayload.TYPE, SideConfigPayload.STREAM_CODEC, SideConfigPacket.get()::handle);
        registrar.playToServer(StorageClearPayload.TYPE, StorageClearPayload.STREAM_CODEC, StorageClearPacket.get()::handle);
        registrar.playToServer(TileConfigPayload.TYPE, TileConfigPayload.STREAM_CODEC, TileConfigPacket.get()::handle);
        registrar.playToServer(TransferControlPayload.TYPE, TransferControlPayload.STREAM_CODEC, TransferControlPacket.get()::handle);

        // CLIENT
        registrar.playToClient(ContainerGuiPayload.TYPE, ContainerGuiPayload.STREAM_CODEC, ContainerGuiPacket.get()::handle);
        registrar.playToClient(EffectAddedPayload.TYPE, EffectAddedPayload.STREAM_CODEC, EffectAddedPacket.get()::handle);
        registrar.playToClient(EffectRemovedPayload.TYPE, EffectRemovedPayload.STREAM_CODEC, EffectRemovedPacket.get()::handle);
        registrar.playToClient(ModelUpdatePayload.TYPE, ModelUpdatePayload.STREAM_CODEC, ModelUpdatePacket.get()::handle);
        registrar.playToClient(OverlayMessagePayload.TYPE, OverlayMessagePayload.STREAM_CODEC, OverlayMessagePacket.get()::handle);
        registrar.playToClient(PlayerMotionPayload.TYPE, PlayerMotionPayload.STREAM_CODEC, PlayerMotionPacket.get()::handle);
        registrar.playToClient(TileControlPayload.TYPE, TileControlPayload.STREAM_CODEC, TileControlPacket.get()::handle);
        registrar.playToClient(TileGuiPayload.TYPE, TileGuiPayload.STREAM_CODEC, TileGuiPacket.get()::handle);
        registrar.playToClient(TileRedstonePayload.TYPE, TileRedstonePayload.STREAM_CODEC, TileRedstonePacket.get()::handle);
        registrar.playToClient(TileRenderPayload.TYPE, TileRenderPayload.STREAM_CODEC, TileRenderPacket.get()::handle);
        registrar.playToClient(TileStatePayload.TYPE, TileStatePayload.STREAM_CODEC, TileStatePacket.get()::handle);
    }

}
