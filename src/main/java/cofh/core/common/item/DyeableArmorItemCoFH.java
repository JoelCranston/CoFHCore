package cofh.core.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

/**
 * DyeableLeatherItem was removed upstream - dyeability is now handled via the DYED_COLOR data
 * component directly rather than a marker interface, so this is just an ArmorItemCoFH now.
 * (Callers set Item.Properties#component(DataComponents.DYED_COLOR, ...) to opt in, matching
 * how vanilla leather armor works post-1.20.5.)
 */
public class DyeableArmorItemCoFH extends ArmorItemCoFH {

    public DyeableArmorItemCoFH(Holder<ArmorMaterial> pMaterial, ArmorItem.Type pType, Item.Properties pProperties) {

        super(pMaterial, pType, pProperties);
    }

}
