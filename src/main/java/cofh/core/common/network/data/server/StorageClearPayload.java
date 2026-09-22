package cofh.core.common.network.data.server;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record StorageClearPayload(BlockPos pos, int storageType, int index) implements CustomPacketPayload {

    public static final Type<StorageClearPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "storage_clear_packet"));

    public static final StreamCodec<ByteBuf, StorageClearPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, StorageClearPayload::pos,
            ByteBufCodecs.INT, StorageClearPayload::storageType,
            ByteBufCodecs.INT, StorageClearPayload::index,
            StorageClearPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
