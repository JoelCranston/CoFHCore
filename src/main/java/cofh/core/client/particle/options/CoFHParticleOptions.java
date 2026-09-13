package cofh.core.client.particle.options;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

/**
 * Shared fields (size/duration/delay) for CoFH's particle options. {@link ParticleOptions} no
 * longer carries network/command serialization itself upstream - {@code writeToNetwork},
 * {@code writeToString}, and the {@code Deserializer} inner class were all removed. Each leaf
 * subclass now owns its own {@code MapCodec} (for {@link ParticleType#codec()}) and
 * {@code StreamCodec} (for {@link ParticleType#streamCodec()}) built by a static factory that
 * takes the owning {@link ParticleType}, following vanilla's {@code ColorParticleOption} pattern.
 * <p>
 * This base class is never registered as its own leaf {@link ParticleType} (only its subclasses
 * are), so it has no codec/streamCodec of its own.
 */
public class CoFHParticleOptions implements ParticleOptions {

    public final ParticleType<? extends CoFHParticleOptions> type;
    public final float size;
    public final float duration;
    public final float delay;

    public CoFHParticleOptions(ParticleType<? extends CoFHParticleOptions> type, float size, float duration, float delay) {

        this.type = type;
        this.size = size;
        this.duration = duration;
        this.delay = delay;
    }

    public CoFHParticleOptions(ParticleType<? extends CoFHParticleOptions> type, float size, float duration) {

        this(type, size, duration, 0.0F);
    }

    @Override
    public ParticleType<? extends CoFHParticleOptions> getType() {

        return type;
    }

}
