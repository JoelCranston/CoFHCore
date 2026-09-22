package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class AxeItemCoFH extends AxeItem implements ICoFHItem {

    public AxeItemCoFH(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        super(material, attackDamageIn, attackSpeedIn, builder);
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public AxeItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
