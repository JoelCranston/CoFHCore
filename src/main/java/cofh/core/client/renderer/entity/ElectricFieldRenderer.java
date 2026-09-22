package cofh.core.client.renderer.entity;

import cofh.core.common.entity.ElectricField;
import cofh.core.util.helpers.RenderHelper;
import cofh.lib.util.constants.ModIds;
import cofh.lib.util.helpers.MathHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.SplittableRandom;
import java.util.stream.IntStream;

public class ElectricFieldRenderer extends EntityRenderer<ElectricField, ElectricFieldRenderer.ElectricFieldRenderState> {

    public static final Identifier[] TEXTURES = IntStream.range(0, 5).mapToObj(i -> Identifier.fromNamespaceAndPath(ModIds.ID_COFH_CORE, "textures/particle/plasma_ball_" + i + ".png")).toArray(Identifier[]::new);

    public ElectricFieldRenderer(EntityRendererProvider.Context ctx) {

        super(ctx);
    }

    @Override
    public void submit(ElectricFieldRenderState state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {

        stack.pushPose();

        int time = MathHelper.floor(state.ageInTicks * 0.75F);
        SplittableRandom rand = new SplittableRandom(time * 69420L);
        float rot = rand.nextFloat(MathHelper.F_TAU);

        int packedLight = RenderHelper.FULL_BRIGHT;
        float eyeHeight = state.eyeHeight;

        collector.submitCustomGeometry(stack, RenderTypes.entityTranslucent(state.texture), (pose, consumer) -> {
            Vector4f center = new Vector4f(0, eyeHeight, 0, 1).mul(pose.pose());

            float x = center.x();
            float y = center.y();
            float z = center.z() + 0.1F;

            float sin = MathHelper.sin(rot);
            float cos = MathHelper.cos(rot);
            float w = 0.5F;
            float a = w * (cos - sin);
            float b = w * (sin + cos);

            Vector3f normal = new Vector3f(0, 1, 0).mul(pose.normal());
            float nx = normal.x();
            float ny = normal.y();
            float nz = normal.z();

            consumer.addVertex(x + a, y + b, z).setColor(0xFF, 0xFF, 0xFF, 0xFF).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
            consumer.addVertex(x - b, y + a, z).setColor(0xFF, 0xFF, 0xFF, 0xFF).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
            consumer.addVertex(x - a, y - b, z).setColor(0xFF, 0xFF, 0xFF, 0xFF).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
            consumer.addVertex(x + b, y - a, z).setColor(0xFF, 0xFF, 0xFF, 0xFF).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(nx, ny, nz);
        });

        stack.popPose();

        super.submit(state, stack, collector, camera);
    }

    @Override
    public boolean shouldRender(ElectricField entity, Frustum clip, double x, double y, double z) {

        return super.shouldRender(entity, clip, x, y, z);
    }

    @Override
    public ElectricFieldRenderState createRenderState() {

        return new ElectricFieldRenderState();
    }

    @Override
    public void extractRenderState(ElectricField entity, ElectricFieldRenderState state, float partialTicks) {

        super.extractRenderState(entity, state, partialTicks);
        state.texture = getTextureLocation(entity);
    }

    public Identifier getTextureLocation(ElectricField entity) {

        return TEXTURES[entity.getTextureIndex(TEXTURES.length)];
    }

    public static class ElectricFieldRenderState extends EntityRenderState {

        public Identifier texture = TEXTURES[0];

    }

}
