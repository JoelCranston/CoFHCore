package cofh.core.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;

public interface IGuiAccess {

    int guiTop();

    int guiLeft();

    Font fontRenderer();

    Player player();

    default void drawSprite(GuiGraphicsExtractor pGuiGraphics, TextureAtlasSprite sprite, int x, int y) {

        pGuiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
    }

    default void drawSprite(GuiGraphicsExtractor pGuiGraphics, TextureAtlasSprite sprite, int color, int x, int y) {

        pGuiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16, ARGB.opaque(color));
    }

    default void drawIcon(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int x, int y) {

        drawTexturedModalRect(pGuiGraphics, texture, x, y, 0, 0, 16, 16, 16, 16);
    }

    default void drawIcon(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int color, int x, int y) {

        drawTexturedModalRect(pGuiGraphics, texture, x, y, 0, 0, 16, 16, 16, 16, color);
    }

    default void drawSizedRect(GuiGraphicsExtractor pGuiGraphics, int x1, int y1, int x2, int y2, int color) {

        pGuiGraphics.fill(x1, y1, x2, y2, color);
    }

    default void drawColoredModalRect(GuiGraphicsExtractor pGuiGraphics, int x1, int y1, int x2, int y2, int color) {

        pGuiGraphics.fill(x1, y1, x2, y2, color);
    }

    default void drawTexturedModalRect(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int x, int y, int textureX, int textureY, int width, int height) {

        drawTexturedModalRect(pGuiGraphics, texture, x, y, textureX, textureY, width, height, 256, 256);
    }

    default void drawTexturedModalRect(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int x, int y, int textureX, int textureY, int width, int height, int color) {

        drawTexturedModalRect(pGuiGraphics, texture, x, y, textureX, textureY, width, height, 256, 256, color);
    }

    default void drawTexturedModalRect(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int x, int y, int u, int v, int width, int height, int texW, int texH) {

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texW, texH);
    }

    default void drawTexturedModalRect(GuiGraphicsExtractor pGuiGraphics, Identifier texture, int x, int y, int u, int v, int width, int height, int texW, int texH, int color) {

        pGuiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, texW, texH, ARGB.opaque(color));
    }

    default void drawString(GuiGraphicsExtractor pGuiGraphics, String text, int x, int y, int color, boolean dropShadow) {

        pGuiGraphics.text(fontRenderer(), text, x, y, textColor(color), dropShadow);
    }

    default void drawString(GuiGraphicsExtractor pGuiGraphics, FormattedCharSequence text, int x, int y, int color, boolean dropShadow) {

        pGuiGraphics.text(fontRenderer(), text, x, y, textColor(color), dropShadow);
    }

    // Text without an alpha byte is not drawn.
    static int textColor(int color) {

        return (color & 0xFC000000) == 0 ? color | 0xFF000000 : color;
    }

}
