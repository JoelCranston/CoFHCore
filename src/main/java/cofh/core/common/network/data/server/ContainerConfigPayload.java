package cofh.core.common.network.data.server;

import cofh.core.common.network.data.PayloadCodecs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ContainerConfigPayload(FriendlyByteBuf buf) implements CustomPacketPayload {

    public static final Type<ContainerConfigPayload> TYPE = new Type<>(new ResourceLocation(ID_COFH_CORE, "container_config_packet"));

    public static final StreamCodec<FriendlyByteBuf, ContainerConfigPayload> STREAM_CODEC = PayloadCodecs.REMAINING_BYTES.map(
            ContainerConfigPayload::new, ContainerConfigPayload::buf
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
