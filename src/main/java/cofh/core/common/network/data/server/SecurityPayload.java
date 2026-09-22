package cofh.core.common.network.data.server;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record SecurityPayload(byte mode) implements CustomPacketPayload {

    public static final Type<SecurityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "security_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, SecurityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, SecurityPayload::mode,
            SecurityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
