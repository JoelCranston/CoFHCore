package cofh.core.common.network.packet.server;

import cofh.core.common.item.IBlockRayTraceItem;
import cofh.core.common.network.data.server.ItemRayTraceBlockPayload;
import cofh.core.util.ProxyUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ItemRayTraceBlockPacket {

    public static final ItemRayTraceBlockPacket INSTANCE = new ItemRayTraceBlockPacket();

    public static ItemRayTraceBlockPacket get() {

        return INSTANCE;
    }

    public void handle(final ItemRayTraceBlockPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                ItemStack stack = player.getItemInHand(payload.hand());
                if (stack.getItem() instanceof IBlockRayTraceItem item) {
                    item.handleBlockRayTrace(serverPlayer.level(), serverPlayer, payload.hand(), stack, payload.origin(), payload.result());
                }
            }
        });
    }

    public static void sendToServer(Player player, InteractionHand hand, Vec3 origin, BlockHitResult result) {

        Player client = ProxyUtils.getClientPlayer();
        if (client != null && client.equals(player)) {
            ClientPacketDistributor.sendToServer(new ItemRayTraceBlockPayload(hand, origin, result));
        }
    }

}
