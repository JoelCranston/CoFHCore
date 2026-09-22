package cofh.core.util.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

import java.util.List;

import static cofh.core.init.CoreRecipeSerializers.SHAPED_POTION_RECIPE_SERIALIZER;

public class ShapedPotionNBTRecipe implements CraftingRecipe {

    public static final MapCodec<ShapedPotionNBTRecipe> CODEC = RecordCodecBuilder.mapCodec(
            codec -> codec.group(
                            Recipe.CommonInfo.MAP_CODEC.forGetter(ShapedPotionNBTRecipe::commonInfo),
                            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(ShapedPotionNBTRecipe::bookInfo),
                            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.wrappedRecipe.pattern),
                            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.wrappedRecipe.result)
                    )
                    .apply(codec, ShapedPotionNBTRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedPotionNBTRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, ShapedPotionNBTRecipe::commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, ShapedPotionNBTRecipe::bookInfo,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.wrappedRecipe.pattern,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.wrappedRecipe.result,
            ShapedPotionNBTRecipe::new);

    private final ShapedRecipe wrappedRecipe;

    public ShapedPotionNBTRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ShapedRecipePattern pattern, ItemStackTemplate pResult) {

        wrappedRecipe = new ShapedRecipe(commonInfo, bookInfo, pattern, pResult);
    }

    private Recipe.CommonInfo commonInfo() {

        return new Recipe.CommonInfo(wrappedRecipe.showNotification());
    }

    private CraftingRecipe.CraftingBookInfo bookInfo() {

        return new CraftingRecipe.CraftingBookInfo(wrappedRecipe.category(), wrappedRecipe.group());
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
    public ItemStack assemble(CraftingInput inv) {

        ItemStack result = wrappedRecipe.assemble(inv);

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
    public boolean showNotification() {

        return wrappedRecipe.showNotification();
    }

    @Override
    public String group() {

        return wrappedRecipe.group();
    }

    @Override
    public PlacementInfo placementInfo() {

        return wrappedRecipe.placementInfo();
    }

    @Override
    public List<RecipeDisplay> display() {

        return wrappedRecipe.display();
    }

    @Override
    public RecipeSerializer<ShapedPotionNBTRecipe> getSerializer() {

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
