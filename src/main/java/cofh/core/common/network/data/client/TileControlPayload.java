package cofh.core.common.network.data.client;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TileControlPayload(BlockPos pos, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<TileControlPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "tile_control_packet"));

    public static final StreamCodec<FriendlyByteBuf, TileControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileControlPayload::pos,
            PayloadCodecs.REMAINING_BYTES, TileControlPayload::buf,
            TileControlPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
