package cofh.core.client.gui.element;

import cofh.core.client.gui.GuiColor;
import cofh.core.client.gui.IGuiAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import static cofh.lib.util.Constants.PATH_ELEMENTS;

public abstract class ElementSlider extends ElementBase {

    public static final Identifier HOVER = Identifier.parse(PATH_ELEMENTS + "button_hover.png");
    public static final Identifier ENABLED = Identifier.parse(PATH_ELEMENTS + "button_enabled.png");
    public static final Identifier DISABLED = Identifier.parse(PATH_ELEMENTS + "button_disabled.png");

    protected int value;
    protected int valueMin;
    protected int valueMax;
    protected int sliderWidth;
    protected int sliderHeight;

    protected boolean isDragging;

    public int borderColor = new GuiColor(120, 120, 120, 255).getColor();
    public int backgroundColor = new GuiColor(0, 0, 0, 255).getColor();

    protected ElementSlider(IGuiAccess containerScreen, int x, int y, int width, int height, int maxValue) {

        this(containerScreen, x, y, width, height, maxValue, 0);
    }

    protected ElementSlider(IGuiAccess containerScreen, int x, int y, int width, int height, int maxValue, int minValue) {

        super(containerScreen, x, y, width, height);
        valueMax = maxValue;
        valueMin = minValue;
    }

    public ElementSlider setColor(int backgroundColor, int borderColor) {

        this.borderColor = borderColor;
        this.backgroundColor = backgroundColor;
        return this;
    }

    public ElementSlider setSliderSize(int width, int height) {

        sliderWidth = width;
        sliderHeight = height;
        return this;
    }

    public ElementSlider setValue(int value) {

        value = Math.max(valueMin, Math.min(valueMax, value));
        if (value != this.value) {
            this.value = value;
            onValueChanged(this.value);
        }
        return this;
    }

    public ElementSlider setLimits(int min, int max) {

        valueMin = min;
        valueMax = max;
        setValue(value);
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        drawColoredModalRect(pGuiGraphics, posX() - 1, posY() - 1, posX() + width + 1, posY() + height + 1, borderColor);
        drawColoredModalRect(pGuiGraphics, posX(), posY(), posX() + width, posY() + height, backgroundColor);
    }

    protected void drawSlider(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY, int sliderX, int sliderY) {

        int sliderMidX = sliderWidth / 2;
        int sliderMidY = sliderHeight / 2;
        int sliderEndX = sliderWidth - sliderMidX;
        int sliderEndY = sliderHeight - sliderMidY;

        Identifier sliderTexture;
        if (!enabled()) {
            sliderTexture = DISABLED;
        } else if (isHovering(mouseX, mouseY)) {
            sliderTexture = HOVER;
        } else {
            sliderTexture = ENABLED;
        }
        drawTexturedModalRect(pGuiGraphics, sliderTexture, sliderX, sliderY, 0, 0, sliderMidX, sliderMidY);
        drawTexturedModalRect(pGuiGraphics, sliderTexture, sliderX, sliderY + sliderMidY, 0, 256 - sliderEndY, sliderMidX, sliderEndY);
        drawTexturedModalRect(pGuiGraphics, sliderTexture, sliderX + sliderMidX, sliderY, 256 - sliderEndX, 0, sliderEndX, sliderMidY);
        drawTexturedModalRect(pGuiGraphics, sliderTexture, sliderX + sliderMidX, sliderY + sliderMidY, 256 - sliderEndX, 256 - sliderEndY, sliderEndX, sliderEndY);
    }

    @Override
    public void drawForeground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        int sliderX = posX() + getSliderX();
        int sliderY = posY() + getSliderY();

        drawSlider(pGuiGraphics, mouseX, mouseY, sliderX, sliderY);
    }

    protected boolean isHovering(int x, int y) {

        return intersectsWith(x, y);
    }

    public int getSliderX() {

        return 0;
    }

    public int getSliderY() {

        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        isDragging = mouseButton == 0;
        update((int) mouseX, (int) mouseY);
        return true;
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY) {

        if (isDragging) {
            onStopDragging();
        }
        isDragging = false;
    }

    @Override
    public void update(int mouseX, int mouseY) {

        if (isDragging) {
            dragSlider(mouseX - posX(), mouseY - posY());
        }
    }

    protected abstract void dragSlider(int x, int y);

    @Override
    public boolean mouseWheel(double mouseX, double mouseY, double movement) {

        if (movement > 0) {
            setValue(value - 1);
        } else if (movement < 0) {
            setValue(value + 1);
        }
        return true;
    }

    public void onValueChanged(int value) {

    }

    public void onStopDragging() {

    }

    public int getValue() {

        return value;
    }

}
