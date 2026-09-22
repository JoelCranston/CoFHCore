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

public class ShapedPotionNBTRecipe implements CraftingRecipe {

    private final ShapedRecipe wrappedRecipe;

    public ShapedPotionNBTRecipe(String pGroup, CraftingBookCategory pCategory, ShapedRecipePattern pattern, ItemStack pResult) {

        wrappedRecipe = new ShapedRecipe(pGroup, pCategory, pattern, pResult);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {

        // boolean flag
        boolean potionItem = false;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() == Items.POTION) {
                if (stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).hasEffects()) {
                    potionItem = true;
                    break;
                }
            }
        }
        return potionItem && wrappedRecipe.matches(inv, worldIn);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {

        ItemStack result = wrappedRecipe.getResultItem(registryAccess).copy();

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
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {

        return wrappedRecipe.getResultItem(registryAccess);
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
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_311730_ -> p_311730_.wrappedRecipe.result)
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

    //    public static class Serializer implements RecipeSerializer<ShapedPotionNBTRecipe> {
    //
    //        public ShapedPotionNBTRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
    //
    //            String s = GsonHelper.getAsString(json, "group", "");
    //            CraftingBookCategory craftingbookcategory = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", (String) null), CraftingBookCategory.MISC);
    //            Map<String, Ingredient> map = ShapedRecipeInternal.keyFromJson(GsonHelper.getAsJsonObject(json, "key"));
    //            String[] astring = ShapedRecipeInternal.shrink(ShapedRecipeInternal.patternFromJson(GsonHelper.getAsJsonArray(json, "pattern")));
    //            int i = astring[0].length();
    //            int j = astring.length;
    //            NonNullList<Ingredient> nonnulllist = ShapedRecipeInternal.dissolvePattern(astring, map, i, j);
    //            ItemStack itemstack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
    //            return new ShapedPotionNBTRecipe(recipeId, s, craftingbookcategory, i, j, nonnulllist, itemstack);
    //        }
    //
    //        @Override
    //        public ShapedPotionNBTRecipe fromNetwork(FriendlyByteBuf buffer) {
    //
    //            int i = buffer.readVarInt();
    //            int j = buffer.readVarInt();
    //            String s = buffer.readUtf(32767);
    //            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
    //            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i * j, Ingredient.EMPTY);
    //
    //            for (int k = 0; k < nonnulllist.size(); ++k) {
    //                nonnulllist.set(k, Ingredient.fromNetwork(buffer));
    //            }
    //            ItemStack itemstack = buffer.readItem();
    //            return new ShapedPotionNBTRecipe(recipeId, s, craftingbookcategory, i, j, nonnulllist, itemstack);
    //        }
    //
    //        @Override
    //        public void toNetwork(FriendlyByteBuf buffer, ShapedPotionNBTRecipe recipe) {
    //
    //            buffer.writeVarInt(recipe.getRecipeWidth());
    //            buffer.writeVarInt(recipe.getRecipeHeight());
    //            buffer.writeUtf(recipe.getGroup());
    //
    //            for (Ingredient ingredient : recipe.getIngredients()) {
    //                ingredient.toNetwork(buffer);
    //            }
    //            buffer.writeItem(recipe.wrappedRecipe.result);
    //        }
    //
    //    }
    // endregion
}
