package cofh.core.common.item;

import cofh.core.util.helpers.ItemHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static cofh.lib.util.constants.NBTTags.TAG_MODE;

/**
 * Implement this interface on Item classes which have multiple modes - what that means is completely up to you.
 * This just provides a uniform way of dealing with them.
 *
 * @author King Lemming
 */
public interface IMultiModeItem {

    /**
     * Returns a copy; write the mode through {@link #setModeTag}.
     */
    default CompoundTag getModeTag(ItemStack stack) {

        return ItemHelper.getCustomData(stack);
    }

    default void setModeTag(ItemStack stack, int mode) {

        ItemHelper.mutateCustomData(stack, tag -> tag.putInt(TAG_MODE, mode));
    }

    /**
     * Get the current mode of an item.
     */
    default int getMode(ItemStack stack) {

        return getModeTag(stack).getInt(TAG_MODE);
    }

    /**
     * Attempt to set the empowered state of the item.
     *
     * @param stack ItemStack to set the mode on.
     * @param mode  Desired mode.
     * @return TRUE if the operation was successful, FALSE if it was not.
     */
    default boolean setMode(ItemStack stack, int mode) {

        if (getNumModes(stack) <= 1) {
            mode = 0;
        }
        if (mode < getNumModes(stack)) {
            setModeTag(stack, mode);
            return true;
        }
        return false;
    }

    /**
     * Increment the current mode of an item.
     */
    default boolean incrMode(ItemStack stack) {

        if (getNumModes(stack) <= 1) {
            return false;
        }
        int curMode = getMode(stack);
        ++curMode;
        if (curMode >= getNumModes(stack)) {
            curMode = 0;
        }
        setModeTag(stack, curMode);
        return true;
    }

    /**
     * Decrement the current mode of an item.
     */
    default boolean decrMode(ItemStack stack) {

        // Decrement is only available if there are 3 or more modes. Otherwise, it's just a toggle.
        if (getNumModes(stack) <= 2) {
            return false;
        }
        int curMode = getMode(stack);
        --curMode;
        if (curMode < 0) {
            curMode = getNumModes(stack) - 1;
        }
        setModeTag(stack, curMode);
        return true;
    }

    /**
     * Returns the number of possible modes.
     */
    default int getNumModes(ItemStack stack) {

        return 2;
    }

    /**
     * Callback method for reacting to a state change. Useful in KeyBinding handlers.
     *
     * @param player Player holding the item, if applicable.
     * @param stack  The item being held.
     */
    default void onModeChange(Player player, ItemStack stack) {

    }

}
