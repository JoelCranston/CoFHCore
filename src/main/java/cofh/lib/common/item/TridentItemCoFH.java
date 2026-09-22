package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TridentItem;

public class TridentItemCoFH extends TridentItem implements ICoFHItem {

    public TridentItemCoFH(Properties builder) {

        super(builder);
    }

    public TridentItemCoFH(ToolMaterial material, Properties builder) {

        super(builder.enchantable(material.enchantmentValue()));
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public ICoFHItem setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
