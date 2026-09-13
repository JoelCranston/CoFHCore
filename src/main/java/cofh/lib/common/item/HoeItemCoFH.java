package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;

public class HoeItemCoFH extends HoeItem implements ICoFHItem {

    public HoeItemCoFH(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {

        super(tier, builder.attributes(DiggerItem.createAttributes(tier, attackDamageIn, attackSpeedIn)));
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public HoeItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
    }
    // endregion
}
