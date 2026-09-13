package cofh.core.common.network.data.server;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public class ItemLeftClickPayload implements CustomPacketPayload {

    public static final Type<ItemLeftClickPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "item_left_click_packet"));

    public static final ItemLeftClickPayload INSTANCE = new ItemLeftClickPayload();

    public static final StreamCodec<io.netty.buffer.ByteBuf, ItemLeftClickPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    public ItemLeftClickPayload() {

    }

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
