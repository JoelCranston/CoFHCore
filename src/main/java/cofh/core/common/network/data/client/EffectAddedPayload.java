package cofh.core.common.network.data.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record EffectAddedPayload(int entityId, Identifier effect, int duration) implements CustomPacketPayload {

    public static final Type<EffectAddedPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "effect_added_packet"));

    public static final StreamCodec<ByteBuf, EffectAddedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, EffectAddedPayload::entityId,
            Identifier.STREAM_CODEC, EffectAddedPayload::effect,
            ByteBufCodecs.INT, EffectAddedPayload::duration,
            EffectAddedPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
