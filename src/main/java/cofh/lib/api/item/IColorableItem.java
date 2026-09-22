package cofh.lib.api.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public interface IColorableItem {

    default int getColor(ItemStack item, int colorIndex) {

        if (colorIndex == 0) {
            // The display/color sub-tag is the DYED_COLOR component since 1.20.5.
            DyedItemColor dyed = item.get(DataComponents.DYED_COLOR);
            return dyed != null ? dyed.rgb() : 0xFFFFFF;
        }
        return 0xFFFFFF;
    }

}
