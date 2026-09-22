package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class PickaxeItemCoFH extends Item implements ICoFHItem {

    public PickaxeItemCoFH(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        this(builder.pickaxe(material, attackDamageIn, attackSpeedIn));
    }

    protected PickaxeItemCoFH(Properties builder) {

        super(builder);
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public PickaxeItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
