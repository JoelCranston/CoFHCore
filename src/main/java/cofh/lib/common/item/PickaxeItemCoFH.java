package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;

public class PickaxeItemCoFH extends PickaxeItem implements ICoFHItem {

    // PickaxeItem's ctor dropped the (int, float) attack damage/speed params - combat stats moved
    // to the ItemAttributeModifiers data component, built via DiggerItem's shared static helper.
    public PickaxeItemCoFH(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {

        super(tier, builder.attributes(DiggerItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public PickaxeItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
    }
    // endregion
}
