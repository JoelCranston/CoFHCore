package cofh.core.util.helpers;

import cofh.core.common.item.IAugmentableItem;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cofh.lib.util.Constants.MAX_AUGMENTS;
import static cofh.lib.util.constants.NBTTags.*;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;
import static net.minecraft.nbt.Tag.TAG_LIST;

public final class AugmentableHelper {

    private AugmentableHelper() {

    }

    public static boolean isAugmentableItem(ItemStack stack) {

        return !stack.isEmpty() && stack.getItem() instanceof IAugmentableItem;
    }

    public static List<ItemStack> readAugmentsFromItem(ItemStack stack) {

        ListTag augmentTag = getAugmentNBT(stack);
        if (augmentTag.isEmpty()) {
            return Collections.emptyList();
        }
        return getAugments(augmentTag);
    }

    public static void writeAugmentsToItem(ItemStack stack, List<ItemStack> augments) {

        writeAugmentsToItem(stack, convertAugments(augments));
    }

    // region AUGMENTABLE REDIRECTS
    public static List<ItemStack> getAugments(ItemStack augmentable) {

        return !isAugmentableItem(augmentable) ? Collections.emptyList() : ((IAugmentableItem) augmentable.getItem()).getAugments(augmentable);
    }

    public static int getAugmentSlots(ItemStack augmentable) {

        return !isAugmentableItem(augmentable) ? 0 : MathHelper.clamp(((IAugmentableItem) augmentable.getItem()).getAugmentSlots(augmentable), 0, MAX_AUGMENTS);
    }

    public static boolean validAugment(ItemStack augmentable, ItemStack augment, List<ItemStack> augments) {

        return isAugmentableItem(augmentable) && ((IAugmentableItem) augmentable.getItem()).validAugment(augmentable, augment, augments);
    }

    public static void setAugments(ItemStack stack, List<ItemStack> augments) {

        if (!isAugmentableItem(stack)) {
            return;
        }
        ((IAugmentableItem) stack.getItem()).setAugments(stack, augments);
    }
    // endregion

    // region ATTRIBUTES
    public static void setAttribute(CompoundTag subTag, String attribute, float value) {

        subTag.putFloat(attribute, value);
    }

    public static void setAttributeFromAugmentMax(CompoundTag subTag, CompoundTag augmentData, String attribute) {

        float mod = Math.max(getAttributeMod(augmentData, attribute), getAttributeMod(subTag, attribute));
        if (mod > 0.0F) {
            subTag.putFloat(attribute, mod);
        }
    }

    public static void setAttributeFromAugmentAdd(CompoundTag subTag, CompoundTag augmentData, String attribute) {

        float mod = getAttributeMod(augmentData, attribute) + getAttributeMod(subTag, attribute);
        subTag.putFloat(attribute, mod);
    }

    public static void setAttributeFromAugmentString(CompoundTag subTag, CompoundTag augmentData, String attribute) {

        String mod = getAttributeModString(augmentData, attribute);
        if (!mod.isEmpty()) {
            subTag.putString(attribute, mod);
        }
    }

    public static float getAttributeMod(CompoundTag augmentData, String key) {

        return augmentData.getFloat(key);
    }

    public static String getAttributeModString(CompoundTag augmentData, String key) {

        return augmentData.getString(key);
    }

    public static float getAttributeModWithDefault(CompoundTag augmentData, String key, float defaultValue) {

        return augmentData.contains(key) ? augmentData.getFloat(key) : defaultValue;
    }

    public static String getAttributeModWithDefault(CompoundTag augmentData, String key, String defaultValue) {

        return augmentData.contains(key) ? augmentData.getString(key) : defaultValue;
    }

    public static float getPropertyWithDefault(ItemStack container, String key, float defaultValue) {

        return getAttributeModWithDefault(ItemHelper.getCustomSubTag(container, TAG_PROPERTIES), key, defaultValue);
    }

    public static String getPropertyWithDefault(ItemStack container, String key, String defaultValue) {

        return getAttributeModWithDefault(ItemHelper.getCustomSubTag(container, TAG_PROPERTIES), key, defaultValue);
    }

    // endregion

    // region INTERNAL HELPERS
    // Block items keep augments in their block entity data, which is restored on placement.
    private static void writeAugmentsToItem(ItemStack stack, ListTag list) {

        if (stack.getItem() instanceof BlockItem || ItemHelper.hasCustomSubTag(stack, TAG_BLOCK_ENTITY)) {
            CompoundTag nbt = ItemHelper.getBlockEntityData(stack);
            nbt.put(TAG_AUGMENTS, list);
            ItemHelper.setBlockEntityData(stack, nbt);
            return;
        }
        ItemHelper.setCustomSubTag(stack, TAG_AUGMENTS, list);
    }

    private static List<ItemStack> getAugments(ListTag list) {

        HolderLookup.Provider provider = ProxyUtils.registryAccess();
        ArrayList<ItemStack> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); ++i) {
            ret.add(ItemStack.parseOptional(provider, list.getCompound(i)));
        }
        return ret.isEmpty() ? Collections.emptyList() : ret;
    }

    private static ListTag getAugmentNBT(ItemStack stack) {

        CompoundTag blockEntityData = ItemHelper.getBlockEntityData(stack);
        if (blockEntityData.contains(TAG_AUGMENTS, TAG_LIST)) {
            return blockEntityData.getList(TAG_AUGMENTS, TAG_COMPOUND);
        }
        return ItemHelper.getCustomData(stack).getList(TAG_AUGMENTS, TAG_COMPOUND);
    }

    private static ListTag convertAugments(List<ItemStack> augments) {

        ListTag list = new ListTag();
        for (ItemStack augment : augments) {
            // Empty slots are intentionally written.
            //if (!augment.isEmpty()) {
            list.add(augment.isEmpty() ? new CompoundTag() : augment.save(ProxyUtils.registryAccess()));
            //}
        }
        return list;
    }
    // endregion
}
