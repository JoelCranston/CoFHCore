package cofh.lib.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.equipment.ArmorMaterial;

import java.util.EnumMap;
import java.util.Map;

public class ArmorMaterialCoFH {

    private ArmorMaterialCoFH() {

    }

    public static ArmorMaterial create(int durability, int[] defenseIn, int enchantabilityIn, Holder<SoundEvent> equipSoundIn, float toughnessIn, float knockbackResistanceIn, TagKey<Item> repairItems, ResourceKey<EquipmentAsset> assetId) {

        Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
        ArmorType[] types = ArmorType.values();
        for (int i = 0; i < types.length && i < defenseIn.length; ++i) {
            defense.put(types[i], defenseIn[i]);
        }
        return new ArmorMaterial(durability, defense, enchantabilityIn, equipSoundIn, toughnessIn, knockbackResistanceIn, repairItems, assetId);
    }

}
