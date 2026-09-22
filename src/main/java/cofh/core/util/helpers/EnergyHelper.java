package cofh.core.util.helpers;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;

public class EnergyHelper {

    private EnergyHelper() {

    }

    public static boolean hasEnergyHandlerCap(BlockEntity tile, Direction face) {

        return tile != null && tile.getLevel() != null && tile.getLevel().getCapability(Capabilities.Energy.BLOCK, tile.getBlockPos(), tile.getBlockState(), tile, face) != null;
    }

    public static IEnergyStorage getEnergyHandlerCap(BlockEntity tile, Direction face) {

        EnergyHandler handler = tile == null || tile.getLevel() == null ? null : tile.getLevel().getCapability(Capabilities.Energy.BLOCK, tile.getBlockPos(), tile.getBlockState(), tile, face);
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    public static boolean hasEnergyHandlerCap(ItemStack item) {

        return !item.isEmpty() && ItemAccess.forStack(item).getCapability(Capabilities.Energy.ITEM) != null;
    }

}
