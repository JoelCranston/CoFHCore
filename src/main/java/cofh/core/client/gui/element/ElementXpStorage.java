package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.lib.common.xp.XpStorage;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import static cofh.lib.util.Constants.FALSE;

public class ElementXpStorage extends ElementResourceStorage {

    public ElementXpStorage(IGuiAccess gui, int posX, int posY, XpStorage storage) {

        super(gui, posX, posY, storage);
        drawStorage = FALSE;
        minDisplay = 0;

        this.claimable = () -> storage.getStored() > 0;
    }

    @Override
    protected void drawResource(GuiGraphicsExtractor pGuiGraphics) {

        int amount = storage.getStored() <= 0 ? 0 : Math.min(getScaled(4) + 1, 4);
        drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, amount * height, width, height);
    }

}
