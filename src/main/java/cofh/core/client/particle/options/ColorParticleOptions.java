package cofh.core.client.particle.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class ColorParticleOptions extends CoFHParticleOptions {

    public final int rgba0;

    public ColorParticleOptions(ParticleType<? extends ColorParticleOptions> type, float size, float duration, float delay, int rgba) {

        super(type, size, duration, delay);
        this.rgba0 = rgba;
    }

    public ColorParticleOptions(ParticleType<? extends ColorParticleOptions> type, float size, float duration, float delay) {

        this(type, size, duration, delay, 0xFFFFFFFF);
    }

    public ColorParticleOptions(ParticleType<? extends ColorParticleOptions> type, float size, float duration) {

        this(type, size, duration, 0.0F);
    }

    public ColorParticleOptions(ParticleType<? extends ColorParticleOptions> type) {

        this(type, 1.0F, 1.0F);
    }

    public static MapCodec<ColorParticleOptions> codec(ParticleType<ColorParticleOptions> type) {

        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("size").forGetter(options -> options.size),
                Codec.FLOAT.fieldOf("duration").forGetter(options -> options.duration),
                Codec.FLOAT.fieldOf("delay").forGetter(options -> options.delay),
                Codec.INT.fieldOf("rgba0").forGetter(options -> options.rgba0)
        ).apply(builder, (size, duration, delay, rgba) -> new ColorParticleOptions(type, size, duration, delay, rgba)));
    }

    public static StreamCodec<? super ByteBuf, ColorParticleOptions> streamCodec(ParticleType<ColorParticleOptions> type) {

        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, options -> options.size,
                ByteBufCodecs.FLOAT, options -> options.duration,
                ByteBufCodecs.FLOAT, options -> options.delay,
                ByteBufCodecs.INT, options -> options.rgba0,
                (size, duration, delay, rgba) -> new ColorParticleOptions(type, size, duration, delay, rgba)
        );
    }

}
