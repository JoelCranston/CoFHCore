package cofh.lib.common.enchantment;

import net.minecraft.world.item.ItemStack;

public abstract class EnchantmentOverride extends EnchantmentCoFH {

    protected EnchantmentOverride(EnchantmentDefinition definition) {

        super(definition);
    }

    @Override
    public boolean isEnabled() {

        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {

        return stack.canApplyAtEnchantingTable(this);
    }

    @Override
    public boolean isAllowedOnBooks() {

        return allowOnBooks;
    }

    @Override
    public boolean isDiscoverable() {

        return allowGenerateInLoot;
    }

    @Override
    public boolean isTradeable() {

        return allowVillagerTrade;
    }

}
