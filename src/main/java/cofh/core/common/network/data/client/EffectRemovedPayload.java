package cofh.core.common.network.data.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record EffectRemovedPayload(int entityId, ResourceLocation effect) implements CustomPacketPayload {

    public static final Type<EffectRemovedPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "effect_removed_packet"));

    public static final StreamCodec<ByteBuf, EffectRemovedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EffectRemovedPayload::entityId,
            ResourceLocation.STREAM_CODEC, EffectRemovedPayload::effect,
            EffectRemovedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
