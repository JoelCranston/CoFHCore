package cofh.core.common.config;

import cofh.core.init.CoreEnchantments;
import cofh.lib.util.Utils;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.function.Supplier;

import static cofh.lib.util.Constants.TRUE;

public class CoreEnchantConfig implements IBaseConfig {

    @Override
    public void apply(ModConfigSpec.Builder builder) {

        String treasure = "This sets whether or not the Enchantment is considered a 'treasure' enchantment.";

        builder.push("Enchantments");

        improvedFeatherFalling = builder
                .comment("If TRUE, Feather Falling will prevent Farmland from being trampled. This option will work with alternative versions (overrides) of Feather Falling.")
                .define("Improved Feather Falling", improvedFeatherFalling);

        improvedMending = builder
                .comment("If TRUE, Mending behavior is altered so that Experience Orbs always repair items if possible, and the most damaged item is prioritized. This option may not work with alternative versions (overrides) of Mending.")
                .define("Improved Mending", improvedMending);

        builder.push("Holding");
        enableHolding = builder
                .comment("If TRUE, the Holding Enchantment is available for various Storage Items and Blocks.")
                .define("Enable", true);
        treasureHolding = builder
                .comment(treasure)
                .define("Treasure", false);
        builder.pop();

        builder.pop();
    }

    @Override
    public void refresh() {

        // 1.21: an enchantment is datapack-defined, so nothing about it can be set from code.
        // "Enable" is honoured centrally instead - Utils' level lookups report 0 for a disabled
        // enchantment, which is what the flag did. "Treasure" is the minecraft:treasure
        // enchantment tag now and is a datapack decision; the option is left in place but only a
        // data pack can act on it.
        Utils.setEnchantmentEnabled(CoreEnchantments.HOLDING, enableHolding.get());
    }

    public static boolean improvedFeatherFalling() {

        return improvedFeatherFalling.get();
    }

    public static boolean improvedMending() {

        return improvedMending.get();
    }

    private static Supplier<Boolean> improvedFeatherFalling = TRUE;
    private static Supplier<Boolean> improvedMending = TRUE;

    private Supplier<Boolean> enableHolding;
    private Supplier<Boolean> treasureHolding;

}
