package cofh.core.common.item;

import cofh.core.common.config.CoreClientConfig;
import cofh.lib.api.item.ICoFHItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.FuelValues;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static cofh.lib.util.Constants.TRUE;
import static cofh.lib.util.helpers.StringHelper.getTextComponent;
import static net.minecraft.ChatFormatting.GRAY;

public class BlockItemCoFH extends BlockItem implements ICoFHItem {

    protected Supplier<Boolean> showInGroups = TRUE;

    protected int burnTime = -1;
    protected String modId = "";

    protected Supplier<CreativeModeTab> displayGroup;

    public BlockItemCoFH(Block blockIn, Properties builder) {

        super(blockIn, builder);
    }

    public BlockItemCoFH setBurnTime(int burnTime) {

        this.burnTime = burnTime;
        return this;
    }

    public BlockItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    protected void tooltipDelegate(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }

    //    @Override
    //    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
    //
    //        if (!showInGroups.get() || getBlock() == null || displayGroup != null && displayGroup.get() != null && displayGroup.get() != group) {
    //            return;
    //        }
    //        super.fillItemCategory(group, items);
    //    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {

        List<Component> additionalTooltips = new ArrayList<>();
        tooltipDelegate(stack, context.level(), additionalTooltips, flagIn);

        if (!additionalTooltips.isEmpty()) {
            if (Minecraft.getInstance().hasShiftDown() || CoreClientConfig.alwaysShowDetails.get()) {
                additionalTooltips.forEach(tooltip);
            } else if (CoreClientConfig.holdShiftForDetails.get()) {
                tooltip.accept(getTextComponent("info.cofh.hold_shift_for_details").withStyle(GRAY));
            }
        }
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType, FuelValues fuelValues) {

        // Negative burn time throws; defer to the furnace_fuels data map.
        return burnTime < 0 ? super.getBurnTime(itemStack, recipeType, fuelValues) : burnTime;
    }

    //    @Override
    //    public Collection<CreativeModeTab> getCreativeTabs() {
    //
    //        return displayGroup != null && displayGroup.get() != null ? Collections.singletonList(displayGroup.get()) : super.getCreativeTabs();
    //    }

}
