package cofh.core.client.particle.options;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class CylindricalParticleOptions extends ColorParticleOptions {

    public final float height;

    public CylindricalParticleOptions(ParticleType<? extends CylindricalParticleOptions> type, float size, float duration, float delay, int rgba0, float height) {

        super(type, size, duration, delay, rgba0);
        this.height = height;
    }

    public CylindricalParticleOptions(ParticleType<? extends CylindricalParticleOptions> type, float size, float duration, float delay, float height) {

        this(type, size, duration, delay, 0xFFFFFFFF, height);
    }

    public CylindricalParticleOptions(ParticleType<? extends CylindricalParticleOptions> type, float size, float duration, float height) {

        this(type, size, duration, 0.0F, height);
    }

    public CylindricalParticleOptions(ParticleType<? extends CylindricalParticleOptions> type) {

        this(type, 1.0F, 1.0F, 1.0F);
    }

    // Not codec/streamCodec: a static method of the same erasure would clash with ColorParticleOptions'.
    public static MapCodec<CylindricalParticleOptions> cylindricalCodec(ParticleType<CylindricalParticleOptions> type) {

        return RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.FLOAT.fieldOf("size").forGetter(options -> options.size),
                Codec.FLOAT.fieldOf("duration").forGetter(options -> options.duration),
                Codec.FLOAT.fieldOf("delay").forGetter(options -> options.delay),
                Codec.INT.fieldOf("rgba0").forGetter(options -> options.rgba0),
                Codec.FLOAT.fieldOf("height").forGetter(options -> options.height)
        ).apply(builder, (size, duration, delay, rgba, height) -> new CylindricalParticleOptions(type, size, duration, delay, rgba, height)));
    }

    public static StreamCodec<? super ByteBuf, CylindricalParticleOptions> cylindricalStreamCodec(ParticleType<CylindricalParticleOptions> type) {

        return StreamCodec.composite(
                ByteBufCodecs.FLOAT, options -> options.size,
                ByteBufCodecs.FLOAT, options -> options.duration,
                ByteBufCodecs.FLOAT, options -> options.delay,
                ByteBufCodecs.INT, options -> options.rgba0,
                ByteBufCodecs.FLOAT, options -> options.height,
                (size, duration, delay, rgba, height) -> new CylindricalParticleOptions(type, size, duration, delay, rgba, height)
        );
    }

}
