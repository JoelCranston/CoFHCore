package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record FilterableGuiTogglePayload(int type, int entityId, BlockPos pos,
                                         byte mode) implements CustomPacketPayload {

    public static final Type<FilterableGuiTogglePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "filterable_gui_toggle_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, FilterableGuiTogglePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FilterableGuiTogglePayload::type,
            ByteBufCodecs.VAR_INT, FilterableGuiTogglePayload::entityId,
            BlockPos.STREAM_CODEC, FilterableGuiTogglePayload::pos,
            ByteBufCodecs.BYTE, FilterableGuiTogglePayload::mode,
            FilterableGuiTogglePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
