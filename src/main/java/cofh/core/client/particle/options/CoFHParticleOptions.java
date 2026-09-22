package cofh.core.client.particle.options;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

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
