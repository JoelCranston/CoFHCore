package cofh.core.client.renderer.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Lazy;

import static net.minecraft.client.model.geom.LayerDefinitions.OUTER_ARMOR_DEFORMATION;

public class ArmorFullSuitModel extends HumanoidModel<HumanoidRenderState> {

    public static final ModelLayerLocation ARMOR_FULL_SUIT_LAYER = new ModelLayerLocation(Identifier.parse("cofh_core:armor_full_suit"), "outer_armor");
    public static final Lazy<HumanoidModel<HumanoidRenderState>> INSTANCE = Lazy.of(() -> new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ARMOR_FULL_SUIT_LAYER)));

    public ArmorFullSuitModel(ModelPart root) {

        super(root);
    }

    public static LayerDefinition createBodyLayer() {

        CubeDeformation scale = OUTER_ARMOR_DEFORMATION;
        MeshDefinition meshdefinition = HumanoidModel.createMesh(scale, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, scale.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, scale.extend(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale.extend(-0.5F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, scale.extend(-0.5F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

}
