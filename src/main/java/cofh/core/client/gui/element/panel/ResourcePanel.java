package cofh.core.client.gui.element.panel;

import cofh.core.client.gui.IGuiAccess;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

import static cofh.lib.util.helpers.StringHelper.*;

public class ResourcePanel extends PanelBase {

    public static int defaultSide = LEFT;
    public static int defaultHeaderColor = 0xe1c92f;
    public static int defaultSubHeaderColor = 0xaaafb8;
    public static int defaultTextColor = 0x101010;
    public static int defaultBackgroundColorOut = 0xd0650b;
    public static int defaultBackgroundColorIn = 0x0a76d0;

    private Identifier icon;

    private String resource = "";

    private IntSupplier curAmt = () -> -1;
    private String curDesc = "";
    private String curUnit = "";

    private IntSupplier maxAmt = () -> -1;
    private String maxDesc = "";
    private String maxUnit = "";

    private DoubleSupplier efficiency = () -> -1;

    public ResourcePanel(IGuiAccess gui) {

        this(gui, defaultSide);
    }

    protected ResourcePanel(IGuiAccess gui, int sideIn) {

        super(gui, sideIn);

        headerColor = defaultHeaderColor;
        subheaderColor = defaultSubHeaderColor;
        textColor = defaultTextColor;
        backgroundColor = defaultBackgroundColorIn;

        maxHeight = 92;
        maxWidth = 100;

        setVisible(() -> !resource.isEmpty());
    }

    public ResourcePanel setResource(Identifier icon, String resource, boolean producer) {

        this.icon = icon;
        this.resource = resource;
        this.backgroundColor = producer ? defaultBackgroundColorOut : defaultBackgroundColorIn;
        return this;
    }

    public ResourcePanel setCurrent(IntSupplier curAmt, String curDesc, String curUnit) {

        this.curAmt = curAmt;
        this.curDesc = curDesc;
        this.curUnit = curUnit;
        return this;
    }

    public ResourcePanel setMax(IntSupplier maxAmt, String maxDesc, String maxUnit) {

        this.maxAmt = maxAmt;
        this.maxDesc = maxDesc;
        this.maxUnit = maxUnit;
        return this;
    }

    public ResourcePanel setEfficiency(DoubleSupplier efficiency) {

        this.efficiency = efficiency;
        return this;
    }

    @Override
    protected void drawForeground(GuiGraphicsExtractor pGuiGraphics) {

        drawPanelIcon(pGuiGraphics, icon);
        if (!fullyOpen) {
            return;
        }
        drawString(pGuiGraphics, localize(resource), sideOffset() + 20, 6, headerColor, true);

        if (curAmt.getAsInt() >= 0) {
            drawString(pGuiGraphics, localize(curDesc) + ":", sideOffset() + 6, 18, subheaderColor, true);
            drawString(pGuiGraphics, curAmt.getAsInt() + " " + localize(curUnit), sideOffset() + 14, 30, textColor, false);
        }
        if (maxAmt.getAsInt() >= 0) {
            drawString(pGuiGraphics, localize(maxDesc) + ":", sideOffset() + 6, 42, subheaderColor, true);
            drawString(pGuiGraphics, maxAmt.getAsInt() + " " + localize(maxUnit), sideOffset() + 14, 54, textColor, false);
        }
        if (efficiency.getAsDouble() >= 0) {
            drawString(pGuiGraphics, localize("info.cofh.efficiency") + ":", sideOffset() + 6, 66, subheaderColor, true);
            drawString(pGuiGraphics, DF0.format(efficiency.getAsDouble() * 100) + "%", sideOffset() + 14, 78, textColor, false);
        }
    }

    @Override
    public void addTooltip(List<Component> tooltipList, int mouseX, int mouseY) {

        if (!fullyOpen) {
            tooltipList.add(getTextComponent(curAmt.getAsInt() + " " + localize(curUnit)));
        }
    }

}
