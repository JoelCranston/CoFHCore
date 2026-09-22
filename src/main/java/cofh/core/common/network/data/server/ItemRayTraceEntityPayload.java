package cofh.core.common.network.data.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ItemRayTraceEntityPayload(InteractionHand hand, Vec3 origin, int targetId, Vec3 offset,
                                        float power) implements CustomPacketPayload {

    public static final Type<ItemRayTraceEntityPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "item_ray_trace_entity_packet"));

    private static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC = ByteBufCodecs.idMapper(
            i -> InteractionHand.values()[i], InteractionHand::ordinal
    );
    private static final StreamCodec<ByteBuf, Vec3> VEC3_CODEC = Vec3.STREAM_CODEC;

    public static final StreamCodec<FriendlyByteBuf, ItemRayTraceEntityPayload> STREAM_CODEC = StreamCodec.composite(
            HAND_CODEC, ItemRayTraceEntityPayload::hand,
            VEC3_CODEC, ItemRayTraceEntityPayload::origin,
            ByteBufCodecs.VAR_INT, ItemRayTraceEntityPayload::targetId,
            VEC3_CODEC, ItemRayTraceEntityPayload::offset,
            ByteBufCodecs.FLOAT, ItemRayTraceEntityPayload::power,
            ItemRayTraceEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
