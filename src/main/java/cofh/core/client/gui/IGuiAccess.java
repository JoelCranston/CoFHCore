package cofh.core.client.gui;

import cofh.core.util.helpers.RenderHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

public interface IGuiAccess {

    int guiTop();

    int guiLeft();

    Font fontRenderer();

    Player player();

    int blitOffset();

    default void drawSprite(GuiGraphics pGuiGraphics, TextureAtlasSprite sprite, int x, int y) {

        RenderHelper.setPosTexShader();
        RenderHelper.setBlockTextureSheet();
        RenderHelper.resetShaderColor();
        pGuiGraphics.blit(x, y, blitOffset(), 16, 16, sprite);
    }

    default void drawSprite(GuiGraphics pGuiGraphics, TextureAtlasSprite sprite, int color, int x, int y) {

        RenderHelper.setPosTexShader();
        RenderHelper.setBlockTextureSheet();
        RenderHelper.setShaderColorFromInt(color);
        pGuiGraphics.blit(x, y, blitOffset(), 16, 16, sprite);
        RenderHelper.resetShaderColor();
    }

    default void drawIcon(GuiGraphics pGuiGraphics, Identifier texture, int x, int y) {

        RenderHelper.setPosTexShader();
        RenderHelper.setShaderTexture0(texture);
        RenderHelper.resetShaderColor();
        drawTexturedModalRect(pGuiGraphics.pose(), x, y, 0, 0, 16, 16, 16, 16);
    }

    default void drawIcon(GuiGraphics pGuiGraphics, Identifier texture, int color, int x, int y) {

        RenderHelper.setPosTexShader();
        RenderHelper.setShaderTexture0(texture);
        RenderHelper.setShaderColorFromInt(color);
        drawTexturedModalRect(pGuiGraphics.pose(), x, y, 0, 0, 16, 16, 16, 16);
        RenderHelper.resetShaderColor();
    }

    default void drawSizedRect(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {

        if (x1 < x2) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 < y2) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }

        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(r, g, b, a);

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.addVertex(mat, (float) (x1), (float) (y2), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x2), (float) (y2), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x2), (float) (y1), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x1), (float) (y1), (float) (blitOffset()));
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    default void drawColoredModalRect(PoseStack poseStack, int x1, int y1, int x2, int y2, int color) {

        int temp;
        if (x1 < x2) {
            temp = x1;
            x1 = x2;
            x2 = temp;
        }
        if (y1 < y2) {
            temp = y1;
            y1 = y2;
            y2 = temp;
        }
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(r, g, b, a);

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.addVertex(mat, (float) (x1), (float) (y2), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x2), (float) (y2), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x2), (float) (y1), (float) (blitOffset()));
        buffer.addVertex(mat, (float) (x1), (float) (y1), (float) (blitOffset()));
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    default void drawTexturedModalRect(GuiGraphics guiGraphics, int x, int y, int textureX, int textureY, int width, int height) {

        drawTexturedModalRect(guiGraphics.pose(), x, y, textureX, textureY, width, height);
    }

    default void drawTexturedModalRect(PoseStack poseStack, int x, int y, int textureX, int textureY, int width, int height) {

        final float f = 0.00390625F;
        Tesselator tesselator = Tesselator.getInstance();

        Matrix4f mat = poseStack.last().pose();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(mat, (float) (x), (float) ((y + height)), (float) (blitOffset())).setUv(((float) textureX * f), ((float) (textureY + height) * f));
        bufferbuilder.addVertex(mat, (float) ((x + width)), (float) ((y + height)), (float) (blitOffset())).setUv(((float) (textureX + width) * f), ((float) (textureY + height) * f));
        bufferbuilder.addVertex(mat, (float) ((x + width)), (float) (y), (float) (blitOffset())).setUv(((float) (textureX + width) * f), ((float) textureY * f));
        bufferbuilder.addVertex(mat, (float) (x), (float) (y), (float) (blitOffset())).setUv(((float) textureX * f), ((float) textureY * f));
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    default void drawTexturedModalRect(GuiGraphics guiGraphics, int x, int y, int u, int v, int width, int height, float texW, float texH) {

        drawTexturedModalRect(guiGraphics.pose(), x, y, u, v, width, height, texW, texH);
    }

    default void drawTexturedModalRect(PoseStack poseStack, int x, int y, int u, int v, int width, int height, float texW, float texH) {

        float texU = 1 / texW;
        float texV = 1 / texH;
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        Matrix4f mat = poseStack.last().pose();
        buffer.addVertex(mat, (float) (x), (float) (y + height), (float) (blitOffset())).setUv((u) * texU, (v + height) * texV);
        buffer.addVertex(mat, (float) (x + width), (float) (y + height), (float) (blitOffset())).setUv((u + width) * texU, (v + height) * texV);
        buffer.addVertex(mat, (float) (x + width), (float) (y), (float) (blitOffset())).setUv((u + width) * texU, (v) * texV);
        buffer.addVertex(mat, (float) (x), (float) (y), (float) (blitOffset())).setUv((u) * texU, (v) * texV);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

}
