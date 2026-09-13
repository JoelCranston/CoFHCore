package cofh.lib.common.enchantment;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class EnchantmentCoFH extends Enchantment {

    protected boolean enable = true;

    protected boolean allowGenerateInLoot = true;
    protected boolean allowOnBooks = true;
    protected boolean allowVillagerTrade = true;
    protected boolean treasureEnchantment = false;

    protected EnchantmentCoFH(EnchantmentDefinition definition) {

        super(definition);
    }

    public EnchantmentCoFH setEnable(boolean enable) {

        this.enable = enable;
        return this;
    }

    public EnchantmentCoFH setTreasureEnchantment(boolean treasureEnchantment) {

        this.treasureEnchantment = treasureEnchantment;
        return this;
    }

    public EnchantmentCoFH setAllowOnBooks(boolean allowOnBooks) {

        this.allowOnBooks = allowOnBooks;
        return this;
    }

    @Override
    public String getDescriptionId() {

        return isEnabled() ? this.getOrCreateDescriptionId() : "enchantment.cofh_core.disabled";
    }

    public boolean isEnabled() {

        return enable;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {

        return enable && super.canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean isAllowedOnBooks() {

        return enable && allowOnBooks;
    }

    @Override
    public boolean isDiscoverable() {

        return enable && allowGenerateInLoot;
    }

    @Override
    public boolean isTradeable() {

        return enable && allowVillagerTrade;
    }

    @Override
    public boolean isTreasureOnly() {

        return treasureEnchantment;
    }

}
