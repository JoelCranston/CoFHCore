package cofh.core.client.particle;

import cofh.core.client.particle.options.CoFHParticleOptions;
import cofh.lib.util.helpers.MathHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * The base class for CoFH particles.
 */
public abstract class CoFHParticle extends Particle {

    public static final ParticleRenderType GROUP = new ParticleRenderType("cofh_core:custom");

    //The total time a particle is rendered, in ticks.
    protected float duration = 1.0F;
    //The amount of delay before a particle is rendered, in ticks.
    protected float delay = 0.0F;
    //The size of the particle. May represent different things for different particles. Refer to the implementation for exact details.
    protected float size = 1.0F;
    protected final int seed;

    public CoFHParticle(CoFHParticleOptions data, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {

        super(level, x + dx, y + dy, z + dz);
        this.seed = random.nextInt();
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        setLifetime(data.duration, data.delay);
        setSize(data.size);
    }

    public void render(PoseStack stack, MultiBufferSource buffer, Vec3 cameraPos, float pTicks) {

        float time = this.age + pTicks - this.delay;
        if (time < 0 || this.duration <= time) {
            return;
        }
        stack.pushPose();

        double x = MathHelper.interpolate(this.xo, this.x, pTicks);
        double y = MathHelper.interpolate(this.yo, this.y, pTicks);
        double z = MathHelper.interpolate(this.zo, this.z, pTicks);
        stack.translate(x - cameraPos.x, y - cameraPos.y, z - cameraPos.z);

        render(stack, buffer, getLightCoords(pTicks, x, y, z), time, pTicks);

        stack.popPose();
    }

    /**
     * Method for rendering impl.
     *
     * @param buffer Buffer source for this particle; each {@link RenderType} requested is submitted separately.
     * @param time   Number of ticks since the particle started rendering.
     * @param pTicks Partial ticks. The {@code time} parameter should usually be used instead.
     */
    public abstract void render(PoseStack stack, MultiBufferSource buffer, int packedLight, float time, float pTicks);

    @Override
    public ParticleRenderType getGroup() {

        return GROUP;
    }

    @Override
    public int getLightCoords(float pTicks) {

        double x = MathHelper.interpolate(this.xo, this.x, pTicks);
        double y = MathHelper.interpolate(this.yo, this.y, pTicks);
        double z = MathHelper.interpolate(this.zo, this.z, pTicks);
        return getLightCoords(pTicks, x, y, z);
    }

    protected int getLightCoords(float pTicks, double x, double y, double z) {

        BlockPos blockpos = BlockPos.containing(x, y, z);
        return this.level.hasChunkAt(blockpos) ? LevelRenderer.getLightCoords(this.level, blockpos) : 0;
    }

    protected void setLifetime(float duration, float delay) {

        this.delay = delay;
        this.duration = duration;
        lifetime = MathHelper.ceil(this.duration + delay);
    }

    protected void setSize(float size) {

        this.size = size;
    }

}
