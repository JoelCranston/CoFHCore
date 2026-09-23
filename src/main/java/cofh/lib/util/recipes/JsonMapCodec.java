package cofh.lib.util.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.*;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Simple map codec for {@link JsonObject}.
 * <p>
 * With thanks to TechReborn.
 */
public class JsonMapCodec extends MapCodec<JsonObject> {

    public static final MapCodec<JsonObject> INSTANCE = new JsonMapCodec();

    // Recipe codec over JSON; the registry ops stay available to RecipeJsonUtils while fromJson runs.
    public static <T> MapCodec<T> of(Function<JsonObject, T> fromJson, Function<T, JsonObject> toJson) {

        return new MapCodec<>() {

            @Override
            public <U> Stream<U> keys(DynamicOps<U> ops) {

                return INSTANCE.keys(ops);
            }

            @Override
            public <U> DataResult<T> decode(DynamicOps<U> ops, MapLike<U> input) {

                return INSTANCE.decode(ops, input).flatMap(json -> RecipeJsonUtils.withOps(ops, () -> {
                    try {
                        return DataResult.success(fromJson.apply(json));
                    } catch (JsonParseException e) {
                        return DataResult.error(e::getMessage);
                    }
                }));
            }

            @Override
            public <U> RecordBuilder<U> encode(T input, DynamicOps<U> ops, RecordBuilder<U> prefix) {

                JsonObject json = toJson.apply(input);
                return json == null ? prefix : INSTANCE.encode(json, ops, prefix);
            }
        };
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {

        throw new UnsupportedOperationException("Not implemented yet! Should not be necessary for regular JSON serialization.");
    }

    @Override
    public <T> DataResult<JsonObject> decode(DynamicOps<T> ops, MapLike<T> input) {
        // convert input to JsonElement
        JsonElement converted = ops.convertTo(JsonOps.INSTANCE, ops.createMap(input.entries()));

        // convert JsonElement to JsonObject
        if (converted.isJsonObject()) {
            return DataResult.success(converted.getAsJsonObject());
        } else {
            return DataResult.error(() -> "Not a json object: " + converted);
        }
    }

    @Override
    public <T> RecordBuilder<T> encode(JsonObject input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        // Convert each value to the correct type and add it to the prefix
        for (var entry : input.entrySet()) {
            // Convert each value
            var convertedValue = JsonOps.INSTANCE.convertTo(ops, entry.getValue());
            // Add to prefix
            prefix.add(entry.getKey(), convertedValue);
        }
        return prefix;
    }

}
