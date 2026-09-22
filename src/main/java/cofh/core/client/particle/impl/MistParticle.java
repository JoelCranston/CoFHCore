package cofh.core.client.particle.impl;

import cofh.core.client.particle.options.ColorParticleOptions;
import cofh.core.util.helpers.vfx.Color;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

import javax.annotation.Nonnull;

public class MistParticle extends GasParticle {

    protected MistParticle(ColorParticleOptions data, ClientLevel level, SpriteSet sprites, double x, double y, double z, double dx, double dy, double dz) {

        super(data, level, sprites, x, y, z, dx, dy, dz);
        oRoll = roll = random.nextFloat() * MathHelper.F_TAU;
        baseColor = Color.fromRGBA(data.rgba0);
        groundFriction = 0.3F;
    }

    @Override
    protected void animate(float time, float pTicks) {

        float progress = time / duration;
        float q = 2 * progress - 1;
        q *= q;
        setColor0(baseColor.scaleAlpha(Math.max((1 - q * q) * MathHelper.cos(0.25F * MathHelper.F_PI * progress), 0)));

        //Only set render size based off BB size
        this.size = this.bbWidth * MathHelper.sin(0.25F * MathHelper.F_PI * (progress + 1));
        super.animate(time, pTicks);
    }

    @Nonnull
    public static ParticleProvider<ColorParticleOptions> factory(SpriteSet spriteSet) {

        return (data, level, x, y, z, dx, dy, dz, random) -> new MistParticle(data, level, spriteSet, x, y, z, dx, dy, dz);
    }

}
