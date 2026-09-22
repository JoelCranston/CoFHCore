package cofh.core.util.helpers;

import cofh.core.client.event.CoreClientEvents;
import cofh.core.util.helpers.vfx.Color;
import cofh.lib.util.helpers.MathHelper;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Contains various helper functions to assist with rendering.
 *
 * @author King Lemming
 */
public final class RenderHelper {

    private RenderHelper() {

    }

    public static final float RENDER_OFFSET = 1.0F / 512.0F;
    public static final int FULL_BRIGHT = 0x00F000F0;
    public static final Identifier MC_BLOCK_SHEET = Identifier.parse("textures/atlas/blocks.png");
    public static final Identifier MC_FONT_DEFAULT = Identifier.parse("textures/font/ascii.png");
    public static final Identifier MC_FONT_SGA = Identifier.parse("textures/font/ascii_sga.png");
    public static final Identifier MC_ITEM_GLINT = Identifier.parse("textures/misc/enchanted_item_glint.png");
    public static PoseStack particleStack = new PoseStack();

    // region ACCESSORS
    public static MultiBufferSource.BufferSource bufferSource() {

        return Minecraft.getInstance().renderBuffers().bufferSource();
    }

    public static TextureManager engine() {

        return Minecraft.getInstance().getTextureManager();
    }

    public static TextureAtlas textureMap() {

        return Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
    }

    public static Tesselator tesselator() {

        return Tesselator.getInstance();
    }

    public static EntityRenderDispatcher renderEntity() {

        return Minecraft.getInstance().getEntityRenderDispatcher();
    }

    public static ModelPart bakeLayer(ModelLayerLocation location) {

        return Minecraft.getInstance().getEntityModels().bakeLayer(location);
    }

    public static int renderTime() {

        return CoreClientEvents.renderTime;
    }

    public static float partialTick() {

        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static float frameDelta() {

        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
    }

    public static boolean isFabulousGraphics() {

        return Minecraft.getInstance().options.improvedTransparency().get();
    }
    // endregion

    // region DRAW METHODS
    // TODO This is unused, needs PoseStack argument and Shaders set
/*    public static void drawStencil(int xStart, int yStart, int xEnd, int yEnd, int flag) {

        RenderSystem.disableTexture();
        GL11.glStencilFunc(GL11.GL_ALWAYS, flag, flag);
        GL11.glStencilOp(GL11.GL_ZERO, GL11.GL_ZERO, GL11.GL_REPLACE);
        GL11.glStencilMask(flag);
        RenderSystem.colorMask(false, false, false, false);
        RenderSystem.depthMask(false);
        GL11.glClearStencil(0);
        RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, false);

        BufferBuilder buffer = Tessellator.getInstance().getBuilder();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buffer.vertex(xStart, yEnd, 0).endVertex();
        buffer.vertex(xEnd, yEnd, 0).endVertex();
        buffer.vertex(xEnd, yStart, 0).endVertex();
        buffer.vertex(xStart, yStart, 0).endVertex();
        Tessellator.getInstance().end();

        RenderSystem.enableTexture();
        GL11.glStencilFunc(GL11.GL_EQUAL, flag, flag);
        GL11.glStencilMask(0);
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.depthMask(true);
    }*/
    // endregion

    // region MATRIX DRAW METHODS
    public static void drawFluid(GuiGraphicsExtractor pGuiGraphics, int x, int y, FluidStack fluid, int width, int height) {

        if (fluid.isEmpty()) {
            return;
        }
        drawTiledTexture(pGuiGraphics, x, y, getFluidTexture(fluid), width, height, getFluidColor(fluid));
    }

    public static void drawIcon(GuiGraphicsExtractor pGuiGraphics, int x, int y, TextureAtlasSprite icon, int width, int height) {

        pGuiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x, y, width, height);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor pGuiGraphics, int x, int y, TextureAtlasSprite icon, int width, int height) {

        drawTiledTexture(pGuiGraphics, x, y, icon, width, height, -1);
    }

