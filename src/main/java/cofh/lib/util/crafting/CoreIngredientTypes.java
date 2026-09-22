package cofh.lib.util.crafting;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;

/**
 * Custom ingredient types have to be registered since 1.20.5 so they can be serialized; the
 * matching JSON key is {@code "neoforge:ingredient_type": "cofh_core:with_count"}.
 */
public class CoreIngredientTypes {

    private CoreIngredientTypes() {

    }

    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, ID_COFH_CORE);

    public static final DeferredHolder<IngredientType<?>, IngredientType<IngredientWithCount>> WITH_COUNT =
            INGREDIENT_TYPES.register("with_count", () -> new IngredientType<>(IngredientWithCount.CODEC, IngredientWithCount.STREAM_CODEC));

}
