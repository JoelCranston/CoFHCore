package cofh.core.common.network.data.server;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public record GhostItemPayload(int slotNumber, ItemStack stack, int count) implements CustomPacketPayload {

    public static final Type<GhostItemPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, "ghost_item_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostItemPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, GhostItemPayload::slotNumber,
            ItemStack.OPTIONAL_STREAM_CODEC, GhostItemPayload::stack,
            ByteBufCodecs.INT, GhostItemPayload::count,
            GhostItemPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {

        return TYPE;
    }

}
