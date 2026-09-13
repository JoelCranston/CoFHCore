package cofh.lib.common.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * ArmorMaterial became a final record upstream (no longer an interface CoFH can implement), and
 * durability moved from the material onto the item's own DataComponents.MAX_DAMAGE. This is now a
 * factory for the record instead of a base class to extend.
 */
public class ArmorMaterialCoFH {

    private ArmorMaterialCoFH() {

    }

    public static ArmorMaterial create(int[] damageReductionAmountsIn, int enchantabilityIn, Holder<SoundEvent> equipSoundIn,
                                        float toughnessIn, float knockbackResistanceIn, Supplier<Ingredient> repairMaterialSupplier) {

        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        ArmorItem.Type[] types = ArmorItem.Type.values();
        for (int i = 0; i < types.length && i < damageReductionAmountsIn.length; ++i) {
            defense.put(types[i], damageReductionAmountsIn[i]);
        }
        return new ArmorMaterial(defense, enchantabilityIn, equipSoundIn, repairMaterialSupplier, List.of(), toughnessIn, knockbackResistanceIn);
    }

}
