package cofh.core.common.fluid;

import cofh.core.util.helpers.FluidHelper;
import cofh.lib.common.fluid.FluidCoFH;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collection;
import java.util.Optional;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static cofh.core.CoFHCore.FLUIDS;
import static cofh.core.CoFHCore.FLUID_TYPES;
import static cofh.core.util.references.CoreIDs.ID_FLUID_POTION;
import static cofh.lib.util.constants.NBTTags.TAG_POTION;

public class PotionFluid extends FluidCoFH {

    private static PotionFluid INSTANCE;

    public static PotionFluid create() {

        if (INSTANCE == null) {
            INSTANCE = new PotionFluid();
        }
        return INSTANCE;
    }

    protected PotionFluid() {

        super(FLUIDS, ID_FLUID_POTION);

        // This is only used for testing.
        // bucket = toolsTab(1000, ITEMS.register(bucket(key), () -> new BucketItem(stillFluid, properties().containerItem(Items.BUCKET).maxStackSize(1).group(ItemGroup.BREWING)));
    }

    @Override
    protected BaseFlowingFluid.Properties fluidProperties() {

        return new BaseFlowingFluid.Properties(type(), stillFluid, flowingFluid);
    }

    @Override
    protected Supplier<FluidType> type() {

        return TYPE;
    }

    public static final DeferredHolder<FluidType, FluidType> TYPE = FLUID_TYPES.register(ID_FLUID_POTION, () -> new FluidType(FluidType.Properties.create()
            .density(1100)
            .viscosity(1100)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BOTTLE_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BOTTLE_EMPTY)) {

        @Override
        public Component getDescription(FluidStack stack) {

            Optional<Holder<Potion>> potion = FluidHelper.getPotionContents(stack).potion();
            if (potion.isEmpty() || potion.get().is(Potions.WATER)) {
                return super.getDescription(stack);
            }
            // Potion.getName(Optional<Holder>, prefix) builds the whole key, suffix included.
            return Component.translatable(Potion.getName(potion, Items.POTION.getDescriptionId() + ".effect."));
        }

        @Override
        public Rarity getRarity(FluidStack stack) {

            return FluidHelper.getPotionContents(stack).hasEffects() ? Rarity.UNCOMMON : Rarity.COMMON;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {

            consumer.accept(new IClientFluidTypeExtensions() {

                private static final ResourceLocation
                        STILL = ResourceLocation.parse("cofh_core:block/fluids/potion_still"),
                        FLOW = ResourceLocation.parse("cofh_core:block/fluids/potion_flow");

                @Override
                public int getTintColor(FluidStack stack) {

                    return 0xFF000000 | getPotionColor(stack);
                }

                @Override
                public ResourceLocation getStillTexture() {

                    return STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {

                    return FLOW;
                }
            });
        }
    });

    // region HELPERS
    public static int DEFAULT_COLOR = 0xF800F8;

    public static int getPotionColor(FluidStack stack) {

        PotionContents contents = FluidHelper.getPotionContents(stack);
        // getColor() already prefers an explicit customColor over the effects' blend.
        return contents.potion().isEmpty() && contents.customEffects().isEmpty() ? DEFAULT_COLOR : contents.getColor();
    }

    public static FluidStack getPotionAsFluid(int amount, Holder<Potion> type, boolean hasCustom) {

        if (type == null) {
            return FluidStack.EMPTY;
        }
        if (type.is(Potions.WATER) && !hasCustom) {
            return new FluidStack(Fluids.WATER, amount);
        }
        return addPotionToFluidStack(new FluidStack(INSTANCE.stillFluid.get(), amount), type);
    }

    public static FluidStack getPotionAsFluid(int amount, Holder<Potion> type) {

        return getPotionAsFluid(amount, type, false);
    }

    public static FluidStack addPotionToFluidStack(FluidStack stack, Holder<Potion> type) {

        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(type));
        return stack;
    }

