package cofh.core.common.network.data.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record OverlayMessagePayload(String message) implements CustomPacketPayload {

    public static final Type<OverlayMessagePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "overlay_message_packet"));

    public static final StreamCodec<ByteBuf, OverlayMessagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, OverlayMessagePayload::message,
            OverlayMessagePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
