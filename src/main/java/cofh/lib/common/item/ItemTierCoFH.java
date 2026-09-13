package cofh.lib.common.item;

import net.minecraft.tags.TagKey;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class ItemTierCoFH implements Tier {

    // level/getLevel() is no longer part of Tier upstream - mining eligibility is purely
    // tag-based now (getIncorrectBlocksForDrops()). Kept as a plain (non-override) field/getter
    // since FishingRodItemCoFH still derives a luck modifier from it, but callers now must supply
    // the incorrect-blocks tag directly rather than a numeric level.
    private final int level;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private final TagKey<Block> incorrectBlocksForDrops;
    private final LazyLoadedValue<Ingredient> repairIngredient;

    public ItemTierCoFH(int level, int uses, float speed, float damage, int enchantmentValue, TagKey<Block> incorrectBlocksForDrops, Supplier<Ingredient> repairIngredient) {

        this.level = level;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.incorrectBlocksForDrops = incorrectBlocksForDrops;
        this.repairIngredient = new LazyLoadedValue<>(repairIngredient);
    }

    // region IItemTier
    @Override
    public int getUses() {

        return this.uses;
    }

    @Override
    public float getSpeed() {

        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {

        return this.damage;
    }

    public int getLevel() {

        return this.level;
    }

    @Override
    public int getEnchantmentValue() {

        return this.enchantmentValue;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {

        return this.incorrectBlocksForDrops;
    }

    @Override
    public Ingredient getRepairIngredient() {

        return this.repairIngredient.get();
    }
    // endregion
}
