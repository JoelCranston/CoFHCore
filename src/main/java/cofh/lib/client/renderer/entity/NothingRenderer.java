package cofh.lib.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;

public class NothingRenderer extends EntityRenderer<Entity, EntityRenderState> {

    public NothingRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState camera) {

    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double x, double y, double z) {

        return super.shouldRender(entity, frustum, x, y, z);
    }

    @Override
    public EntityRenderState createRenderState() {

        return new EntityRenderState();
    }

}