    public static void drawTiledTexture(GuiGraphicsExtractor pGuiGraphics, int x, int y, TextureAtlasSprite icon, int width, int height, int color) {

        if (icon == null || width <= 0 || height <= 0) {
            return;
        }
        // Whole 16x16 tiles, clipped at the edge; the sprite sub-rectangle blit is private.
        pGuiGraphics.enableScissor(x, y, x + width, y + height);
        for (int i = 0; i < width; i += 16) {
            for (int j = 0; j < height; j += 16) {
                pGuiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x + i, y + j, 16, 16, color);
            }
        }
        pGuiGraphics.disableScissor();
    }

    public static void drawScaledTexturedModalRectFromSprite(GuiGraphicsExtractor pGuiGraphics, int x, int y, TextureAtlasSprite icon, int width, int height) {

        if (icon == null) {
            return;
        }
        float minU = icon.getU0();
        float maxU = icon.getU1();
        float minV = icon.getV0();
        float maxV = icon.getV1();

        float u = minU + (maxU - minU) * width / 16F;
        float v = minV + (maxV - minV) * height / 16F;

        pGuiGraphics.blit(icon.atlasLocation(), x, y, x + width, y + height, minU, u, minV, v);
    }
    // endregion

    // region PASSTHROUGHS
    // TODO Fix these if needed.
/*    public static void disableStandardItemLighting() {

        net.minecraft.client.renderer.RenderHelper.turnOff();
    }

    public static void enableStandardItemLighting() {

        net.minecraft.client.renderer.RenderHelper.turnBackOn();
    }

    public static void setupGuiFlatDiffuseLighting() {

        net.minecraft.client.renderer.RenderHelper.setupForFlatItems();
    }

    public static void setupGui3DDiffuseLighting() {

        net.minecraft.client.renderer.RenderHelper.setupFor3DItems();
    }*/
    // endregion

    // region TEXTURE GETTERS
    public static TextureAtlasSprite getTexture(String location) {

        return textureMap().getSprite(Identifier.parse(location));
    }

    public static TextureAtlasSprite getTexture(Identifier location) {

        return textureMap().getSprite(location);
    }

    public static FluidModel getFluidModel(Fluid fluid) {

        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
    }

    public static TextureAtlasSprite getFluidTexture(Fluid fluid) {

        return getFluidModel(fluid).stillMaterial().sprite();
    }

    public static TextureAtlasSprite getFluidTexture(FluidStack fluid) {

        return getFluidTexture(fluid.getFluid());
    }

    public static int getFluidColor(FluidStack fluid) {

        return ARGB.opaque(FluidHelper.color(fluid));
    }

    public static boolean textureExists(String location) {

        return textureExists(Identifier.parse(location));
    }

    public static boolean textureExists(Identifier location) {

        return getTexture(location) != getTexture(MissingTextureAtlasSprite.getLocation());
    }
    // endregion

    public static BakedQuad mulColor(BakedQuad quad, int color) {

        MutableQuad mutable = new MutableQuad().setFrom(quad);
        for (int v = 0; v < 4; v++) {
            mutable.setColor(v, ARGB.multiply(quad.bakedColors().color(v), ARGB.opaque(color)));
        }
        return mutable.toBakedQuad();
    }

    public static float red(int color) {

        return (float) (color >> 16 & 255) / 255.0F;
    }

    public static float green(int color) {

        return (float) (color >> 8 & 255) / 255.0F;
    }

    public static float blue(int color) {

        return (float) (color & 255) / 255.0F;
    }

    public static void renderItemOnBlockSide(PoseStack poseStackIn, ItemStack stack, Direction side, BlockPos pos) {

        if (stack.isEmpty() || side.getAxis() == Direction.Axis.Y) {
            return;
        }
        poseStackIn.pushPose();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        switch (side) {
            case NORTH -> poseStackIn.translate(x + 0.75, y + 0.84375, z + RenderHelper.RENDER_OFFSET * 145);
            case SOUTH -> {
                poseStackIn.translate(x + 0.25, y + 0.84375, z + 1 - RenderHelper.RENDER_OFFSET * 145);
                poseStackIn.mulPose(MathHelper.quaternion(0, 180, 0));
            }
            case WEST -> {
                poseStackIn.translate(x + RenderHelper.RENDER_OFFSET * 145, y + 0.84375, z + 0.25);
                poseStackIn.mulPose(MathHelper.quaternion(0, 90, 0));
            }
            case EAST -> {
                poseStackIn.translate(x + 1 - RenderHelper.RENDER_OFFSET * 145, y + 0.84375, z + 0.75);
                poseStackIn.mulPose(MathHelper.quaternion(0, 270, 0));
            }
            default -> {
            }
        }
        poseStackIn.scale(0.03125F, 0.03125F, -RenderHelper.RENDER_OFFSET);
        poseStackIn.mulPose(MathHelper.quaternion(0, 0, 180));

        // renderItem().renderAndDecorateItem(stack, 0, 0);

        poseStackIn.popPose();

        // What of this do I still need?

        //        GlStateManager.enableAlpha();
        //        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        //        GlStateManager.enableBlend();
        //        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        //        GlStateManager.popMatrix();
        //        net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
    }

    // TODO Fix if required, 1.17 render changes are required.
