package cofh.lib.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

/**
 * See HorseArmorItemCoFH - dyeability is now a constructor flag on the merged AnimalArmorItem
 * rather than a distinct item class (DyeableLeatherItem, the old marker interface, was removed
 * upstream entirely - dyeability is driven by the DYED_COLOR data component now).
 */
public class DyeableHorseArmorItemCoFH extends HorseArmorItemCoFH {

    public DyeableHorseArmorItemCoFH(Holder<ArmorMaterial> material, Properties builder) {

        super(material, true, builder);
    }

}
