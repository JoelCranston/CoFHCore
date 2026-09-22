package cofh.core.client.gui.element.listbox;

import cofh.core.client.gui.element.ElementListBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IListBoxElement {

    int getHeight();

    int getWidth();

    Object getValue();

    void draw(GuiGraphicsExtractor pGuiGraphics, ElementListBox listBox, int x, int y, int backColor, int textColor);

}
