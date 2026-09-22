package cofh.core.client.particle.impl;

import cofh.core.client.particle.CylindricalParticle;
import cofh.core.client.particle.options.CylindricalParticleOptions;
import cofh.core.util.helpers.vfx.VFXHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;

public class ShockwaveParticle extends CylindricalParticle {

    private ShockwaveParticle(CylindricalParticleOptions data, ClientLevel level, double x, double y, double z, double xDir, double yDir, double zDir) {

        super(data, level, Math.floor(x), Math.floor(y), Math.floor(z));
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buffer, int packedLightIn, float time, float pTicks) {

        VFXHelper.renderShockwave(stack, buffer, level, BlockPos.containing(x, y, z), time * (size * 0.5F + 5) / duration, size * 0.5F, height);
    }

    @Nonnull
    public static ParticleProvider<CylindricalParticleOptions> factory(SpriteSet spriteSet) {

        return (data, level, x, y, z, xDir, yDir, zDir, random) -> new ShockwaveParticle(data, level, x, y, z, xDir, yDir, zDir);
    }

}
