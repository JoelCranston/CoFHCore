package cofh.lib.util.crafting;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * An ingredient which matches nothing; stands in for the removed Ingredient.EMPTY.
 */
public class EmptyIngredient implements ICustomIngredient {

    public static final EmptyIngredient INSTANCE = new EmptyIngredient();
    public static final Ingredient EMPTY = INSTANCE.toVanilla();

    public static final MapCodec<EmptyIngredient> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private EmptyIngredient() {

    }

    @Override
    public boolean test(ItemStack stack) {

        return false;
    }

    @Override
    public Stream<Holder<Item>> items() {

        return Stream.empty();
    }

    @Override
    public boolean isSimple() {

        return true;
    }

    @Override
    public IngredientType<?> getType() {

        return CoreIngredientTypes.EMPTY.get();
    }

    @Override
    public SlotDisplay display() {

        return SlotDisplay.Empty.INSTANCE;
    }

}
