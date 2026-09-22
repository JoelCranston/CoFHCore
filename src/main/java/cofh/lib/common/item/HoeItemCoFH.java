package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class HoeItemCoFH extends HoeItem implements ICoFHItem {

    public HoeItemCoFH(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        super(material, attackDamageIn, attackSpeedIn, builder);
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public HoeItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
