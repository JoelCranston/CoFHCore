package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record RedstoneControlPayload(BlockPos pos, int threshold, byte mode) implements CustomPacketPayload {

    public static final Type<RedstoneControlPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "redstone_control_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, RedstoneControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RedstoneControlPayload::pos,
            ByteBufCodecs.INT, RedstoneControlPayload::threshold,
            ByteBufCodecs.BYTE, RedstoneControlPayload::mode,
            RedstoneControlPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
