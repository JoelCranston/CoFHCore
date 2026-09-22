package cofh.core.client;

import cofh.core.client.settings.KeyBindingModeChange;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

public class CoreKeys {

    private CoreKeys() {

    }

    /**
     * 26.1.2: a key mapping's category is a KeyMapping.Category record over an Identifier, not a free
     * string. Build it directly rather than through the deprecated Category.register(Identifier) - that
     * one throws on a duplicate id - and hand it to RegisterKeyMappingsEvent#registerCategory, which is
     * what puts it in the sort order. Its label comes from id.toLanguageKey("key.category"), so this one
     * reads key.category.cofh_core.cofh.
     */
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(ID_COFH_CORE, "cofh"));

    public static final KeyMapping MULTIMODE_INCREMENT = new KeyBindingModeChange.Increment("key.cofh.mode_change_increment", 86, CATEGORY);
    public static final KeyMapping MULTIMODE_DECREMENT = new KeyBindingModeChange.Decrement("key.cofh.mode_change_decrement", 66, CATEGORY);

}
