package cofh.core.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class BoatRendererCoFH extends AbstractBoatRenderer {

    private final Model.Simple waterPatchModel;
    private final EntityModel<BoatRenderState> model;

    public BoatRendererCoFH(EntityRendererProvider.Context context, boolean chestBoat, String modId, String name, ModelLayerLocation modelLayerLoc) {

        super(context, Identifier.fromNamespaceAndPath(modId, chestBoat ? "textures/entity/chest_boat/" + name + ".png" : "textures/entity/boat/" + name + ".png"));
        this.waterPatchModel = new Model.Simple(context.bakeLayer(ModelLayers.BOAT_WATER_PATCH), t -> RenderTypes.waterMask());
        this.model = new BoatModel(context.bakeLayer(modelLayerLoc));
    }

    @Override
    protected EntityModel<BoatRenderState> model() {

        return model;
    }

    @Override
    protected void submitTypeAdditions(BoatRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {

        if (!state.isUnderWater) {
            collector.submitModel(waterPatchModel, Unit.INSTANCE, poseStack, texture, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
    }

}
