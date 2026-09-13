package cofh.core.client.particle.types;

import cofh.core.client.particle.options.BiColorParticleOptions;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.StreamCodec;

public class PointToPointParticleType extends ParticleType<BiColorParticleOptions> {

    public PointToPointParticleType(boolean overrideLimit) {

        super(overrideLimit);
    }

    public PointToPointParticleType() {

        this(false);
    }

    @Override
    public MapCodec<BiColorParticleOptions> codec() {

        return BiColorParticleOptions.biColorCodec(this);
    }

    @Override
    public StreamCodec<? super ByteBuf, BiColorParticleOptions> streamCodec() {

        return BiColorParticleOptions.biColorStreamCodec(this);
    }

}
