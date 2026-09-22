package cofh.core.common.network.packet.server;

import cofh.core.common.block.entity.ITileXpHandler;
import cofh.core.common.network.data.server.ClaimXPPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClaimXPPacket {

    public static final ClaimXPPacket INSTANCE = new ClaimXPPacket();

    public static ClaimXPPacket get() {

        return INSTANCE;
    }

    public void handle(final ClaimXPPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Player player = context.player();

            Level world = player.level;
            if (!world.isLoaded(payload.pos())) {
                return;
            }
            BlockEntity tile = world.getBlockEntity(payload.pos());
            if (tile instanceof ITileXpHandler tileXpHandler) {
                tileXpHandler.claimXP(player);
            }
        });
    }

    public static boolean sendToServer(ITileXpHandler tile) {

        if (tile == null) {
            return false;
        }
        ClientPacketDistributor.sendToServer(new ClaimXPPayload(tile.pos()));
        return true;
    }

}
