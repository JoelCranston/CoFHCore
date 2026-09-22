package cofh.core.util.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import static cofh.core.init.CoreRecipeSerializers.SHAPED_POTION_RECIPE_SERIALIZER;

/**
 * A shaped recipe that copies the potion contents of one input onto its result.
 * <p>
 * 1.21: NeoForge's {@code IShapedRecipe} is gone (getWidth/getHeight live on the vanilla
 * {@link ShapedRecipe}), recipes match against a {@link CraftingInput} rather than the menu's
 * container, and a serializer supplies a {@link MapCodec} plus a {@link StreamCodec} instead of
 * {@code codec()}/{@code fromNetwork}/{@code toNetwork}.
 */
public class ShapedPotionNBTRecipe implements CraftingRecipe {

    private final ShapedRecipe wrappedRecipe;

    public ShapedPotionNBTRecipe(String pGroup, CraftingBookCategory pCategory, ShapedRecipePattern pattern, ItemStack pResult) {

        wrappedRecipe = new ShapedRecipe(pGroup, pCategory, pattern, pResult);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {

        boolean potionItem = false;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.POTION && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects()) {
                potionItem = true;
                break;
            }
        }
        return potionItem && wrappedRecipe.matches(inv, worldIn);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registries) {

        ItemStack result = wrappedRecipe.getResultItem(registries).copy();

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.POTION && stack.has(DataComponents.POTION_CONTENTS)) {
                result.set(DataComponents.POTION_CONTENTS, stack.get(DataComponents.POTION_CONTENTS));
                break;
            }
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {

        return wrappedRecipe.canCraftInDimensions(width, height);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {

        return wrappedRecipe.getResultItem(registries);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {

        return wrappedRecipe.getIngredients();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {

        return SHAPED_POTION_RECIPE_SERIALIZER.get();
    }

    public int getWidth() {

        return wrappedRecipe.getWidth();
    }

    public int getHeight() {

        return wrappedRecipe.getHeight();
    }

    @Override
    public CraftingBookCategory category() {

        return wrappedRecipe.category();
    }

    // region SERIALIZER
    public static class Serializer implements RecipeSerializer<ShapedPotionNBTRecipe> {

        public static final MapCodec<ShapedPotionNBTRecipe> CODEC = RecordCodecBuilder.mapCodec(
                codec -> codec.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.wrappedRecipe.getGroup()),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.wrappedRecipe.category()),
                                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.wrappedRecipe.pattern),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.wrappedRecipe.result)
                        )
                        .apply(codec, ShapedPotionNBTRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedPotionNBTRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, recipe -> recipe.wrappedRecipe.getGroup(),
                CraftingBookCategory.STREAM_CODEC, recipe -> recipe.wrappedRecipe.category(),
                ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.wrappedRecipe.pattern,
                ItemStack.STREAM_CODEC, recipe -> recipe.wrappedRecipe.result,
                ShapedPotionNBTRecipe::new);

        @Override
        public MapCodec<ShapedPotionNBTRecipe> codec() {

            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedPotionNBTRecipe> streamCodec() {

            return STREAM_CODEC;
        }

    }
    // endregion

}
