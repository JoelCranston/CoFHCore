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
/**
 * 1.21: a Recipe's generic is a {@link net.minecraft.world.item.crafting.RecipeInput}, not a
 * Container, and persistence takes a HolderLookup.Provider. This shim never actually crafts -
 * it only rides Mojang's recipe syncing and datapack loading - so the input type is the
 * do-nothing {@link RecipeInput} form.
 */
public abstract class SerializableRecipe implements Recipe<RecipeInput> {

    // region IRecipe
    @Override
    public boolean matches(RecipeInput inv, Level worldIn) {

        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput inv, HolderLookup.Provider registries) {

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {

        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {

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
