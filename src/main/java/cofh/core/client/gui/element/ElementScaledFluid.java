package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.core.util.helpers.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.function.Supplier;

public class ElementScaledFluid extends ElementScaled {

    protected Supplier<FluidStack> fluidSup;

    public ElementScaledFluid(IGuiAccess gui, int posX, int posY) {

        super(gui, posX, posY);
    }

    public ElementScaledFluid setFluid(Supplier<FluidStack> sup) {

        this.fluidSup = sup;
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        int quantity = quantitySup.getAsInt();
        FluidStack fluid = fluidSup.get();

        if (drawBackground) {
            drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, 0, width, height);
        }
        switch (direction) {
            case TOP:
                // vertical top -> bottom
                RenderHelper.drawFluid(pGuiGraphics, posX(), posY(), fluid, width, quantity);
                drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), width, 0, width, quantity);
                return;
            case BOTTOM:
                // vertical bottom -> top
                RenderHelper.drawFluid(pGuiGraphics, posX(), posY() + height - quantity, fluid, width, quantity);
                drawTexturedModalRect(pGuiGraphics, texture, posX(), posY() + height - quantity, width, height - quantity, width, quantity);
                return;
            case LEFT:
                // horizontal left -> right
                RenderHelper.drawFluid(pGuiGraphics, posX(), posY(), fluid, quantity, height);
                drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), width, 0, quantity, height);
                return;
            case RIGHT:
                // horizontal right -> left
                RenderHelper.drawFluid(pGuiGraphics, posX() + width - quantity, posY(), fluid, quantity, height);
                drawTexturedModalRect(pGuiGraphics, texture, posX() + width - quantity, posY(), width + width - quantity, 0, quantity, height);
        }
    }

}
