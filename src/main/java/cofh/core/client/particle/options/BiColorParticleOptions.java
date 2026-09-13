package cofh.core.client.particle.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class BiColorParticleOptions extends ColorParticleOptions {

    public final int rgba1;

    public BiColorParticleOptions(ParticleType<? extends BiColorParticleOptions> type, float size, float duration, float delay, int rgba0, int rgba1) {

        super(type, size, duration, delay, rgba0);
        this.rgba1 = rgba1;
    }

    public BiColorParticleOptions(ParticleType<? extends BiColorParticleOptions> type, float size, float duration, float delay) {

        this(type, size, duration, delay, 0xFFFFFFFF, 0xFFFFFFFF);
    }

    public BiColorParticleOptions(ParticleType<? extends BiColorParticleOptions> type, float size, float duration) {

        this(type, size, duration, 0.0F);
    }

    public BiColorParticleOptions(ParticleType<? extends BiColorParticleOptions> type) {

        this(type, 1.0F, 1.0F, 0.0F);
    }

    // Named distinctly from ColorParticleOptions#codec/streamCodec - see the same note on
    // CylindricalParticleOptions for why (static erasure "name clash" across the hierarchy).
    public static MapCodec<BiColorParticleOptions> biColorCodec(ParticleType<BiColorParticleOptions> type) {

        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("size").forGetter(options -> options.size),
                Codec.FLOAT.fieldOf("duration").forGetter(options -> options.duration),
                Codec.FLOAT.fieldOf("delay").forGetter(options -> options.delay),
                Codec.INT.fieldOf("rgba0").forGetter(options -> options.rgba0),
                Codec.INT.fieldOf("rgba1").forGetter(options -> options.rgba1)
        ).apply(builder, (size, duration, delay, rgba0, rgba1) -> new BiColorParticleOptions(type, size, duration, delay, rgba0, rgba1)));
    }

    public static StreamCodec<? super ByteBuf, BiColorParticleOptions> biColorStreamCodec(ParticleType<BiColorParticleOptions> type) {

        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, options -> options.size,
                ByteBufCodecs.FLOAT, options -> options.duration,
                ByteBufCodecs.FLOAT, options -> options.delay,
                ByteBufCodecs.INT, options -> options.rgba0,
                ByteBufCodecs.INT, options -> options.rgba1,
                (size, duration, delay, rgba0, rgba1) -> new BiColorParticleOptions(type, size, duration, delay, rgba0, rgba1)
        );
    }

}
