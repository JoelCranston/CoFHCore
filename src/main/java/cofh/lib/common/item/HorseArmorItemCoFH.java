package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * HorseArmorItem/DyeableHorseArmorItem were merged upstream into AnimalArmorItem (BodyType.CANINE
 * added for wolf armor); dyeability is now a constructor flag rather than a separate item class,
 * and protection/texture are both driven by the ArmorMaterial rather than passed directly.
 */
public class HorseArmorItemCoFH extends AnimalArmorItem implements ICoFHItem {

    protected int enchantability = 1;

    public HorseArmorItemCoFH(Holder<ArmorMaterial> material, boolean dyeable, Properties builder) {

        super(material, AnimalArmorItem.BodyType.EQUESTRIAN, dyeable, builder);
    }

    public HorseArmorItemCoFH setEnchantability(int enchantability) {

        this.enchantability = enchantability;
        return this;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {

        return enchantability > 0;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {

        return enchantability;
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public HorseArmorItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
    }
    // endregion
}
