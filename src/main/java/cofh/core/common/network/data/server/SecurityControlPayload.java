package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record SecurityControlPayload(BlockPos pos, byte mode) implements CustomPacketPayload {

    public static final Type<SecurityControlPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "security_control_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, SecurityControlPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SecurityControlPayload::pos,
            ByteBufCodecs.BYTE, SecurityControlPayload::mode,
            SecurityControlPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
