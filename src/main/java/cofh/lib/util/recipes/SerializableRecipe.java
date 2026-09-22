package cofh.lib.util.recipes;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
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
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider pRegistryAccess) {

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {

        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistryAccess) {

        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {

        return true;
    }

    @Override
    public abstract RecipeSerializer<?> getSerializer();

    @Override
    public abstract RecipeType<?> getType();
    // endregion
}
