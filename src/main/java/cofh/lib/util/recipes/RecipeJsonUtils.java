package cofh.lib.util.recipes;

import cofh.lib.common.block.BlockIngredient;
import cofh.lib.common.fluid.FluidIngredient;
import cofh.lib.util.crafting.EmptyIngredient;
import cofh.lib.util.crafting.IngredientWithCount;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

import static cofh.lib.util.Constants.BASE_CHANCE_LOCKED;
import static cofh.lib.util.Constants.BUCKET_VOLUME;

public abstract class RecipeJsonUtils {

    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Ingredient codec that accepts the CoFH shapes (count, value, legacy item/tag objects).
    public static final Codec<Ingredient> INGREDIENT_CODEC = new Codec<>() {

        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {

            return withOps(ops, () -> DataResult.success(Pair.of(parseIngredient(ops.convertTo(JsonOps.INSTANCE, input)), ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {

            return Ingredient.CODEC.encode(input, ops, prefix);
        }
    };

    private static final ThreadLocal<DynamicOps<?>> OPS = new ThreadLocal<>();
    private static RegistryOps<JsonElement> staticOps;

    private RecipeJsonUtils() {

    }

    // Runs a parse with the caller's ops, so registry-aware codecs (ingredients) can resolve holders and tags.
    public static <R> R withOps(DynamicOps<?> ops, Supplier<R> parse) {

        DynamicOps<?> previous = OPS.get();
        OPS.set(ops);
        try {
            return parse.get();
        } finally {
            if (previous == null) {
                OPS.remove();
            } else {
                OPS.set(previous);
            }
        }
    }

    public static DynamicOps<JsonElement> jsonOps() {

        if (OPS.get() instanceof RegistryOps<?> registryOps) {
            return registryOps.withParent(JsonOps.INSTANCE);
        }
        if (staticOps == null) {
            staticOps = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        }
        return staticOps;
    }

    // region HELPERS
    private static Ingredient ingredientFromJson(JsonElement element) {

        return Ingredient.CODEC.parse(jsonOps(), legacyIngredient(element)).getOrThrow(JsonParseException::new);
    }

    // Pre-1.21.2 datapacks wrote {"item": id} and {"tag": id}; vanilla now wants "id" and "#id".
    private static JsonElement legacyIngredient(JsonElement element) {

        if (element.isJsonArray()) {
            JsonArray array = new JsonArray();
            boolean tags = false;
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                JsonElement converted = legacyIngredient(arrayElement);
                tags |= !converted.isJsonPrimitive() || converted.getAsString().startsWith("#");
                array.add(converted);
            }
            if (!tags) {
                return array;
            }
            // A union with tags is a compound ingredient now; a plain list only holds item ids.
            JsonObject compound = new JsonObject();
            compound.addProperty("neoforge:ingredient_type", "neoforge:compound");
            compound.add("children", array);
            return compound;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            if (object.has(ITEM) && object.get(ITEM).isJsonPrimitive()) {
                return object.get(ITEM);
            }
            if (object.has(TAG) && object.get(TAG).isJsonPrimitive()) {
                return new JsonPrimitive("#" + object.get(TAG).getAsString());
            }
        }
        return element;
    }

    public static Ingredient parseIngredient(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return EmptyIngredient.EMPTY;
        }
        Ingredient ingredient;

        if (element.isJsonArray()) {
            try {
                ingredient = ingredientFromJson(element);
            } catch (Throwable t) {
                ingredient = EmptyIngredient.EMPTY;
                LOG.debug("Invalid Ingredient - using EMPTY instead!", t);
            }
        } else {
            JsonElement subElement = element.getAsJsonObject();
            try {
                JsonObject object = subElement.getAsJsonObject();
                if (object.has(VALUE)) {
                    ingredient = ingredientFromJson(object.get(VALUE));
                } else {
                    ingredient = ingredientFromJson(subElement);
                }
                int count = 1;
                if (object.has(COUNT)) {
                    count = object.get(COUNT).getAsInt();
                } else if (object.has(AMOUNT)) {
                    count = object.get(AMOUNT).getAsInt();
                }
                if (count > 1) {
                    return new IngredientWithCount(ingredient, count).toVanilla();
                }
            } catch (Throwable t) {
                ingredient = EmptyIngredient.EMPTY;
                LOG.debug("Invalid Ingredient - using EMPTY instead!", t);
            }
        }
        return ingredient;
    }

    public static FluidIngredient parseFluidIngredient(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return FluidIngredient.of(FluidStack.EMPTY);
        }
        FluidIngredient ingredient;

        if (element.isJsonArray()) {
            try {
                ingredient = FluidIngredient.fromJson(element);
            } catch (Throwable t) {
                ingredient = FluidIngredient.of(FluidStack.EMPTY);
                LOG.debug("Invalid Ingredient - using EMPTY instead!", t);
            }
        } else {
            JsonElement subElement = element.getAsJsonObject();
            try {
                JsonObject object = subElement.getAsJsonObject();
                if (object.has(VALUE)) {
                    ingredient = FluidIngredient.fromJson(object.get(VALUE));
                } else {
                    ingredient = FluidIngredient.fromJson(subElement);
                }
                int amount = BUCKET_VOLUME;
                if (object.has(AMOUNT)) {
                    amount = object.get(AMOUNT).getAsInt();
                } else if (object.has(COUNT)) {
                    amount = object.get(COUNT).getAsInt();
                }
                if (amount > 0) {
                    ingredient.setAmount(amount);
                }
                if (object.has(NBT)) {
                    JsonElement nbtElement = object.get(NBT);
                    CompoundTag nbt;
                    if (nbtElement.isJsonObject()) {
                        nbt = TagParser.parseCompoundFully(GSON.toJson(nbtElement));
                    } else {
                        nbt = TagParser.parseCompoundFully(GsonHelper.convertToString(nbtElement, NBT));
                    }
                    ingredient.setTag(nbt);
                }
            } catch (Throwable t) {
                ingredient = FluidIngredient.of(FluidStack.EMPTY);
                LOG.debug("Invalid Ingredient - using EMPTY instead!", t);
            }
        }
        return ingredient;
    }

    public static void parseInputs(List<Ingredient> ingredients, List<FluidIngredient> fluids, JsonElement element) {

        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.isJsonArray()) {
                    ingredients.add(parseIngredient(arrayElement.getAsJsonArray()));
                } else if (arrayElement.isJsonObject()) {
                    if (arrayElement.getAsJsonObject().has(FLUID) || arrayElement.getAsJsonObject().has(FLUID_TAG)) {
                        fluids.add(parseFluidIngredient(arrayElement.getAsJsonObject()));
                    } else {
                        ingredients.add(parseIngredient(arrayElement.getAsJsonObject()));
                    }
                }
            }
        } else if (element.getAsJsonObject().has(FLUID) || element.getAsJsonObject().has(FLUID_TAG)) {
            fluids.add(parseFluidIngredient(element.getAsJsonObject()));
        } else {
            ingredients.add(parseIngredient(element.getAsJsonObject()));
        }
    }

    public static void parseOutputs(List<ItemStack> items, List<Float> chances, List<FluidStack> fluids, JsonElement element) {

        if (element == null) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.getAsJsonObject().has(FLUID)) {
                    fluids.add(parseFluidStack(arrayElement));
                } else {
                    ItemStack stack = parseItemStack(arrayElement);
                    if (!stack.isEmpty()) {
                        items.add(stack);
                        chances.add(parseItemChance(arrayElement));
                    }
                }
            }
        } else if (element.getAsJsonObject().has(FLUID)) {
            fluids.add(parseFluidStack(element));
        } else {
            ItemStack stack = parseItemStack(element);
            if (!stack.isEmpty()) {
                items.add(stack);
                chances.add(parseItemChance(element));
            }
        }
    }

    public static void parseOutputTemplates(List<ItemStackTemplate> items, List<Float> chances, List<FluidStackTemplate> fluids, JsonElement element) {

        if (element == null) {
            return;
        }
        if (element.isJsonArray()) {
            for (JsonElement arrayElement : element.getAsJsonArray()) {
                if (arrayElement.getAsJsonObject().has(FLUID)) {
                    FluidStackTemplate fluid = parseFluidStackTemplate(arrayElement);
                    if (fluid != null) {
                        fluids.add(fluid);
                    }
                } else {
                    ItemStackTemplate item = parseItemStackTemplate(arrayElement);
                    if (item != null) {
                        items.add(item);
                        chances.add(parseItemChance(arrayElement));
                    }
                }
            }
        } else if (element.getAsJsonObject().has(FLUID)) {
            FluidStackTemplate fluid = parseFluidStackTemplate(element);
            if (fluid != null) {
                fluids.add(fluid);
            }
        } else {
            ItemStackTemplate item = parseItemStackTemplate(element);
            if (item != null) {
                items.add(item);
                chances.add(parseItemChance(element));
            }
        }
    }

    @Nullable
    public static ItemStackTemplate parseItemStackTemplate(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return null;
        }
        Item item = null;
        int count = 1;
        DataComponentPatch components = DataComponentPatch.EMPTY;

        if (element.isJsonPrimitive()) {
            item = BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString()));
            return item == null || item == Items.AIR ? null : new ItemStackTemplate(item);
        } else {
            JsonObject itemObject = element.getAsJsonObject();

            /* COUNT */
            if (itemObject.has(COUNT)) {
                count = itemObject.get(COUNT).getAsInt();
            } else if (itemObject.has(AMOUNT)) {
                count = itemObject.get(AMOUNT).getAsInt();
            }

            /* ITEM */
            if (itemObject.has(ITEM)) {
                item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemObject.get(ITEM).getAsString()));
            }
            if (item == null || item == Items.AIR || count <= 0) {
                return null;
            }

            /* NBT */
            if (itemObject.has(NBT)) {
                JsonElement nbtElement = itemObject.get(NBT);
                CompoundTag nbt;
                try {
                    if (nbtElement.isJsonObject()) {
                        nbt = TagParser.parseCompoundFully(GSON.toJson(nbtElement));
                    } else {
                        nbt = TagParser.parseCompoundFully(GsonHelper.convertToString(nbtElement, NBT));
                    }
                    components = DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)).build();
                } catch (Exception e) {
                    LOG.debug("Invalid ItemStack - using EMPTY instead!", e);
                    return null;
                }
            }
        }
        return new ItemStackTemplate(item, count, components);
    }

    @Nullable
    public static FluidStackTemplate parseFluidStackTemplate(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return null;
        }
        Fluid fluid = null;
        int amount = BUCKET_VOLUME;
        DataComponentPatch components = DataComponentPatch.EMPTY;

        if (element.isJsonPrimitive()) {
            fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(element.getAsString()));
            return fluid == null || fluid == Fluids.EMPTY ? null : new FluidStackTemplate(fluid, amount);
        } else {
            JsonObject fluidObject = element.getAsJsonObject();

            /* AMOUNT */
            if (fluidObject.has(AMOUNT)) {
                amount = fluidObject.get(AMOUNT).getAsInt();
            } else if (fluidObject.has(COUNT)) {
                amount = fluidObject.get(COUNT).getAsInt();
            }

            /* FLUID */
            if (fluidObject.has(FLUID)) {
                fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidObject.get(FLUID).getAsString()));
            }
            if (fluid == null || fluid == Fluids.EMPTY || amount <= 0) {
                return null;
            }

            /* NBT */
            if (fluidObject.has(NBT)) {
                JsonElement nbtElement = fluidObject.get(NBT);
                CompoundTag nbt;
                try {
                    if (nbtElement.isJsonObject()) {
                        nbt = TagParser.parseCompoundFully(GSON.toJson(nbtElement));
                    } else {
                        nbt = TagParser.parseCompoundFully(GsonHelper.convertToString(nbtElement, NBT));
                    }
                    components = DataComponentPatch.builder().set(DataComponents.CUSTOM_DATA, CustomData.of(nbt)).build();
                } catch (Exception e) {
                    LOG.debug("Invalid FluidStack - using EMPTY instead!", e);
                    return null;
                }
            }
        }
        return new FluidStackTemplate(fluid, amount, components);
    }

    public static ItemStack parseItemStack(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack;
        Item item = null;
        int count = 1;

        if (element.isJsonPrimitive()) {
            item = BuiltInRegistries.ITEM.getValue(Identifier.parse(element.getAsString()));
            return item == null ? ItemStack.EMPTY : new ItemStack(item);
        } else {
            JsonObject itemObject = element.getAsJsonObject();

            /* COUNT */
            if (itemObject.has(COUNT)) {
                count = itemObject.get(COUNT).getAsInt();
            } else if (itemObject.has(AMOUNT)) {
                count = itemObject.get(AMOUNT).getAsInt();
            }

            /* ITEM */
            if (itemObject.has(ITEM)) {
                item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemObject.get(ITEM).getAsString()));
            }
            if (item == null) {
                return ItemStack.EMPTY;
            }
            stack = new ItemStack(item, count);

            /* NBT */
            if (itemObject.has(NBT)) {
                JsonElement nbtElement = itemObject.get(NBT);
                CompoundTag nbt;
                try {
                    if (nbtElement.isJsonObject()) {
                        nbt = TagParser.parseCompoundFully(GSON.toJson(nbtElement));
                    } else {
                        nbt = TagParser.parseCompoundFully(GsonHelper.convertToString(nbtElement, NBT));
                    }
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                } catch (Exception e) {
                    LOG.debug("Invalid ItemStack - using EMPTY instead!", e);
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    public static FluidStack parseFluidStack(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return FluidStack.EMPTY;
        }
        FluidStack stack;
        Fluid fluid = null;
        int amount = BUCKET_VOLUME;

        if (element.isJsonPrimitive()) {
            fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(element.getAsString()));
            return fluid == null ? FluidStack.EMPTY : new FluidStack(fluid, amount);
        } else {
            JsonObject fluidObject = element.getAsJsonObject();

            /* AMOUNT */
            if (fluidObject.has(AMOUNT)) {
                amount = fluidObject.get(AMOUNT).getAsInt();
            } else if (fluidObject.has(COUNT)) {
                amount = fluidObject.get(COUNT).getAsInt();
            }

            /* FLUID */
            if (fluidObject.has(FLUID)) {
                fluid = BuiltInRegistries.FLUID.getValue(Identifier.parse(fluidObject.get(FLUID).getAsString()));
            }
            if (fluid == null) {
                return FluidStack.EMPTY;
            }
            stack = new FluidStack(fluid, amount);

            /* NBT */
            if (fluidObject.has(NBT)) {
                JsonElement nbtElement = fluidObject.get(NBT);
                CompoundTag nbt;
                try {
                    if (nbtElement.isJsonObject()) {
                        nbt = TagParser.parseCompoundFully(GSON.toJson(nbtElement));
                    } else {
                        nbt = TagParser.parseCompoundFully(GsonHelper.convertToString(nbtElement, NBT));
                    }
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                } catch (Exception e) {
                    LOG.debug("Invalid FluidStack - using EMPTY instead!", e);
                    return FluidStack.EMPTY;
                }
            }
        }
        return stack;
    }

    private static float parseItemChance(JsonElement element) {

        JsonObject json = element.getAsJsonObject();

        if (json.has(CHANCE)) {
            float chance = json.get(CHANCE).getAsFloat();
            if (chance > 0.0F && json.has(LOCKED) && json.get(LOCKED).getAsBoolean()) {
                chance *= BASE_CHANCE_LOCKED;
            }
            return chance;
        }
        return BASE_CHANCE_LOCKED;
    }

    public static Block parseBlock(JsonElement element) {

        if (element == null || element.isJsonNull()) {
            return Blocks.AIR;
        }
        Block block;

        block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(element.getAsString()));
        return block == null ? Blocks.AIR : block;
    }

    public static BlockIngredient getAsBlockIngredient(JsonObject json, String member) {

        return convertToBlockIngredient(json.get(member), member);
    }

    public static BlockIngredient convertToBlockIngredient(JsonElement element, String member) {

        if (element == null || element.isJsonNull()) {
            return BlockIngredient.EMPTY;
        }
        if (element.isJsonArray()) {
            return BlockIngredient.fromJsonArray(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            return BlockIngredient.fromJsonObject(element.getAsJsonObject());
        }
        throw new JsonSyntaxException("Expected " + member + " to be an Object or an Array of Objects, was " + GsonHelper.getType(element));
    }
    // endregion

    // region STRING CONSTANTS
    public static final String ADJACENT = "adjacent";
    public static final String AMOUNT = "amount";
    public static final String AMPLIFIER = "amplifier";
    public static final String ATTRIBUTE = "attribute";
    public static final String BASE = "base";
    public static final String BELOW = "below";
    public static final String BLOCK = "block";
    public static final String CHANCE = "chance";
    public static final String COMMENT = "//";
    public static final String CONSTANT = "constant";
    public static final String COUNT = "count";
    public static final String CYCLES = "cycles";
    public static final String DURATION_MOD = "duration_mod";
    public static final String ENABLE = "enable";
    public static final String ENERGY = "energy";
    public static final String ENERGY_MOD = "energy_mod";
    public static final String ENTRY = "entry";
    public static final String EXPERIENCE = "experience";
    public static final String FLUID = "fluid";
    public static final String FLUID_TAG = "fluid_tag";
    public static final String HIVE = "hive";
    public static final String HONEY = "honey";
    public static final String HONEYCOMB = "honeycomb";
    public static final String INGREDIENT = "ingredient";
    public static final String INGREDIENTS = "ingredients";
    public static final String INPUT = "input";
    public static final String INPUT_FLUIDS = "input_fluids";
    public static final String INPUT_ITEMS = "input_items";
    public static final String INPUTS = "inputs";
    public static final String ITEM = "item";
    public static final String LAVA = "lava";
    public static final String LEAF = "leaf";
    public static final String LEAVES = "leaves";
    public static final String LOCKED = "locked";
    public static final String LOOT_TABLE = "loot_table";
    public static final String MIN_CHANCE = "min_chance";
    public static final String MOD_LOADED = "mod";
    public static final String MIN_HEIGHT = "min_height";
    public static final String MAX_HEIGHT = "max_height";
    public static final String MIN_LEAVES = "min_leaves";
    public static final String MAX_LEAVES = "max_leaves";
    public static final String NAME = "Name";
    public static final String NBT = "nbt";
    public static final String OPERATION = "operation";
    public static final String OUTPUT = "output";
    public static final String OUTPUT_FLUIDS = "output_fluids";
    public static final String OUTPUT_ITEMS = "output_items";
    public static final String OUTPUT_MOD = "output_mod";
    public static final String OUTPUTS = "outputs";
    public static final String PRIMARY_MOD = "primary_mod";
    public static final String PROPERTIES = "Properties";
    public static final String REMOVE = "remove";
    public static final String RESULT = "result";
    public static final String RESULTS = "results";
    public static final String SAPLING = "sapling";
    public static final String STATE = "state";
    public static final String SECONDARY_MOD = "secondary_mod";
    public static final String TAG = "tag";
    public static final String TICKS = "ticks";
    public static final String TIME = "time";
    public static final String TRUNK = "trunk";
    public static final String TYPE = "type";
    public static final String USE_CHANCE = "use_chance";
    public static final String VALUE = "value";
    public static final String WATER = "water";
    public static final String WATER_MOD = "water_mod";
    public static final String WILDCARD = "wildcard";
    public static final String XP = "xp";
    // endregion
}
