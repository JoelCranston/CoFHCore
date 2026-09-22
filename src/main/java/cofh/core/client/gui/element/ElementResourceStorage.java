package cofh.core.client.gui.element;

import cofh.core.client.gui.IGuiAccess;
import cofh.lib.api.IResourceStorage;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.function.Supplier;

import static cofh.core.CoFHCore.LOG;
import static cofh.lib.util.Constants.FALSE;
import static cofh.lib.util.Constants.TRUE;
import static cofh.lib.util.helpers.StringHelper.format;

public abstract class ElementResourceStorage extends ElementBase {

    protected Identifier creativeTexture;
    protected Identifier underlayTexture;
    protected Identifier overlayTexture;

    protected IResourceStorage storage;
    protected int minDisplay = 1;

    protected Supplier<Boolean> drawStorage = TRUE;
    protected Supplier<Boolean> drawUnderlay = TRUE;
    protected Supplier<Boolean> drawOverlay = TRUE;

    protected Supplier<Boolean> claimStorage = FALSE;
    protected Supplier<Boolean> clearStorage = FALSE;
    protected Supplier<Boolean> claimable = FALSE;
    protected Supplier<Boolean> clearable;

    public ElementResourceStorage(IGuiAccess gui, int posX, int posY, IResourceStorage storage) {

        super(gui, posX, posY);
        this.storage = storage;

        this.clearable = () -> storage.getStored() > 0;
    }

    public final ElementResourceStorage setCreativeTexture(String texture) {

        if (texture == null) {
            LOG.warn("Attempted to assign a NULL creative texture.");
            return this;
        }
        this.creativeTexture = Identifier.parse(texture);
        return this;
    }

    public final ElementResourceStorage setUnderlayTexture(String texture) {

        return setUnderlayTexture(texture, TRUE);
    }

    public final ElementResourceStorage setUnderlayTexture(String texture, Supplier<Boolean> draw) {

        if (texture == null || draw == null) {
            LOG.warn("Attempted to assign a NULL underlay texture.");
            return this;
        }
        this.underlayTexture = Identifier.parse(texture);
        this.drawUnderlay = draw;
        return this;
    }

    public final ElementResourceStorage setOverlayTexture(String texture) {

        return setOverlayTexture(texture, TRUE);
    }

    public final ElementResourceStorage setOverlayTexture(String texture, Supplier<Boolean> draw) {

        if (texture == null || draw == null) {
            LOG.warn("Attempted to assign a NULL overlay texture.");
            return this;
        }
        this.overlayTexture = Identifier.parse(texture);
        this.drawOverlay = draw;
        return this;
    }

    public final ElementResourceStorage setClaimStorage(Supplier<Boolean> claimStorage) {

        this.claimStorage = claimStorage;
        return this;
    }

    public final ElementResourceStorage setClearStorage(Supplier<Boolean> clearStorage) {

        this.clearStorage = clearStorage;
        return this;
    }

    public ElementResourceStorage setMinDisplay(int minDisplay) {

        this.minDisplay = minDisplay;
        return this;
    }

    @Override
    public void drawBackground(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {

        drawStorage(pGuiGraphics);
        drawUnderlayTexture(pGuiGraphics);
        drawResource(pGuiGraphics);
        drawOverlayTexture(pGuiGraphics);
    }

    @Override
    public void addTooltip(List<Component> tooltipList, int mouseX, int mouseY) {

        if (storage.isCreative()) {
            tooltipList.add(Component.translatable("info.cofh.infinite").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.ITALIC));
        } else {
            tooltipList.add(Component.literal(format(storage.getStored()) + " / " + format(storage.getCapacity()) + " " + storage.getUnit()));
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (clearable.get() && clearStorage != FALSE && (minecraft.hasAltDown() || minecraft.hasShiftDown())) {
            tooltipList.add(Component.translatable("info.cofh.click_to_clear").withStyle(ChatFormatting.GRAY));
        } else if (claimable.get()) {
            tooltipList.add(Component.translatable("info.cofh.click_to_claim").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        Minecraft minecraft = Minecraft.getInstance();
        if (clearable.get() && minecraft.hasShiftDown() && minecraft.hasAltDown()) {
            return clearStorage.get();
        }
        if (claimable.get()) {
            return claimStorage.get();
        }
        return false;
    }

    protected int getScaled(int scale) {

        if (storage.isCreative()) {
            return scale;
        }
        double fraction = (double) storage.getStored() * scale / storage.getCapacity();
        int amount = MathHelper.clamp(MathHelper.round(fraction), 0, scale);
        return fraction > 0 ? Math.max(minDisplay, amount) : amount;
    }

    protected void drawStorage(GuiGraphicsExtractor pGuiGraphics) {

        if (drawStorage.get() && texture != null) {
            drawTexturedModalRect(pGuiGraphics, texture, posX(), posY(), 0, 0, width, height);
        }
    }

    protected void drawUnderlayTexture(GuiGraphicsExtractor pGuiGraphics) {

        if (drawUnderlay.get() && underlayTexture != null) {
            drawTexturedModalRect(pGuiGraphics, underlayTexture, posX(), posY(), 0, 0, width, height);
        }
    }

    protected abstract void drawResource(GuiGraphicsExtractor pGuiGraphics);

    protected void drawOverlayTexture(GuiGraphicsExtractor pGuiGraphics) {

        if (drawOverlay.get() && overlayTexture != null) {
            drawTexturedModalRect(pGuiGraphics, overlayTexture, posX(), posY(), 0, 0, width, height);
        }
    }

}
