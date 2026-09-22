package cofh.core.common.block.entity;

import cofh.core.util.control.ISecurableTile;
import cofh.core.util.control.SecurityControlModule;
import cofh.core.util.helpers.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SecurableBlockEntity extends BlockEntityCoFH implements ISecurableTile {

    protected SecurityControlModule securityControl = new SecurityControlModule(this);

    public SecurableBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {

        super(tileEntityTypeIn, pos, state);
    }

    @Override
    public ItemStack createItemStackTag(ItemStack stack) {

        CompoundTag nbt = ItemHelper.getBlockEntityData(stack);
        if (hasSecurity()) {
            securityControl().write(nbt);
        }
        ItemHelper.setBlockEntityData(stack, nbt);
        return super.createItemStackTag(stack);
    }

    // region NBT
    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

        super.loadAdditional(nbt, registries);

        securityControl.read(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {

        super.saveAdditional(nbt, registries);

        securityControl.write(nbt);
    }
    // endregion

    // region NETWORK

    // CONTROL
    @Override
    public FriendlyByteBuf getControlPacket(FriendlyByteBuf buffer) {

        super.getControlPacket(buffer);

        securityControl.writeToBuffer(buffer);

        return buffer;
    }

    @Override
    public void handleControlPacket(FriendlyByteBuf buffer) {

        super.handleControlPacket(buffer);

        securityControl.readFromBuffer(buffer);
    }
    // endregion

    // region MODULES
    @Override
    public SecurityControlModule securityControl() {

        return securityControl;
    }
    // endregion
}
