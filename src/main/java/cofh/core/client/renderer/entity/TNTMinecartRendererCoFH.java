package cofh.core.client.renderer.entity;

import cofh.core.common.entity.AbstractTNTMinecart;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.util.Mth;

public class TNTMinecartRendererCoFH extends AbstractMinecartRenderer<AbstractTNTMinecart, MinecartTntRenderState> {

    public TNTMinecartRendererCoFH(EntityRendererProvider.Context renderManagerIn) {

        super(renderManagerIn, ModelLayers.TNT_MINECART);
    }

    @Override
    protected void submitMinecartContents(MinecartTntRenderState state, BlockModelRenderState blockModel, PoseStack poseStackIn, SubmitNodeCollector collector, int packedLightIn) {

        float f = state.fuseRemainingInTicks;
        if (f > -1.0F && f < 10.0F) {
            float f1 = 1.0F - f / 10.0F;
            f1 = Mth.clamp(f1, 0.0F, 1.0F);
            f1 = f1 * f1;
            f1 = f1 * f1;
            float f2 = 1.0F + f1 * 0.3F;
            poseStackIn.scale(f2, f2, f2);
        }
        TntMinecartRenderer.submitWhiteSolidBlock(blockModel, poseStackIn, collector, packedLightIn, f > -1.0F && (int) f / 5 % 2 == 0, state.outlineColor);
    }

    @Override
    public MinecartTntRenderState createRenderState() {

        return new MinecartTntRenderState();
    }

    @Override
    public void extractRenderState(AbstractTNTMinecart entityIn, MinecartTntRenderState state, float partialTicks) {

        super.extractRenderState(entityIn, state, partialTicks);
        int i = entityIn.getFuseTicks();
        state.fuseRemainingInTicks = i > -1 ? i - partialTicks + 1.0F : -1.0F;
    }

}
