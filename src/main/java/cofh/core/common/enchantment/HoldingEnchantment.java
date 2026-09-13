package cofh.core.common.enchantment;

import cofh.lib.api.item.IContainerItem;
import cofh.lib.common.enchantment.EnchantmentCoFH;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.HashSet;
import java.util.Set;

public class HoldingEnchantment extends EnchantmentCoFH {

    /** Old getMaxLevel() was config-adjustable; maxLevel is now baked into the definition at
     * construction and can no longer be changed at runtime - see CoreEnchantConfig. */
    public static final int MAX_LEVEL = 4;

    private static final Set<Item> VALID_ITEMS = new HashSet<>();

    public static boolean addValidItem(Item container) {

        return VALID_ITEMS.add(container);
    }

    public HoldingEnchantment() {

        super(Enchantment.definition(
                ItemTags.VANISHING_ENCHANTABLE,
                10, // weight, matches the old Rarity.COMMON
                MAX_LEVEL,
                Enchantment.Cost.dynamicCost(1, 5), // old getMinCost(level) = 1 + (level - 1) * 5
                Enchantment.Cost.dynamicCost(51, 5), // old maxDelegate(level) = getMinCost(level) + 50
                1,
                EquipmentSlot.values()
        ));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {

        Item item = stack.getItem();
        return enable && item instanceof IContainerItem || VALID_ITEMS.contains(item);
    }

}
