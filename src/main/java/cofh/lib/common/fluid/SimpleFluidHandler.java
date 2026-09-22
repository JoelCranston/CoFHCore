package cofh.lib.common.fluid;

import cofh.lib.api.IStorageCallback;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Basic Fluid Handler implementation using CoFH Fluid Storage objects.
 */
public class SimpleFluidHandler implements IFluidHandler, ResourceHandler<FluidResource> {

    @Nullable
    protected IStorageCallback callback;
    protected List<FluidStorageCoFH> tanks;

    protected final BitSet changedTanks = new BitSet();
    protected final SnapshotJournal<BitSet> changeJournal = new SnapshotJournal<>() {

        @Override
        protected BitSet createSnapshot() {

            return (BitSet) changedTanks.clone();
        }

        @Override
        protected void revertToSnapshot(BitSet snapshot) {

            changedTanks.clear();
            changedTanks.or(snapshot);
        }

        @Override
        protected void onRootCommit(BitSet originalState) {

            BitSet changed = (BitSet) changedTanks.clone();
            changedTanks.clear();
            changed.stream().forEach(SimpleFluidHandler.this::onTankChange);
        }
    };

    public SimpleFluidHandler() {

        this(null);
    }

    public SimpleFluidHandler(@Nullable IStorageCallback callback) {

        this.callback = callback;
        this.tanks = new ArrayList<>();
    }

    public SimpleFluidHandler(@Nullable IStorageCallback callback, @Nonnull List<FluidStorageCoFH> tanks) {

        this.callback = callback;
        this.tanks = new ArrayList<>(tanks);
    }

    public boolean hasTanks() {

        return tanks.size() > 0;
    }

    public boolean isEmpty() {

        for (FluidStorageCoFH tank : tanks) {
            if (!tank.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void onTankChange(int tank) {

        if (callback == null) {
            return;
        }
        callback.onTankChanged(tank);
    }

    protected boolean canInsert(int tank) {

        return true;
    }

    protected boolean canExtract(int tank) {

        return true;
    }

    // region IFluidHandler
    @Override
    public int getTanks() {

        return tanks.size();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {

        if (tank < 0 || tank >= getTanks()) {
            return FluidStack.EMPTY;
        }
        return tanks.get(tank).getFluidStack();
    }

    @Nonnull
    @Override
    public int fill(FluidStack resource, FluidAction action) {

        int ret;
        for (FluidStorageCoFH tank : tanks) {
            ret = tank.fill(resource, action);
            if (ret > 0) {
                return ret;
            }
        }
        return 0;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {

        FluidStack ret;
        for (FluidStorageCoFH tank : tanks) {
            ret = tank.drain(resource, action);
            if (!ret.isEmpty()) {
                return ret;
            }
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {

        FluidStack ret;
        for (FluidStorageCoFH tank : tanks) {
            ret = tank.drain(maxDrain, action);
            if (!ret.isEmpty()) {
                return ret;
            }
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank) {

        if (tank < 0 || tank > getTanks()) {
            return 0;
        }
        return tanks.get(tank).getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {

        if (tank < 0 || tank > getTanks()) {
            return false;
        }
        return tanks.get(tank).isFluidValid(stack);
    }
    // endregion

    // region ResourceHandler
    @Override
    public int size() {

        return getTanks();
    }

    @Override
    public FluidResource getResource(int index) {

        return FluidResource.of(getFluidInTank(index));
    }

    @Override
    public long getAmountAsLong(int index) {

        return getFluidInTank(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {

        return index < 0 || index >= getTanks() ? 0 : getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {

        return !resource.isEmpty() && index >= 0 && index < getTanks() && isFluidValid(index, resource.toStack(1));
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {

        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= getTanks() || amount == 0 || !canInsert(index)) {
            return 0;
        }
        FluidStorageCoFH tank = tanks.get(index);
        tank.updateSnapshots(transaction);
        int filled = tank.fill(resource.toStack(amount), FluidAction.EXECUTE);
        if (filled > 0) {
            onChange(index, transaction);
        }
        return filled;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {

        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (index < 0 || index >= getTanks() || amount == 0 || !canExtract(index) || !resource.matches(getFluidInTank(index))) {
            return 0;
        }
        FluidStorageCoFH tank = tanks.get(index);
        tank.updateSnapshots(transaction);
        int drained = tank.drain(amount, FluidAction.EXECUTE).getAmount();
        if (drained > 0) {
            onChange(index, transaction);
        }
        return drained;
    }

    protected void onChange(int index, TransactionContext transaction) {

        changeJournal.updateSnapshots(transaction);
        changedTanks.set(index);
    }
    // endregion
}
