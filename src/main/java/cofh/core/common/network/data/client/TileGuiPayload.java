package cofh.core.common.network.data.client;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record TileGuiPayload(BlockPos pos, FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<TileGuiPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "tile_gui_packet"));

    public static final StreamCodec<FriendlyByteBuf, TileGuiPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TileGuiPayload::pos,
            PayloadCodecs.REMAINING_BYTES, TileGuiPayload::buf,
            TileGuiPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
