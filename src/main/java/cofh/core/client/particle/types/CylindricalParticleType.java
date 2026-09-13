package cofh.core.client.particle.types;

import cofh.core.client.particle.options.CylindricalParticleOptions;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;

public class CylindricalParticleType extends ParticleType<CylindricalParticleOptions> {

    public CylindricalParticleType(boolean overrideLimit) {

        super(overrideLimit);
    }

    public CylindricalParticleType() {

        this(true);
    }

    @Override
    public MapCodec<CylindricalParticleOptions> codec() {

        return CylindricalParticleOptions.cylindricalCodec(this);
    }

    @Override
    public StreamCodec<? super ByteBuf, CylindricalParticleOptions> streamCodec() {

        return CylindricalParticleOptions.cylindricalStreamCodec(this);
    }

}
