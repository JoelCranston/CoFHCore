package cofh.core.client.particle.impl;

import cofh.core.util.helpers.RenderHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

import javax.annotation.Nonnull;

public class SparkParticle extends SingleQuadParticle {

    private final SpriteSet spriteSet;

    private SparkParticle(ClientLevel levelIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, SpriteSet spriteSet) {

        super(levelIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, spriteSet.first());
        lifetime = 8;

        this.spriteSet = spriteSet;
        setSprite(spriteSet.get(random));
        xd = xSpeedIn;
        yd = ySpeedIn;
        zd = zSpeedIn;
        scale(1.5F);
        oRoll = roll = random.nextFloat() * 2 * (float) Math.PI;
    }

    @Override
    public void tick() {

        super.tick();
        if ((age & 1) == 0) {
            setSprite(spriteSet.get(random));
        }
    }

    @Override
    protected Layer getLayer() {

        return Layer.TRANSLUCENT;
    }

    @Override
    public int getLightCoords(float pTicks) {

        return RenderHelper.FULL_BRIGHT;
    }

    @Nonnull
    public static ParticleProvider<SimpleParticleType> factory(SpriteSet spriteSet) {

        return (data, level, x, y, z, dx, dy, dz, random) -> new SparkParticle(level, x, y, z, dx, dy, dz, spriteSet);
    }

}
