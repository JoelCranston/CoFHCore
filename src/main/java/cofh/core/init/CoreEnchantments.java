package cofh.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import static cofh.core.util.references.CoreIDs.ID_HOLDING;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

/**
 * 1.21: enchantments are datapack objects, not registry objects built in code - there is no
 * Enchantment subclass any more and nothing to register on the mod bus. Holding is defined by
 * {@code data/cofh_core/enchantment/holding.json}; what it can be applied to is the item tag
 * {@code cofh_core:enchantable/holding} (mods add their storage items to that tag rather than
 * calling an "addValidItem" method, which cannot work for data-driven supported_items).
 * <p>
 * Code refers to the enchantment through {@link #HOLDING}; {@code Utils}' enchantment helpers
 * take the key and read levels off the stack's ENCHANTMENTS component.
 */
public class CoreEnchantments {

    private CoreEnchantments() {

    }

    public static void register() {

    }

    public static final ResourceKey<Enchantment> HOLDING = ResourceKey.create(Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(ID_COFH_CORE, ID_HOLDING));

}
