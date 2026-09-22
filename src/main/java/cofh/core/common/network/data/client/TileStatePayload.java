package cofh.core.common.network.data.client;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TileStatePayload(BlockPos pos, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<TileStatePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "tile_state_packet"));

    public static final StreamCodec<FriendlyByteBuf, TileStatePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileStatePayload::pos,
            PayloadCodecs.REMAINING_BYTES, TileStatePayload::buf,
            TileStatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
