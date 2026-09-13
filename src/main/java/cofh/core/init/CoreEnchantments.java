package cofh.core.init;

import cofh.core.common.enchantment.HoldingEnchantment;
import cofh.lib.common.enchantment.EnchantmentCoFH;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;

import static cofh.core.CoFHCore.ENCHANTMENTS;
import static cofh.core.util.references.CoreIDs.ID_HOLDING;

public class CoreEnchantments {

    private CoreEnchantments() {

    }

    public static void register() {

    }

    public static final DeferredHolder<Enchantment, EnchantmentCoFH> HOLDING = ENCHANTMENTS.register(ID_HOLDING, HoldingEnchantment::new);

}
