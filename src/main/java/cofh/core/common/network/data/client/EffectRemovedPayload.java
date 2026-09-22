package cofh.core.common.network.data.client;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record EffectRemovedPayload(int entityId, Identifier effect) implements CustomPacketPayload {

    public static final Type<EffectRemovedPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_removed_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, EffectRemovedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EffectRemovedPayload::entityId,
            Identifier.STREAM_CODEC, EffectRemovedPayload::effect,
            EffectRemovedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
