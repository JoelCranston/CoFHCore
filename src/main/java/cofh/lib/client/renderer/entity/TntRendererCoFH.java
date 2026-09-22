package cofh.lib.client.renderer.entity;

import cofh.lib.common.entity.PrimedTntCoFH;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class TntRendererCoFH extends EntityRenderer<PrimedTntCoFH, TntRenderState> {

    private final BlockModelResolver blockModelResolver;

    public TntRendererCoFH(EntityRendererProvider.Context ctx) {

        super(ctx);
        this.shadowRadius = 0.5F;
        this.blockModelResolver = ctx.getBlockModelResolver();
    }

    @Override
    public void submit(TntRenderState state, PoseStack poseStackIn, SubmitNodeCollector collector, CameraRenderState camera) {

        poseStackIn.pushPose();
        poseStackIn.translate(0.0D, 0.5D, 0.0D);
        if (state.fuseRemainingInTicks < 10.0F) {
            float f = 1.0F - state.fuseRemainingInTicks / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f = f * f;
            f = f * f;
            float f1 = 1.0F + f * 0.3F;
            poseStackIn.scale(f1, f1, f1);
        }
        poseStackIn.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStackIn.translate(-0.5D, -0.5D, 0.5D);
        poseStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
        renderFlash(state.blockState, poseStackIn, collector, state.lightCoords, (int) state.fuseRemainingInTicks / 5 % 2 == 0, state.outlineColor);
        poseStackIn.popPose();
        super.submit(state, poseStackIn, collector, camera);
    }

    @Override
    public TntRenderState createRenderState() {

        return new TntRenderState();
    }

    @Override
    public void extractRenderState(PrimedTntCoFH entityIn, TntRenderState state, float partialTicks) {

        super.extractRenderState(entityIn, state, partialTicks);
        state.fuseRemainingInTicks = entityIn.getFuse() - partialTicks + 1.0F;
        blockModelResolver.update(state.blockState, entityIn.getBlock().defaultBlockState(), TntRenderer.BLOCK_DISPLAY_CONTEXT);
    }

    public static void renderFlash(BlockModelRenderState blockModel, PoseStack poseStackIn, SubmitNodeCollector collector, int combinedLight, boolean doFullBright, int outlineColor) {

        int i;
        if (doFullBright) {
            i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            i = OverlayTexture.NO_OVERLAY;
        }
        blockModel.submit(poseStackIn, collector, combinedLight, i, outlineColor);
    }

}
