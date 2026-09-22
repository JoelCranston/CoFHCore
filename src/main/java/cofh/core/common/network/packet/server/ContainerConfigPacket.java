package cofh.core.common.network.packet.server;

import cofh.core.common.inventory.ContainerMenuCoFH;
import cofh.core.common.network.data.server.ContainerConfigPayload;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ContainerConfigPacket {

    public static final ContainerConfigPacket INSTANCE = new ContainerConfigPacket();

    public static ContainerConfigPacket get() {

        return INSTANCE;
    }

    public void handle(final ContainerConfigPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Player player = context.player();

            if (player.containerMenu instanceof ContainerMenuCoFH container) {
                container.handleConfigPacket(payload.buf());
            }
        });
    }

    public static void sendToServer(ContainerMenuCoFH container) {

        if (container == null) {
            return;
        }
        PacketDistributor.sendToServer(new ContainerConfigPayload(container.getConfigPacket(new FriendlyByteBuf(Unpooled.buffer()))));
    }

}
