package cofh.core.util.helpers;

import cofh.core.common.item.ILeftClickHandlerItem;
import cofh.core.common.item.IMultiModeItem;
import com.google.common.base.Strings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ItemHelper {

    private ItemHelper() {

    }

    public static ItemStack consumeItem(ItemStack stack, int amount) {

        if (amount <= 0) {
            return stack;
        }
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Item item = stack.getItem();
        boolean largerStack = stack.getItem().getMaxStackSize(stack) > 1;
        // vanilla only alters the stack passed to hasContainerItem/etc. when the size is > 1

        if (largerStack) {
            stack.shrink(amount);
            if (stack.isEmpty()) {
                stack = ItemStack.EMPTY;
            }
        } else if (item.hasCraftingRemainingItem(stack)) {
            ItemStack ret = item.getCraftingRemainingItem(stack);
            if (ret.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (ret.isDamageableItem() && ret.getDamageValue() > ret.getMaxDamage()) {
                ret = ItemStack.EMPTY;
            }
            return ret;
        }
        return largerStack ? stack : ItemStack.EMPTY;
    }

    // region CLONESTACK
    public static ItemStack cloneStack(Item item) {

        return cloneStack(item, 1);
    }

    public static ItemStack cloneStack(Block block) {

        return cloneStack(block, 1);
    }

    public static ItemStack cloneStack(Item item, int stackSize) {

        if (item == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, stackSize);
    }

    public static ItemStack cloneStack(Block block, int stackSize) {

        if (block == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(block, stackSize);
    }

    public static ItemStack cloneStack(ItemStack stack, int stackSize) {

        if (stack.isEmpty() || stackSize <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack retStack = stack.copy();
        retStack.setCount(stackSize);

        return retStack;
    }

    public static ItemStack cloneStack(ItemStack stack) {

        return stack.isEmpty() ? ItemStack.EMPTY : stack.copy();
    }
    // endregion

    // region NBT TAGS
    // "Tag" here always meant the mod-attached custom NBT blob, not vanilla's structured item
    // data - that's DataComponents.CUSTOM_DATA now (ItemStack#hasTag/getTag/setTag are gone).
    public static ItemStack copyTag(ItemStack container, ItemStack other) {

        if (!other.isEmpty() && other.has(DataComponents.CUSTOM_DATA)) {
            container.set(DataComponents.CUSTOM_DATA, CustomData.of(other.get(DataComponents.CUSTOM_DATA).copyTag()));
        }
        return container;
    }

    /**
     * The mod-attached NBT blob, as a copy. {@link CustomData} is immutable and hands out copies -
     * mutating what this returns does not touch the stack. Empty rather than null when absent.
     */
    public static CompoundTag getCustomData(ItemStack stack) {

        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean hasCustomData(ItemStack stack) {

        return stack.has(DataComponents.CUSTOM_DATA) && !stack.get(DataComponents.CUSTOM_DATA).isEmpty();
    }

    public static void setCustomData(ItemStack stack, CompoundTag tag) {

        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Read-modify-write of the mod-attached blob; the replacement for the old
     * {@code stack.getOrCreateTag().putX(...)} pattern, which mutated a live tag in place.
     */
    public static void mutateCustomData(ItemStack stack, Consumer<CompoundTag> mutator) {

        CustomData.update(DataComponents.CUSTOM_DATA, stack, mutator);
    }

    /**
     * A sub-compound of the mod-attached blob, as a copy - the old {@code getTagElement}. Empty
     * rather than null when absent, so callers can read without a null check.
     */
    public static CompoundTag getCustomSubTag(ItemStack stack, String key) {

        return getCustomData(stack).getCompound(key);
    }

    public static boolean hasCustomSubTag(ItemStack stack, String key) {

        return getCustomData(stack).contains(key, Tag.TAG_COMPOUND);
    }

    /**
     * Writes a sub-compound into the mod-attached blob - the old {@code addTagElement}.
     */
    public static void setCustomSubTag(ItemStack stack, String key, Tag value) {

        mutateCustomData(stack, tag -> tag.put(key, value));
    }

    /**
     * The block entity data vanilla itself writes to a stack; its own component since 1.20.5,
     * not part of the mod-attached blob.
     */
    public static CompoundTag getBlockEntityData(ItemStack stack) {

        return stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
    }

    public static void setBlockEntityData(ItemStack stack, CompoundTag tag) {

        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        } else {
            stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        }
    }

    public static CompoundTag setItemStackTagName(CompoundTag tag, String name) {

        if (Strings.isNullOrEmpty(name)) {
            return null;
        }
        if (tag == null) {
            tag = new CompoundTag();
        }
        if (!tag.contains("display")) {
            tag.put("display", new CompoundTag());
        }
        tag.getCompound("display").putString("Name", name);
        return tag;
    }
    // endregion

    // region COMPARISON
    public static boolean itemsEqualWithTags(ItemStack stackA, ItemStack stackB) {

        return ItemStack.isSameItemSameComponents(stackA, stackB);
    }

    public static boolean itemsEqual(ItemStack stackA, ItemStack stackB) {

        return ItemStack.isSameItem(stackA, stackB);
    }

    /**
     * Compares item, meta, size and nbt of two stacks while ignoring nbt tag keys provided.
     * This is useful in shouldCauseReequipAnimation overrides.
     *
     * @param stackA          first stack to compare
     * @param stackB          second stack to compare
     * @param nbtTagsToIgnore tag keys to ignore when comparing the stacks
     */
    public static boolean areItemStacksEqualIgnoreTags(ItemStack stackA, ItemStack stackB, String... nbtTagsToIgnore) {

        if (stackA.isEmpty() && stackB.isEmpty()) {
            return true;
        }
        if (stackA.isEmpty() || stackB.isEmpty()) {
            return false;
        }
        if (stackA.getItem() != stackB.getItem()) {
            return false;
        }
        if (stackA.getDamageValue() != stackB.getDamageValue()) {
            return false;
        }
        if (stackA.getCount() != stackB.getCount()) {
            return false;
        }
        CompoundTag tagA = stackA.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag tagB = stackB.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tagA.isEmpty() && tagB.isEmpty()) {
            return true;
        }
        int numberOfKeys = tagA.getAllKeys().size();
        if (numberOfKeys != tagB.getAllKeys().size()) {
            return false;
        }

        String[] keys = new String[numberOfKeys];
        keys = tagA.getAllKeys().toArray(keys);

        a:
        for (int i = 0; i < numberOfKeys; ++i) {
            for (int j = 0; j < nbtTagsToIgnore.length; ++j) {
                if (nbtTagsToIgnore[j].equals(keys[i])) {
                    continue a;
                }
            }
            if (!tagA.getCompound(keys[i]).equals(tagB.getCompound(keys[i]))) {
                return false;
            }
        }
        return true;
    }
    // endregion

    // region HELD ITEMS
    public static boolean isPlayerHoldingSomething(Player player) {

        return !getHeldStack(player).isEmpty();
    }

    public static ItemStack getMainhandStack(Player player) {

        return player.getMainHandItem();
    }

    public static ItemStack getOffhandStack(Player player) {

        return player.getOffhandItem();
    }

    public static ItemStack getHeldStack(Player player) {

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty()) {
            stack = player.getOffhandItem();
        }
        return stack;
    }

    public static InteractionHand getMatchingHand(Player player, Predicate<ItemStack> filter) {

        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && filter.test(stack)) {
            return InteractionHand.MAIN_HAND;
        }
        stack = player.getOffhandItem();
        if (!stack.isEmpty() && filter.test(stack)) {
            return InteractionHand.OFF_HAND;
        }
        return InteractionHand.MAIN_HAND;
    }

    public static ItemStack getMatchingHeldStack(Player player, Predicate<ItemStack> filter) {

        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && filter.test(stack)) {
            return stack;
        }
        stack = player.getOffhandItem();
        if (!stack.isEmpty() && filter.test(stack)) {
            return stack;
        }
        return ItemStack.EMPTY;
    }
    // endregion

    // region MODE CHANGE
    public static ItemStack getHeldMultiModeStack(Player player) {

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !(stack.getItem() instanceof IMultiModeItem)) {
            stack = player.getOffhandItem();
        }
        return stack;
    }

    public static boolean isPlayerHoldingMultiModeItem(Player player) {

        if (!isPlayerHoldingSomething(player)) {
            return false;
        }
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() instanceof IMultiModeItem) {
            return true;
        } else {
            heldItem = player.getOffhandItem();
            return heldItem.getItem() instanceof IMultiModeItem;
        }
    }

    public static boolean incrHeldMultiModeItemState(Player player) {

        if (!isPlayerHoldingSomething(player)) {
            return false;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof IMultiModeItem
                ? ((IMultiModeItem) mainHand.getItem()).incrMode(mainHand)
                : offHand.getItem() instanceof IMultiModeItem && ((IMultiModeItem) offHand.getItem()).incrMode(offHand);
    }

    public static boolean decrHeldMultiModeItemState(Player player) {

        if (!isPlayerHoldingSomething(player)) {
            return false;
        }
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return mainHand.getItem() instanceof IMultiModeItem
                ? ((IMultiModeItem) mainHand.getItem()).decrMode(mainHand)
                : offHand.getItem() instanceof IMultiModeItem && ((IMultiModeItem) offHand.getItem()).decrMode(offHand);
    }

    public static void onHeldMultiModeItemChange(Player player) {

        ItemStack heldItem = getHeldMultiModeStack(player);
        ((IMultiModeItem) heldItem.getItem()).onModeChange(player, heldItem);
    }
    // endregion

    // region LEFT CLICK
    public static boolean isPlayerHoldingLeftClickItem(Player player) {

        if (!isPlayerHoldingSomething(player)) {
            return false;
        }
        return player.getMainHandItem().getItem() instanceof ILeftClickHandlerItem;
    }

    public static void onHeldLeftClickItem(Player player) {

        ItemStack heldItem = player.getMainHandItem();
        ((ILeftClickHandlerItem) heldItem.getItem()).onLeftClick(player, heldItem);
    }
    // endregion
}