/*    private static void renderFastItem(@Nonnull ItemStack itemStack, BlockState state, int slot, PoseStack matrix, MultiBufferSource.BufferSource buffer, int combinedLight, int combinedOverlay, Direction side, float partialTickTime) {

        matrix.pushPose();

        Consumer<MultiBufferSource.BufferSource> finish = (IRenderTypeBuffer buf) -> {
            if (buf instanceof IRenderTypeBuffer.Impl)
                ((IRenderTypeBuffer.Impl) buf).endBatch();
        };

        try {
            matrix.translate(0, 0, 100f);
            matrix.scale(1, -1, 1);
            matrix.scale(16, 16, 16);

            IBakedModel itemModel = renderItem().getModel(itemStack, null, null);
            boolean render3D = itemModel.isGui3d();
            finish.accept(buffer);

            if (render3D) {
                setupGui3DDiffuseLighting();
            } else {
                setupGuiFlatDiffuseLighting();
            }
            matrix.last().normal().set(1, -1, 1);
            renderItem().render(itemStack, ItemCameraTransforms.TransformType.GUI, false, matrix, buffer, combinedLight, combinedOverlay, itemModel);
            finish.accept(buffer);
        } catch (Exception e) {
            // pokemon!
        }
        matrix.popPose();
    }*/

    // region GEOMETRY
    public static void renderCuboid(AABB aabb, PoseStack poseStack, VertexConsumer buffer, int light, float r, float g, float b, float a, TextureAtlasSprite icon) {

        var pose = poseStack.last();
        var mat4 = pose.pose();

        float u0 = icon.getU0();
        float u1 = icon.getU1();
        float v0 = icon.getV0();
        float v1 = icon.getV1();

        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, -1.0F, 0.0F);

        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);

        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, -1.0F);

        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);

        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.minX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1.0F, 0.0F, 0.0F);

        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.minZ).setColor(r, g, b, a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
        buffer.addVertex(mat4, (float) aabb.maxX, (float) aabb.minY, (float) aabb.maxZ).setColor(r, g, b, a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1.0F, 0.0F, 0.0F);
    }

    public static Vector4f[] getCuboidCorners(Matrix4f pose, float w, float l, float h) {

        Vector4f[] corners = {new Vector4f(-w, -l, -h, 1.0F), new Vector4f(-w, -l, h, 1.0F), new Vector4f(w, -l, -h, 1.0F), new Vector4f(w, -l, h, 1.0F),
                new Vector4f(w, l, -h, 1.0F), new Vector4f(w, l, h, 1.0F), new Vector4f(-w, l, -h, 1.0F), new Vector4f(-w, l, h, 1.0F)};

        for (Vector4f corner : corners) {
            corner.mul(pose);
        }
        return corners;
    }

    public static void renderSides(VertexConsumer consumer, int light, Color color, Vector4f[] corners, Vector3f normal, float u0, float v0, float u1, float v1) {

        renderFace(consumer, light, color, corners[1], corners[0], corners[2], corners[3], u0, v0, u1, v1, normal);
        renderFace(consumer, light, color, corners[3], corners[2], corners[4], corners[5], u0, v0, u1, v1, normal);
        renderFace(consumer, light, color, corners[5], corners[4], corners[6], corners[7], u0, v0, u1, v1, normal);
        renderFace(consumer, light, color, corners[7], corners[6], corners[0], corners[1], u0, v0, u1, v1, normal);
    }

    public static void renderBottom(VertexConsumer consumer, int light, Color color, Vector4f[] corners, Vector3f normal, float u0, float v0, float u1, float v1) {

        renderFace(consumer, light, color, corners[6], corners[4], corners[2], corners[0], u0, v0, u1, v1, normal);
    }

    public static void renderTop(VertexConsumer consumer, int light, Color color, Vector4f[] corners, Vector3f normal, float u0, float v0, float u1, float v1) {

        renderFace(consumer, light, color, corners[1], corners[3], corners[5], corners[7], u0, v0, u1, v1, normal);
    }

    public static void renderCuboid(VertexConsumer consumer, int light, Color color, Vector4f[] corners, Vector3f normal, float u0, float v0, float u1, float v1) {

        renderSides(consumer, light, color, corners, normal, u0, v0, u1, v1);
        renderBottom(consumer, light, color, corners, normal, u0, v0, u1, v1);
        renderTop(consumer, light, color, corners, normal, u0, v0, u1, v1);
    }

    public static void renderFace(VertexConsumer consumer, int light, Color color, Vector4f a, Vector4f b, Vector4f c, Vector4f d, float u0, float v0, float u1, float v1, Vector3f normal) {

        consumer.addVertex(a.x(), a.y(), a.z()).setColor(color.r, color.g, color.b, color.a).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(b.x(), b.y(), b.z()).setColor(color.r, color.g, color.b, color.a).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(c.x(), c.y(), c.z()).setColor(color.r, color.g, color.b, color.a).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
        consumer.addVertex(d.x(), d.y(), d.z()).setColor(color.r, color.g, color.b, color.a).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(normal.x(), normal.y(), normal.z());
    }

    /**
     * Renders a trigonal trapezohedron (a cube stretched along one diagonal). Has 6 congruent rhombi as faces.
     *
     * @param height The total height of the polyhedron.
     * @param radius The maximum distance of a point within the polyhedron from its axis.
     */
    public static void renderTrigonalTrapezohedron(PoseStack stack, VertexConsumer consumer, int packedLight, Color col, float height, float radius) {

        int a = col.a;
        if (a <= 0) {
            return;
        }
        int r = col.r;
        int g = col.g;
        int b = col.b;
        PoseStack.Pose last = stack.last();
        Matrix4f pose = last.pose();
        //float lenSqr = (diagRatio * diagRatio + 1) * 0.25F;
        //float rad = 0.57735F * diagRatio;
        //float h = 0.5F * (float) Math.sqrt(lenSqr - rad * rad);
        float h = 0.1666667F * height;

        Vector4f u = new Vector4f(0, 3 * h, 0, 1).mul(pose);
        Vector4f l = new Vector4f(0, -3 * h, 0, 1).mul(pose);
        Vector4f[] v = new Vector4f[8];
        for (int i = 0; i < 6; ++i) {
            v[i] = new Vector4f(radius * MathHelper.sin(i * 1.0472F), h, radius * MathHelper.cos(i * 1.0472F), 1).mul(pose);
            h = -h;
        }
        v[6] = v[0];
        v[7] = v[1];
        for (int i = 0; i < 6; i += 2) {
            Vector4f v0 = v[i];
            Vector4f v1 = v[i + 1];
            Vector4f v2 = v[i + 2];
            consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v2.x(), v2.y(), v2.z()).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(u.x(), u.y(), u.z()).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
        }
        for (int i = 1; i < 7; i += 2) {
            Vector4f v0 = v[i + 2];
            Vector4f v1 = v[i + 1];
            Vector4f v2 = v[i];
            consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v2.x(), v2.y(), v2.z()).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(l.x(), l.y(), l.z()).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
        }
    }

    /**
     * Renders a bipyramid.
     *
     * @param height    The total height of the polyhedron.
     * @param radius    The maximum distance of a point within the polyhedron from its axis.
     * @param baseEdges The number of edges that the base of each pyramid has.
     */
    public static void renderBipyramid(PoseStack stack, VertexConsumer consumer, int packedLight, Color col, int baseEdges, float height, float radius) {

        int a = col.a;
        if (col.a <= 0) {
            return;
        }
        int r = col.r;
        int g = col.g;
        int b = col.b;
        PoseStack.Pose last = stack.last();
        Matrix4f pose = last.pose();

        Vector4f u = new Vector4f(0, height * 0.5F, 0, 1).mul(pose);
        Vector4f l = new Vector4f(0, height * -0.5F, 0, 1).mul(pose);
        Vector4f[] v = new Vector4f[baseEdges + 1];
        float angle = MathHelper.F_TAU / baseEdges;
        for (int i = 0; i < baseEdges; ++i) {
            v[i] = new Vector4f(radius * MathHelper.sin(i * angle), 0, radius * MathHelper.cos(i * angle), 1).mul(pose);
        }
        v[baseEdges] = v[0];
        for (int i = 0; i < baseEdges; ++i) {
            Vector4f v0 = v[i];
            Vector4f v1 = v[i + 1];
            consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(u.x(), u.y(), u.z()).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(u.x(), u.y(), u.z()).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);

            consumer.addVertex(v1.x(), v1.y(), v1.z()).setColor(r, g, b, a).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(v0.x(), v0.y(), v0.z()).setColor(r, g, b, a).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(l.x(), l.y(), l.z()).setColor(r, g, b, a).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
            consumer.addVertex(l.x(), l.y(), l.z()).setColor(r, g, b, a).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(last, 0, 1, 0);
        }
    }
    // endregion

}
