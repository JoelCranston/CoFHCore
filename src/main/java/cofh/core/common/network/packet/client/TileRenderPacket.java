package cofh.core.common.network.packet.client;

import cofh.core.common.network.data.client.TileRenderPayload;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.block.entity.IPacketHandlerTile;
import cofh.lib.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TileRenderPacket {

    public static final TileRenderPacket INSTANCE = new TileRenderPacket();

    public static TileRenderPacket get() {

        return INSTANCE;
    }

    public void handle(final TileRenderPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Level world = ProxyUtils.getClientWorld();

            BlockPos pos = payload.pos();

            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IPacketHandlerTile handlerTile) {
                handlerTile.handleRenderPacket(payload.buf());
            }
        });
    }

    public static void sendToClient(IPacketHandlerTile tile) {

        if (tile == null || tile.world() == null || tile.world().isClientSide()) {
            return;
        }
        Utils.sendNear(tile.world(), tile.pos(), new TileRenderPayload(tile.pos(), tile.getRenderPacket(new FriendlyByteBuf(Unpooled.buffer()))));
    }

}
