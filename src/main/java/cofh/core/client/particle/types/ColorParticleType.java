package cofh.core.client.particle.types;

import cofh.core.client.particle.options.ColorParticleOptions;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;

public class ColorParticleType extends ParticleType<ColorParticleOptions> {

    public ColorParticleType(boolean overrideLimit) {

        super(overrideLimit);
    }

    public ColorParticleType() {

        this(false);
    }

    @Override
    public MapCodec<ColorParticleOptions> codec() {

        return ColorParticleOptions.codec(this);
    }

    @Override
    public StreamCodec<? super ByteBuf, ColorParticleOptions> streamCodec() {

        return ColorParticleOptions.streamCodec(this);
    }

}
