package cofh.core.common.network.packet.client;

import cofh.core.common.network.data.client.ModelUpdatePayload;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ModelUpdatePacket {

    public static final ModelUpdatePacket INSTANCE = new ModelUpdatePacket();

    public static ModelUpdatePacket get() {

        return INSTANCE;
    }

    public void handle(final ModelUpdatePayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            Level level = ProxyUtils.getClientWorld();
            if (level == null) {
                return;
            }
            BlockPos pos = payload.pos();
            BlockState state = level.getBlockState(pos);
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile != null) {
                tile.requestModelDataUpdate();
            }
            level.sendBlockUpdated(pos, state, state, 3);
        });
    }

    public static void sendToClient(Level level, BlockPos pos) {

        Utils.sendNear(level, pos, new ModelUpdatePayload(pos));
    }

}
