package cofh.core.common.network.packet.client;

import cofh.core.common.network.data.client.TileControlPayload;
import cofh.core.util.ProxyUtils;
import cofh.lib.api.block.entity.IPacketHandlerTile;
import cofh.lib.util.Utils;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TileControlPacket {

    public static final TileControlPacket INSTANCE = new TileControlPacket();

    public static TileControlPacket get() {

        return INSTANCE;
    }

    public void handle(final TileControlPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Level world = ProxyUtils.getClientWorld();

            BlockPos pos = payload.pos();

            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof IPacketHandlerTile handlerTile) {
                handlerTile.handleControlPacket(payload.buf());
                BlockState state = world.getBlockState(pos);
                world.sendBlockUpdated(pos, state, state, 3);
            }
        });
    }

    public static void sendToClient(IPacketHandlerTile tile) {

        if (tile == null || tile.world() == null || tile.world().isClientSide()) {
            return;
        }
        Utils.sendNear(tile.world(), tile.pos(), new TileControlPayload(tile.pos(), tile.getControlPacket(new FriendlyByteBuf(Unpooled.buffer()))));
    }

}
