package cofh.core.client.renderer.entity;

import cofh.core.common.entity.ThrownKnife;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

import static net.minecraft.world.entity.projectile.arrow.AbstractArrow.IN_GROUND;

public class KnifeRenderer extends EntityRenderer<ThrownKnife, KnifeRenderer.KnifeRenderState> {

    protected final ItemModelResolver itemModelResolver;

    public KnifeRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
        this.itemModelResolver = ctx.getItemModelResolver();
    }

    @Override
    public void submit(KnifeRenderState state, PoseStack poseStackIn, SubmitNodeCollector collector, CameraRenderState camera) {

        poseStackIn.pushPose();
        poseStackIn.mulPose(Axis.YP.rotationDegrees(state.yRot + 90));
        if (state.inGround) {
            poseStackIn.mulPose(Axis.ZP.rotationDegrees(state.groundRoll));
        } else {
            poseStackIn.mulPose(Axis.ZP.rotationDegrees(state.ageInTicks * 40));
        }
        poseStackIn.scale(1.25F, 1.25F, 1.25F);
        state.item.submit(poseStackIn, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStackIn.popPose();
        super.submit(state, poseStackIn, collector, camera);
    }

    @Override
    public KnifeRenderState createRenderState() {

        return new KnifeRenderState();
    }

    @Override
    public void extractRenderState(ThrownKnife entityIn, KnifeRenderState state, float partialTicks) {

        super.extractRenderState(entityIn, state, partialTicks);
        state.yRot = Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot());
        state.inGround = entityIn.getEntityData().get(IN_GROUND);
        state.groundRoll = 0;
        if (state.inGround) {
            Vec3 pos = entityIn.position().subtract(Vec3.atCenterOf(entityIn.blockPosition()));
            double y = Math.abs(pos.y);
            if (Math.abs(pos.x) > y || Math.abs(pos.z) > y) {
                state.groundRoll = 90;
            } else if (pos.y <= 0) {
                state.groundRoll = 180;
            }
        }
        itemModelResolver.updateForNonLiving(state.item, entityIn.getPickupItem(), ItemDisplayContext.GROUND, entityIn);
    }

    public static class KnifeRenderState extends ThrownItemRenderState {

        public float yRot;
        public boolean inGround;
        public float groundRoll;

    }

}
