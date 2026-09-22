package cofh.core.client;

import cofh.core.client.settings.KeyBindingModeChange;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public class CoreKeys {

    private CoreKeys() {

    }

    // Not Category.register, which throws on a duplicate id.
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "cofh"));

    public static final KeyMapping MULTIMODE_INCREMENT = new KeyBindingModeChange.Increment("key.cofh.mode_change_increment", 86, CATEGORY);
    public static final KeyMapping MULTIMODE_DECREMENT = new KeyBindingModeChange.Decrement("key.cofh.mode_change_decrement", 66, CATEGORY);

}
