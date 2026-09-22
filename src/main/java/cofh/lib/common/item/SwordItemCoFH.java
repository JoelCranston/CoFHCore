package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class SwordItemCoFH extends Item implements ICoFHItem {

    public SwordItemCoFH(ToolMaterial material, int attackDamageIn, float attackSpeedIn, Properties builder) {

        super(builder.sword(material, attackDamageIn, attackSpeedIn));
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public SwordItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
