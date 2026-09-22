package cofh.lib.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

public class DyeableHorseArmorItemCoFH extends HorseArmorItemCoFH {

    public DyeableHorseArmorItemCoFH(Holder<ArmorMaterial> material, Properties builder) {

        super(material, true, builder);
    }

}
