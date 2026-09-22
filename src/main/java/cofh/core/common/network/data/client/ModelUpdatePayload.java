package cofh.core.common.network.data.client;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ModelUpdatePayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ModelUpdatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "model_update_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, ModelUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ModelUpdatePayload::pos,
            ModelUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
