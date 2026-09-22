package cofh.lib.common.inventory;

import cofh.lib.api.IStorageCallback;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Simple Item Handler implementation using CoFH Item Storage objects.
 */
public class SimpleItemHandler implements IItemHandler, ResourceHandler<ItemResource> {

    @Nullable
    protected IStorageCallback callback;
    protected List<ItemStorageCoFH> slots;

    protected final BitSet changedSlots = new BitSet();
    protected final SnapshotJournal<BitSet> changeJournal = new SnapshotJournal<>() {

        @Override
        protected BitSet createSnapshot() {

            return (BitSet) changedSlots.clone();
        }

        @Override
        protected void revertToSnapshot(BitSet snapshot) {

            changedSlots.clear();
            changedSlots.or(snapshot);
        }

        @Override
        protected void onRootCommit(BitSet originalState) {

            BitSet changed = (BitSet) changedSlots.clone();
            changedSlots.clear();
            changed.stream().forEach(SimpleItemHandler.this::onInventoryChange);
        }
    };

    public SimpleItemHandler(@Nonnull List<ItemStorageCoFH> slots) {

        this(null, slots);
    }

    public SimpleItemHandler(@Nullable IStorageCallback callback) {

        this.callback = callback;
        this.slots = new ArrayList<>();
    }

    public SimpleItemHandler(@Nullable IStorageCallback callback, @Nonnull List<ItemStorageCoFH> slots) {

        this.callback = callback;
        this.slots = new ArrayList<>(slots);
    }

    public boolean isEmpty() {

        for (ItemStorageCoFH slot : slots) {
            if (!slot.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFull() {

        for (ItemStorageCoFH slot : slots) {
            if (!slot.isFull()) {
                return false;
            }
        }
        return true;
    }

    public void onInventoryChange(int slot) {

        if (callback == null) {
            return;
        }
        callback.onInventoryChanged(slot);
    }

    protected boolean canInsert(int slot) {

        return true;
    }

    protected boolean canExtract(int slot) {

        return true;
    }

    // region IItemHandler
    @Override
    public int getSlots() {

        return slots.size();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {

        if (slot < 0 || slot >= getSlots()) {
            return ItemStack.EMPTY;
        }
        return slots.get(slot).getItemStack();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

        if (slot < 0 || slot >= getSlots()) {
            return stack;
        }
        ItemStack ret = slots.get(slot).insertItem(slot, stack, simulate);

        if (!simulate) {
            onInventoryChange(slot);
        }
        return ret;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

        if (slot < 0 || slot >= getSlots()) {
            return ItemStack.EMPTY;
        }
        ItemStack ret = slots.get(slot).extractItem(slot, amount, simulate);

        if (!simulate) {
            onInventoryChange(slot);
        }
        return ret;
    }

    @Override
    public int getSlotLimit(int slot) {

        if (slot < 0 || slot >= getSlots()) {
            return 0;
        }
        return slots.get(slot).getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {

        if (slot < 0 || slot >= getSlots()) {
            return false;
        }
        return slots.get(slot).isItemValid(stack);
    }
    // endregion

    // region ResourceHandler
    @Override
    public int size() {

        return getSlots();
    }

    @Override
    public ItemResource getResource(int index) {

        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {

        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {

        return getSlotLimit(index);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {

        return !resource.isEmpty() && isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {

        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= getSlots() || amount == 0 || !canInsert(index)) {
            return 0;
        }
        ItemStorageCoFH slot = slots.get(index);
        slot.updateSnapshots(transaction);
        int inserted = amount - slot.insertItem(index, resource.toStack(amount), false).getCount();
        if (inserted > 0) {
            onChange(index, transaction);
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {

        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= getSlots() || amount == 0 || !canExtract(index) || !resource.matches(getStackInSlot(index))) {
            return 0;
        }
        ItemStorageCoFH slot = slots.get(index);
        slot.updateSnapshots(transaction);
        int extracted = slot.extractItem(index, amount, false).getCount();
        if (extracted > 0) {
            onChange(index, transaction);
        }
        return extracted;
    }

    protected void onChange(int index, TransactionContext transaction) {

        changeJournal.updateSnapshots(transaction);
        changedSlots.set(index);
    }
    // endregion
}
