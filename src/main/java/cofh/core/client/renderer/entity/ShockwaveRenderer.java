package cofh.core.client.renderer.entity;

import cofh.core.common.entity.Shockwave;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class ShockwaveRenderer extends EntityRenderer<Shockwave, EntityRenderState> {

    public ShockwaveRenderer(EntityRendererProvider.Context pContext) {

        super(pContext);
    }

    @Override
    public void submit(EntityRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {

    }

    @Override
    public EntityRenderState createRenderState() {

        return new EntityRenderState();
    }

}
