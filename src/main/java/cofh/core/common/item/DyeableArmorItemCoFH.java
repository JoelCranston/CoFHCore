package cofh.core.common.item;

import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;

// Dyeable through the minecraft:dyeable item tag.
public class DyeableArmorItemCoFH extends ArmorItemCoFH {

    public DyeableArmorItemCoFH(ArmorMaterial pMaterial, ArmorType pType, Item.Properties pProperties) {

        super(pMaterial, pType, pProperties);
    }

}
