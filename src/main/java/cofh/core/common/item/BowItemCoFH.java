package cofh.core.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class BowItemCoFH extends BowItem implements ICoFHItem {

    protected float accuracyModifier = 1.0F;
    protected float damageModifier = 1.0F;
    protected float velocityModifier = 1.0F;

    public BowItemCoFH(Properties builder) {

        super(builder);
    }

    public BowItemCoFH(ToolMaterial material, Properties builder) {

        super(builder.enchantable(material.enchantmentValue()));
        setParams(material);
    }

    public BowItemCoFH setParams(ToolMaterial material) {

        this.damageModifier = material.attackDamageBonus() / 4;
        this.velocityModifier = material.speed() / 20;
        return this;
    }

    public BowItemCoFH setParams(float accuracyModifier, float damageModifier, float velocityModifier) {

        this.accuracyModifier = accuracyModifier;
        this.damageModifier = damageModifier;
        this.velocityModifier = velocityModifier;
        return this;
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public BowItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
