package cofh.core.common.network.data.client;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record EffectAddedPayload(int entityId, ResourceLocation effect, int duration) implements CustomPacketPayload {

    public static final Type<EffectAddedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "effect_added_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, EffectAddedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EffectAddedPayload::entityId,
            ResourceLocation.STREAM_CODEC, EffectAddedPayload::effect,
            ByteBufCodecs.INT, EffectAddedPayload::duration,
            EffectAddedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
