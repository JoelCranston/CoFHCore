package cofh.lib.util.recipes;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;

public class SerializableRecipeType<T extends SerializableRecipe> implements RecipeType<T> {

    private final Identifier registryName;

    public SerializableRecipeType(Identifier location) {

        this.registryName = location;
    }

    public SerializableRecipeType(String modId, String name) {

        this.registryName = Identifier.fromNamespaceAndPath(modId, name);
    }

    @Override
    public String toString() {

        return registryName.toString();
    }

}
