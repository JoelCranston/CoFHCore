package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ElementButton extends ElementBase {

    public ElementButton(IGuiAccess gui, int posX, int posY) {

        super(gui, posX, posY);
    }

    public ElementButton(IGuiAccess gui, int posX, int posY, int width, int height) {

        super(gui, posX, posY, width, height);
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, 0, width, height);

        if (enabled()) {
            if (intersectsWith(mouseX, mouseY)) {
                drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), width, 0, width, height);
            } else {
                drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, 0, width, height);
            }
        } else {
            drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), width * 2, 0, width, height);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        return enabled();
    }

}
