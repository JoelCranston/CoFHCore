package cofh.lib.util.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * This class really just serves as a way to ride on Mojang's automated recipe syncing and datapack functionality.
 * It's part of a shim layer, nothing more.
 */
public abstract class SerializableRecipe implements Recipe<RecipeInput> {

    // region IRecipe
    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {

        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput inv) {

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {

        return true;
    }

    @Override
    public boolean showNotification() {

        return false;
    }

    @Override
    public String group() {

        return "";
    }

    @Override
    public PlacementInfo placementInfo() {

        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {

        // Never shown; display() is empty.
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public abstract RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer();

    @Override
    public abstract RecipeType<? extends Recipe<RecipeInput>> getType();
    // endregion
}
