package cofh.lib.common.inventory;

import cofh.lib.api.IStorageCallback;
import cofh.lib.api.StorageGroup;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cofh.lib.util.constants.NBTTags.TAG_ITEM_INV;
import static cofh.lib.util.constants.NBTTags.TAG_SLOT;
import static net.minecraft.nbt.Tag.TAG_COMPOUND;

/**
 * Inventory abstraction using CoFH Item Storage objects.
 */
public class SimpleItemInv extends SimpleItemHandler {

    protected String tag;

    protected IItemHandler allHandler;

    public SimpleItemInv(@Nonnull List<ItemStorageCoFH> slots) {

        this(null, slots, TAG_ITEM_INV);
    }

    public SimpleItemInv(@Nullable IStorageCallback callback) {

        this(callback, TAG_ITEM_INV);
    }

    public SimpleItemInv(@Nullable IStorageCallback callback, @Nonnull List<ItemStorageCoFH> slots) {

        this(callback, slots, TAG_ITEM_INV);
    }

    public SimpleItemInv(@Nullable IStorageCallback callback, @Nonnull String tag) {

        this(callback, Collections.emptyList(), tag);
    }

    public SimpleItemInv(@Nullable IStorageCallback callback, @Nonnull List<ItemStorageCoFH> slots, @Nonnull String tag) {

        super(callback, slots);
        this.tag = tag;
    }

    public void addSlot(ItemStorageCoFH slot) {

        if (allHandler != null) {
            return;
        }
        slots.add(slot);
    }

    public void clear() {

        for (ItemStorageCoFH slot : slots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
    }

    public void set(int slot, ItemStack stack) {

        slots.get(slot).setItemStack(stack);
    }

    public ItemStack get(int slot) {

        return slots.get(slot).getItemStack();
    }

    // TODO: Works but not used atm. Commented out for safety.
    //    public List<ItemStack> getStacksInSlots(int startSlot, int endSlot) {
    //
    //        if (startSlot < 0 || endSlot >= slots.size()) {
    //            return Collections.emptyList();
    //        }
    //        if (startSlot > endSlot) {
    //            int temp = endSlot;
    //            endSlot = startSlot;
    //            startSlot = temp;
    //        }
    //        ArrayList<ItemStack> ret = new ArrayList<>();
    //        for (int i = startSlot; i <= endSlot; ++i) {
    //            ret.add(get(i));
    //        }
    //        return ret;
    //    }

    public ItemStorageCoFH getSlot(int slot) {

        return slots.get(slot);
    }

    // region NBT
    // ItemStorageCoFH#read/write need a HolderLookup.Provider now (ItemStack persistence does) -
    // threaded through every method here even though most of these are currently unused, since
    // they all bottom out in the same per-slot read/write.
    public SimpleItemInv read(HolderLookup.Provider provider, CompoundTag nbt) {

        for (ItemStorageCoFH slot : slots) {
            slot.setItemStack(ItemStack.EMPTY);
        }
        ListTag list = nbt.getList(tag, TAG_COMPOUND);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag slotTag = list.getCompound(i);
            int slot = slotTag.getByte(TAG_SLOT);
            if (slot >= 0 && slot < slots.size()) {
                slots.get(slot).read(provider, slotTag);
            }
        }
        return this;
    }

    public CompoundTag write(HolderLookup.Provider provider, CompoundTag nbt) {

        if (slots.size() <= 0) {
            return nbt;
        }
        ListTag list = new ListTag();
        for (int i = 0; i < slots.size(); ++i) {
            if (!slots.get(i).isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte(TAG_SLOT, (byte) i);
                slots.get(i).write(provider, slotTag);
                list.add(slotTag);
            }
        }
        if (!list.isEmpty()) {
            nbt.put(tag, list);
        } else {
            nbt.remove(tag);
        }
        return nbt;
    }
    // endregion

    // region HELPERS
    public CompoundTag writeSlotsToNBT(HolderLookup.Provider provider, CompoundTag nbt, int startIndex, int endIndex) {

        return writeSlotsToNBT(provider, nbt, tag, startIndex, endIndex);
    }

    public CompoundTag writeSlotsToNBT(HolderLookup.Provider provider, CompoundTag nbt, String saveTag, int startIndex) {

        return writeSlotsToNBT(provider, nbt, saveTag, startIndex, slots.size());
    }

    public CompoundTag writeSlotsToNBT(HolderLookup.Provider provider, CompoundTag nbt, String saveTag, int startIndex, int endIndex) {

        if (startIndex < 0 || startIndex >= endIndex || startIndex >= slots.size()) {
            return nbt;
        }
        ListTag list = new ListTag();
        for (int i = startIndex; i < Math.min(endIndex, slots.size()); ++i) {
            if (!slots.get(i).isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotTag.putByte(TAG_SLOT, (byte) i);
                slots.get(i).write(provider, slotTag);
                list.add(slotTag);
            }
        }
        if (!list.isEmpty()) {
            nbt.put(saveTag, list);
        } else {
            nbt.remove(tag);
        }
        return nbt;
    }
    // endregion

    // region UNORDERED METHODS
    public SimpleItemInv readSlotsUnordered(HolderLookup.Provider provider, ListTag list, int startIndex) {

        return readSlotsUnordered(provider, list, startIndex, slots.size());
    }

    public SimpleItemInv readSlotsUnordered(HolderLookup.Provider provider, ListTag list, int startIndex, int endIndex) {

        if (startIndex < 0 || startIndex >= endIndex || startIndex >= slots.size()) {
            return this;
        }
        for (int i = 0; i < Math.min(Math.min(endIndex, slots.size()) - startIndex, list.size()); ++i) {
            CompoundTag slotTag = list.getCompound(i);
            slots.get(startIndex + i).read(provider, slotTag);
        }
        return this;
    }

    public CompoundTag writeSlotsToNBTUnordered(HolderLookup.Provider provider, CompoundTag nbt, int startIndex, int endIndex) {

        return writeSlotsToNBTUnordered(provider, nbt, tag, startIndex, endIndex);
    }

    public CompoundTag writeSlotsToNBTUnordered(HolderLookup.Provider provider, CompoundTag nbt, String saveTag, int startIndex) {

        return writeSlotsToNBTUnordered(provider, nbt, saveTag, startIndex, slots.size());
    }

    public CompoundTag writeSlotsToNBTUnordered(HolderLookup.Provider provider, CompoundTag nbt, String saveTag, int startIndex, int endIndex) {

        if (startIndex < 0 || startIndex >= endIndex || startIndex >= slots.size()) {
            return nbt;
        }
        ListTag list = new ListTag();
        for (int i = startIndex; i < Math.min(endIndex, slots.size()); ++i) {
            if (!slots.get(i).isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slots.get(i).write(provider, slotTag);
                list.add(slotTag);
            }
        }
        if (!list.isEmpty()) {
            nbt.put(saveTag, list);
        } else {
            nbt.remove(saveTag);
        }
        return nbt;
    }
    // endregion

    public IItemHandler getHandler(StorageGroup group) {

        if (allHandler == null) {
            ((ArrayList<ItemStorageCoFH>) slots).trimToSize();
            allHandler = new SimpleItemHandler(callback, slots);
        }
        return allHandler;
    }

}
