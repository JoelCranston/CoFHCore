package cofh.core.common.network.packet.server;

import cofh.core.common.network.data.server.TileConfigPayload;
import cofh.lib.api.block.entity.IPacketHandlerTile;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class TileConfigPacket {

    public static final TileConfigPacket INSTANCE = new TileConfigPacket();

    public static TileConfigPacket get() {

        return INSTANCE;
    }

    public void handle(final TileConfigPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Player player = context.player();

            Level world = player.level;
            if (!world.isLoaded(payload.pos())) {
                return;
            }
            BlockEntity tile = world.getBlockEntity(payload.pos());
            if (tile instanceof IPacketHandlerTile handlerTile) {
                handlerTile.handleConfigPacket(payload.buf());
            }
        });
    }

    public static void sendToServer(IPacketHandlerTile tile) {

        if (tile == null) {
            return;
        }
        ClientPacketDistributor.sendToServer(new TileConfigPayload(tile.pos(), tile.getConfigPacket(new FriendlyByteBuf(Unpooled.buffer()))));
    }

}
