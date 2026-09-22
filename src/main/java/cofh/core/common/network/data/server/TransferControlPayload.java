package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TransferControlPayload(BlockPos pos, boolean transferIn,
                                     boolean transferOut) implements CustomPacketPayload {

    public static final Type<TransferControlPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "transfer_control_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, TransferControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TransferControlPayload::pos,
            ByteBufCodecs.BOOL, TransferControlPayload::transferIn,
            ByteBufCodecs.BOOL, TransferControlPayload::transferOut,
            TransferControlPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
