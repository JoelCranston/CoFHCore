package cofh.core.common.network.data.client;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ContainerGuiPayload(FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<ContainerGuiPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "container_gui_packet"));

    public static final StreamCodec<FriendlyByteBuf, ContainerGuiPayload> STREAM_CODEC = PayloadCodecs.REMAINING_BYTES.map(
            ContainerGuiPayload::new, ContainerGuiPayload::buf
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
