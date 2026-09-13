package cofh.lib.common.enchantment;

import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;

public abstract class DamageEnchantmentCoFH extends EnchantmentCoFH {

    protected DamageEnchantmentCoFH(EnchantmentDefinition definition) {

        super(definition);
    }

    /**
     * Matches the old getMinCost(level) = 10 + (level - 1) * 8 formula, for subclasses building
     * their own {@link EnchantmentDefinition} at construction time.
     */
    protected static Enchantment.Cost minCost() {

        return Enchantment.Cost.dynamicCost(10, 8);
    }

    /**
     * Matches the old maxDelegate(level) = getMinCost(level) + 20 formula.
     */
    protected static Enchantment.Cost maxCost() {

        return Enchantment.Cost.dynamicCost(30, 8);
    }

    @Override
    public boolean checkCompatibility(Enchantment ench) {

        return super.checkCompatibility(ench) && !(ench instanceof DamageEnchantment) && !(ench instanceof DamageEnchantmentCoFH);
    }

    public static float getExtraDamage(int level) {

        return level * 2.5F;
    }

}
