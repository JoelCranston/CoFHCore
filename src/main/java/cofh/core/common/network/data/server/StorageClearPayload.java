package cofh.core.common.network.data.server;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

// CustomPacketPayload#type() replaced id() in 1.20.5, so a record component may not be
// called "type" any more.
public record StorageClearPayload(BlockPos pos, int storageType, int index) implements CustomPacketPayload {

    public static final Type<StorageClearPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "storage_clear_packet"));

    public static final StreamCodec<io.netty.buffer.ByteBuf, StorageClearPayload> STREAM_CODEC = StreamCodec.composite(
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
