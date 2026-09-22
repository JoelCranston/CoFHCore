package cofh.core.common.network.data.server;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ItemModeChangePayload(boolean decr) implements CustomPacketPayload {

    public static final Type<ItemModeChangePayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "item_mode_change_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, ItemModeChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ItemModeChangePayload::decr,
            ItemModeChangePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
