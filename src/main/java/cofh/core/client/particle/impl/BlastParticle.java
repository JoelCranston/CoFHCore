package cofh.core.client.particle.impl;

import cofh.core.client.particle.SpriteParticle;
import cofh.core.client.particle.options.ColorParticleOptions;
import cofh.core.util.helpers.RenderHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

import javax.annotation.Nonnull;

public class BlastParticle extends SpriteParticle {

    private BlastParticle(ColorParticleOptions data, ClientLevel level, SpriteSet sprites, double x, double y, double z, double dx, double dy, double dz) {

        super(data, level, sprites, x, y, z, dx, dy, dz);
        gravity = -0.1F;
        friction = 0.9F;
        oRoll = roll = 0.5F * (float) random.nextGaussian();
    }

    @Override
    protected Layer getLayer() {

        return Layer.OPAQUE;
    }

    @Override
    public int getLightCoords(float pTicks) {

        return RenderHelper.FULL_BRIGHT;
    }

    @Override
    public void move(double dx, double dy, double dz) {

    }

    @Override
    protected void updateVelocity() {

    }

    @Nonnull
    public static ParticleProvider<ColorParticleOptions> factory(SpriteSet spriteSet) {

        return (data, level, x, y, z, dx, dy, dz, random) -> new BlastParticle(data, level, spriteSet, x, y, z, dx, dy, dz);
    }

}
