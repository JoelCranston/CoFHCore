package cofh.core.common.network.data.client;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TileRenderPayload(BlockPos pos, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<TileRenderPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "tile_render_packet"));

    public static final StreamCodec<FriendlyByteBuf, TileRenderPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileRenderPayload::pos,
            PayloadCodecs.REMAINING_BYTES, TileRenderPayload::buf,
            TileRenderPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