    public static FluidStack setCustomEffects(FluidStack stack, Collection<MobEffectInstance> effects) {

        if (stack.isEmpty() || effects.isEmpty()) {
            return stack;
        }
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        for (MobEffectInstance effect : effects) {
            contents = contents.withEffectAdded(effect);
        }
        stack.set(DataComponents.POTION_CONTENTS, contents);
        return stack;
    }

    public static Collection<MobEffectInstance> getCustomEffects(FluidStack stack) {

        return stack.isEmpty() ? Collections.emptyList() : stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).customEffects();
    }

    public static FluidStack setCustomColor(FluidStack stack, int color) {

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(contents.potion(), Optional.of(color), contents.customEffects()));
        return stack;
    }

    public static ItemStack setCustomColor(ItemStack stack, int color) {

        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(contents.potion(), Optional.of(color), contents.customEffects()));
        return stack;
    }

    public static FluidStack getPotionFluidFromItem(int amount, ItemStack stack) {

        Item item = stack.getItem();

        if (item.equals(Items.POTION)) {
            // The whole potion is one component now, so the conversion is a straight copy - no
            // more picking the potion id, custom effects and custom colour apart by hand.
            PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
            if (contents.potion().isEmpty() && contents.customEffects().isEmpty()) {
                return FluidStack.EMPTY;
            }
            FluidStack fluid = getPotionAsFluid(amount, contents.potion().orElse(Potions.WATER), !contents.customEffects().isEmpty());
            if (!fluid.isEmpty()) {
                fluid.set(DataComponents.POTION_CONTENTS, contents);
                copyDisplay(stack, fluid);
            }
            return fluid;
        }
        return FluidStack.EMPTY;
    }

    public static ItemStack getItemFromPotionFluid(FluidStack fluid) {

        ItemStack stack = new ItemStack(Items.POTION);
        stack.set(DataComponents.POTION_CONTENTS, FluidHelper.getPotionContents(fluid));
        copyDisplay(fluid, stack);
        return stack;
    }
    /**
     * The old conversion copied the "display" and "HideFlags" NBT keys across; those are the
     * CUSTOM_NAME and TOOLTIP_DISPLAY components now.
     */
    private static void copyDisplay(DataComponentHolder from, MutableDataComponentHolder to) {

        copyComponent(from, to, DataComponents.CUSTOM_NAME);
        copyComponent(from, to, DataComponents.HIDE_ADDITIONAL_TOOLTIP);
    }

    private static <T> void copyComponent(DataComponentHolder from, MutableDataComponentHolder to, DataComponentType<T> type) {

        T value = from.get(type);
        if (value != null) {
            to.set(type, value);
        }
    }
    // endregion

    //    protected static class PotionFluidAttributes extends FluidAttributes {
    //
    //        protected PotionFluidAttributes(Builder builder, Fluid fluid) {
    //
    //            super(builder, fluid);
    //        }
    //
    //        @Override
    //        public Component getDisplayName(FluidStack stack) {
    //
    //            Potion potion = PotionUtils.getPotion(stack.getTag());
    //            if (potion == Potions.EMPTY || potion == Potions.WATER) {
    //                return super.getDisplayName(stack);
    //            }
    //            return new Component.translatable(potion.getName(Items.POTION.getDescriptionId() + ".effect."));
    //        }
    //
    //        public Rarity getRarity(FluidStack stack) {
    //
    //            return FluidHelper.getPotionContents(stack).hasEffects() ? Rarity.UNCOMMON : Rarity.COMMON;
    //        }
    //
    //        @Override
    //        public int getColor(FluidStack stack) {
    //
    //            return 0xFF000000 | getPotionColor(stack);
    //        }
    //
    //        public static Builder builder(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
    //
    //            return new Builder(stillTexture, flowingTexture, PotionFluidAttributes::new) {};
    //        }
    //
    //    }

}
