package cofh.lib.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.conditions.ICondition;

public record TagExistsCondition(TagKey<Item> tag) implements ICondition {

    // 1.20.5: a condition registers its MapCodec, not a Codec (dispatch codecs need map codecs).
    public static final MapCodec<TagExistsCondition> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Identifier.CODEC.xmap(loc -> TagKey.create(Registries.ITEM, loc), TagKey::location).fieldOf("tag").forGetter(TagExistsCondition::tag))
                    .apply(builder, TagExistsCondition::new));

    public TagExistsCondition(String location) {

        this(Identifier.parse(location));
    }

    public TagExistsCondition(String namespace, String path) {

        this(Identifier.fromNamespaceAndPath(namespace, path));
    }

    public TagExistsCondition(Identifier tag) {

        this(TagKey.create(Registries.ITEM, tag));
    }

    @Override
    public boolean test(IContext context) {

        return !context.getTag(tag).isEmpty();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {

        return CODEC;
    }

    @Override
    public String toString() {

        return "tag_exists(\"" + tag.location() + "\")";
    }

}
