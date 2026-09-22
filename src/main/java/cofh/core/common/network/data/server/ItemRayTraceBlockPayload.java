package cofh.core.common.network.data.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record ItemRayTraceBlockPayload(InteractionHand hand, Vec3 origin,
                                       BlockHitResult result) implements CustomPacketPayload {

    public static final Type<ItemRayTraceBlockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "item_ray_trace_block_packet"));

    private static final StreamCodec<ByteBuf, InteractionHand> HAND_CODEC = ByteBufCodecs.idMapper(
            i -> InteractionHand.values()[i], InteractionHand::ordinal
    );
    private static final StreamCodec<FriendlyByteBuf, Vec3> VEC3_CODEC = StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);
    private static final StreamCodec<FriendlyByteBuf, BlockHitResult> BLOCK_HIT_RESULT_CODEC = StreamCodec.of(FriendlyByteBuf::writeBlockHitResult, FriendlyByteBuf::readBlockHitResult);

    public static final StreamCodec<FriendlyByteBuf, ItemRayTraceBlockPayload> STREAM_CODEC = StreamCodec.composite(
            HAND_CODEC, ItemRayTraceBlockPayload::hand,
            VEC3_CODEC, ItemRayTraceBlockPayload::origin,
            BLOCK_HIT_RESULT_CODEC, ItemRayTraceBlockPayload::result,
            ItemRayTraceBlockPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
