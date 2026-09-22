package cofh.core.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import static cofh.core.util.references.CoreIDs.ID_HOLDING;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public class CoreEnchantments {

    private CoreEnchantments() {

    }

    public static void register() {

    }

    public static final ResourceKey<Enchantment> HOLDING = ResourceKey.create(Registries.ENCHANTMENT,
            Identifier.fromNamespaceAndPath(ID_COFH_CORE, ID_HOLDING));

}
