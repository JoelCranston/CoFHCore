package cofh.core.compat.jei;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.types.IRecipeType;

import java.util.List;

public class PotionNBTRecipeManagerPlugin implements IRecipeManagerPlugin {

    @Override
    public <V> List<IRecipeType<?>> getRecipeTypes(IFocus<V> focus) {

        return List.of();
    }

    @Override
    public <T, V> List<T> getRecipes(IRecipeType<T> recipeType, IFocus<V> focus) {

        return List.of();
    }

    @Override
    public <T> List<T> getRecipes(IRecipeType<T> recipeType) {

        return List.of();
    }

}
