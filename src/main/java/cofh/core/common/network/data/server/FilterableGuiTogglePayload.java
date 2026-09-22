package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

// CustomPacketPayload#type() replaced id() in 1.20.5, so a record component may not be
// called "type" any more.
public record FilterableGuiTogglePayload(int toggleType, int entityId, BlockPos pos,
                                         byte mode) implements CustomPacketPayload {

    public static final Type<FilterableGuiTogglePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "filterable_gui_toggle_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, FilterableGuiTogglePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FilterableGuiTogglePayload::toggleType,
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
