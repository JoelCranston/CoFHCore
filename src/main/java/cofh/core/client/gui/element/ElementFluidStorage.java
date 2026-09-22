package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.RenderHelper;
import cofh.lib.common.fluid.FluidStorageCoFH;
import cofh.lib.util.helpers.StringHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ElementFluidStorage extends ElementResourceStorage {

    protected FluidStorageCoFH tank;
    protected TextureAtlasSprite fluidTexture;

    public ElementFluidStorage(IGuiAccess gui, int posX, int posY, FluidStorageCoFH storage) {

        super(gui, posX, posY, storage);
        this.tank = storage;
    }

    public ElementFluidStorage setFluidTexture(TextureAtlasSprite fluidTexture) {

        this.fluidTexture = fluidTexture;
        return this;
    }

    @Override
    protected void drawResource(GuiGraphicsExtractor pGuiGraphics) {

        int resourceHeight = height - 2;
        int resourceWidth = width - 2;

        int amount = getScaled(resourceHeight);
        boolean invert = FluidHelper.density(tank.getFluidStack()) < 0;

        if (fluidTexture != null) {
            RenderHelper.drawTiledTexture(pGuiGraphics, posX() + 1, posY() + 1 + (invert ? 0 : resourceHeight - amount), fluidTexture, resourceWidth, amount);
        } else {
            RenderHelper.drawFluid(pGuiGraphics, posX() + 1, posY() + 1 + (invert ? 0 : resourceHeight - amount), tank.getFluidStack(), resourceWidth, amount);
        }
    }

    @Override
    protected void drawOverlayTexture(GuiGraphicsExtractor pGuiGraphics) {

        if (!drawOverlay.get()) {
            return;
        }
        if (storage.isCreative() && creativeTexture != null) {
            drawTexturedModalRect(pGuiGraphics, creativeTexture, posX(), posY(), 0, 0, width, height);
        } else {
            super.drawOverlayTexture(pGuiGraphics);
        }
    }

    @Override
    public void addTooltip(List<Component> tooltipList, int mouseX, int mouseY) {

        if (storage.getStored() > 0) {
            tooltipList.add(StringHelper.getFluidName(tank.getFluidStack()));
            if (FluidHelper.hasPotionTag(tank.getFluidStack())) {
                FluidHelper.addPotionTooltip(tank.getFluidStack(), tooltipList);
            }
        }
        super.addTooltip(tooltipList, mouseX, mouseY);
    }

}
