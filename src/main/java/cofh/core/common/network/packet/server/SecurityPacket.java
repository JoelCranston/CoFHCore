package cofh.core.common.network.packet.server;

import cofh.core.common.network.data.server.SecurityPayload;
import cofh.lib.api.control.ISecurable;
import cofh.lib.api.control.ISecurable.AccessMode;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public class SecurityPacket {

    public static final SecurityPacket INSTANCE = new SecurityPacket();

    public static SecurityPacket get() {

        return INSTANCE;
    }

    public void handle(final SecurityPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Player player = context.player();

            if (player.containerMenu instanceof ISecurable securable) {
                securable.setAccess(AccessMode.VALUES[payload.mode()]);
            }
        });
    }

    public static void sendToServer(AccessMode accessMode) {

        PacketDistributor.sendToServer(new SecurityPayload((byte) accessMode.ordinal()));
    }

}
