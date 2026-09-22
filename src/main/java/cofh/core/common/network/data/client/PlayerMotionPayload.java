package cofh.core.common.network.data.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record PlayerMotionPayload(double motionX, double motionY, double motionZ) implements CustomPacketPayload {

    public static final Type<PlayerMotionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "player_motion_packet"));

    public static final StreamCodec<ByteBuf, PlayerMotionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, PlayerMotionPayload::motionX,
            ByteBufCodecs.DOUBLE, PlayerMotionPayload::motionY,
            ByteBufCodecs.DOUBLE, PlayerMotionPayload::motionZ,
            PlayerMotionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
