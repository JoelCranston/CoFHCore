package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.lib.common.inventory.ItemStorageCoFH;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class ElementItemStorage extends ElementResourceStorage {

    public ElementItemStorage(IGuiAccess gui, int posX, int posY, ItemStorageCoFH storage) {

        super(gui, posX, posY, storage);
        clearable = () -> !storage.isCreative();
    }

    @Override
    protected void drawResource(GuiGraphicsExtractor pGuiGraphics) {

        Identifier resourceTexture = storage.isCreative() && creativeTexture != null ? creativeTexture : texture;
        int resourceHeight = height - 2;
        int amount = getScaled(resourceHeight);
        drawTexturedModalRect(pGuiGraphics, resourceTexture, posX(), posY() + 1 + resourceHeight - amount, width, 1 + resourceHeight - amount, width, amount);
    }

}
