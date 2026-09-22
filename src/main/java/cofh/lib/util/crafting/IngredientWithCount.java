package cofh.lib.util.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * An ingredient that also requires a minimum stack size.
 * <p>
 * 1.21: {@link Ingredient} is final and backed by a HolderSet, so this can no longer be a
 * subclass. NeoForge's replacement for "an ingredient with custom matching" is
 * {@link ICustomIngredient}, whose {@link ICustomIngredient#toVanilla()} produces the
 * {@code Ingredient} the rest of the recipe system wants. The type has to be registered (see
 * {@code CoreIngredientTypes}) so the ingredient can be written to disk and to the network.
 */
public class IngredientWithCount implements ICustomIngredient {

    public static final MapCodec<IngredientWithCount> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(IngredientWithCount::getIngredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(IngredientWithCount::getCount)
    ).apply(instance, IngredientWithCount::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IngredientWithCount> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, IngredientWithCount::getIngredient,
            ByteBufCodecs.VAR_INT, IngredientWithCount::getCount,
            IngredientWithCount::new);

    private final Ingredient ingredient;
    private final int count;

    public IngredientWithCount(Ingredient ingredient, int count) {

        this.ingredient = ingredient;
        this.count = count;
    }

    public Ingredient getIngredient() {

        return ingredient;
    }

    public int getCount() {

        return count;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {

        return stack != null && ingredient.test(stack) && stack.getCount() >= count;
    }

    @Override
    public Stream<ItemStack> getItems() {

        return Arrays.stream(ingredient.getItems()).map(stack -> stack.copyWithCount(count));
    }

    @Override
    public boolean isSimple() {

        // Stack size is not part of the HolderSet, so matching cannot be done by item id alone.
        return false;
    }

    @Override
    public IngredientType<?> getType() {

        return CoreIngredientTypes.WITH_COUNT.get();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof IngredientWithCount other)) return false;
        return count == other.count && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {

        return Objects.hash(ingredient, count);
    }

    @Override
    public String toString() {

        return count + "x " + ingredient;
    }

}
