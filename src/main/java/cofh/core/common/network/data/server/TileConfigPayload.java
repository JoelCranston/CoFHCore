package cofh.core.common.network.data.server;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TileConfigPayload(BlockPos pos, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<TileConfigPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "tile_config_packet"));

    public static final StreamCodec<FriendlyByteBuf, TileConfigPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileConfigPayload::pos,
            PayloadCodecs.REMAINING_BYTES, TileConfigPayload::buf,
            TileConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
