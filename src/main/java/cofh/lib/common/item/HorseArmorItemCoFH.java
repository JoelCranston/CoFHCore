package cofh.lib.common.item;

import net.minecraft.world.item.Item;
import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

public class HorseArmorItemCoFH extends Item implements ICoFHItem {

    public HorseArmorItemCoFH(ArmorMaterial material, Properties builder) {

        super(builder.horseArmor(material));
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public HorseArmorItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
