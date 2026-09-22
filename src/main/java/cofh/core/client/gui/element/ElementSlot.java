package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

import static cofh.core.CoFHCore.LOG;
import static cofh.lib.util.Constants.TRUE;

public class ElementSlot extends ElementBase {

    protected Identifier underlayTexture;
    protected Identifier overlayTexture;

    protected Supplier<Boolean> drawUnderlay = TRUE;
    protected Supplier<Boolean> drawOverlay = TRUE;

    public ElementSlot(IGuiAccess gui, int posX, int posY) {

        super(gui, posX, posY);
    }

    public final ElementSlot setUnderlayTexture(String texture) {

        return setUnderlayTexture(texture, TRUE);
    }

    public final ElementSlot setUnderlayTexture(String texture, Supplier<Boolean> draw) {

        if (texture == null || draw == null) {
            LOG.warn("Attempted to assign a NULL underlay texture.");
            return this;
        }
        this.underlayTexture = Identifier.parse(texture);
        this.drawUnderlay = draw;
        return this;
    }

    public final ElementSlot setOverlayTexture(String texture) {

        return setOverlayTexture(texture, TRUE);
    }

    public final ElementSlot setOverlayTexture(String texture, Supplier<Boolean> draw) {

        if (texture == null || draw == null) {
            LOG.warn("Attempted to assign a NULL overlay texture.");
            return this;
        }
        this.overlayTexture = Identifier.parse(texture);
        this.drawOverlay = draw;
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        drawSlot(pGuiGraphics);
        drawUnderlayTexture(pGuiGraphics);
    }

    @Override
    public void drawForeground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        drawOverlayTexture(pGuiGraphics);
    }

    protected void drawSlot(GuiGraphicsExtractor pGuiGraphics) {

        drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, 0, width, height);
    }

    protected void drawUnderlayTexture(GuiGraphicsExtractor pGuiGraphics) {

        if (drawUnderlay.get() && underlayTexture != null) {
            drawTexturedModalRect(pGuiGraphics, underlayTexture, posX(), posY(), 0, 0, width, height);
        }
    }

    protected void drawOverlayTexture(GuiGraphicsExtractor pGuiGraphics) {

        if (drawOverlay.get() && overlayTexture != null) {
            drawTexturedModalRect(pGuiGraphics, overlayTexture, posX(), posY(), 0, 0, width, height);
        }
    }

}
