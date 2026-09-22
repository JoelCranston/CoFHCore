package cofh.core.client.particle;

import cofh.core.client.particle.options.ColorParticleOptions;
import cofh.core.util.helpers.vfx.Color;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Reimplementation of {@link SingleQuadParticle} in a CoFH flavor.
 * Should theoretically be much more configurable and performant compared to vanilla.
 */
public abstract class SpriteParticle extends SingleQuadParticle {

    //The total time a particle is rendered, in ticks.
    protected float duration = 1.0F;
    //The amount of delay before a particle is rendered, in ticks.
    protected float delay = 0.0F;
    //The size of the particle, i.e. the width of the quad.
    protected float size = 1.0F;
    protected final int seed;
    public Color c0 = Color.WHITE;

    protected final SpriteSet sprites;

    protected SpriteParticle(ColorParticleOptions data, ClientLevel level, SpriteSet sprites, double x, double y, double z, double dx, double dy, double dz) {

        super(level, x + dx, y + dy, z + dz, sprites.get(0, 1));
        this.seed = random.nextInt();
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.sprites = sprites;
        setLifetime(data.duration, data.delay);
        setSize(data.size);
        setColor0(data.rgba0);
    }

    @Override
    public void tick() {

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age >= this.lifetime) {
            this.remove();
            return;
        }
        if (this.age++ >= this.delay) {
            setSprite();
            this.move(this.xd, this.yd, this.zd);
            updateVelocity();
        }
    }

    protected void setSprite() {

        int max = MathHelper.ceil(this.duration * 128);
        int time = MathHelper.clamp(MathHelper.floor((this.age - this.delay) * 128), 0, max);
        this.sprite = sprites.get(time, max);
    }

    protected void updateVelocity() {

        this.yd -= 0.04D * (double) this.gravity;
        if (this.speedUpWhenYMotionIsBlocked && this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
        }

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        if (this.onGround) {
            this.xd *= 0.7F;
            this.zd *= 0.7F;
        }
    }

    @Override
    protected Layer getLayer() {

        return Layer.TRANSLUCENT;
    }

    protected void setLifetime(float duration, float delay) {

        this.delay = delay;
        this.duration = duration;
        lifetime = MathHelper.ceil(this.duration + delay);
    }

    public void setSize(float size) {

        float half = size * 0.5F;
        Vec3 pos = new Vec3(x, y + half, z);
        setBoundingBox(new AABB(pos, pos).inflate(half));
        bbWidth = bbHeight = size;
        this.size = size;
    }

    protected void setColor0(int rgba) {

        this.c0 = Color.fromRGBA(rgba);
    }

    protected void setColor0(Color color) {

        this.c0 = color;
    }

    @Override
    public float getQuadSize(float pTicks) {

        return size * 0.5F;
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float pTicks) {

        float time = this.age + pTicks - this.delay;
        if (time < 0 || this.duration <= time) {
            return;
        }
        animate(time, pTicks);
        this.rCol = c0.r * Color.INT_TO_FLOAT;
        this.gCol = c0.g * Color.INT_TO_FLOAT;
        this.bCol = c0.b * Color.INT_TO_FLOAT;
        this.alpha = c0.a * Color.INT_TO_FLOAT;
        super.extract(state, camera, pTicks);
    }

    /**
     * Updates the colour and size for the frame about to be drawn.
     *
     * @param time   Number of ticks since the particle started rendering.
     * @param pTicks Partial ticks. The {@code time} parameter should usually be used instead.
     */
    protected void animate(float time, float pTicks) {

    }

}
