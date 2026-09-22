package cofh.lib.common.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EnergyHandlerRestrictionWrapper implements IEnergyStorage, EnergyHandler {

    protected IEnergyStorage wrappedHandler;
    protected EnergyHandler wrappedEnergyHandler;
    protected boolean canReceive;
    protected boolean canExtract;

    public <T extends IEnergyStorage & EnergyHandler> EnergyHandlerRestrictionWrapper(T wrappedHandler, boolean canReceive, boolean canExtract) {

        this.wrappedHandler = wrappedHandler;
        this.wrappedEnergyHandler = wrappedHandler;
        this.canReceive = canReceive;
        this.canExtract = canExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {

        if (!canReceive()) {
            return 0;
        }
        return wrappedHandler.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {

        if (!canExtract()) {
            return 0;
        }
        return wrappedHandler.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {

        return wrappedHandler.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {

        return wrappedHandler.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {

        return canExtract;
    }

    @Override
    public boolean canReceive() {

        return canReceive;
    }

    // region EnergyHandler
    @Override
    public long getAmountAsLong() {

        return wrappedEnergyHandler.getAmountAsLong();
    }

    @Override
    public long getCapacityAsLong() {

        return wrappedEnergyHandler.getCapacityAsLong();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {

        return canReceive() ? wrappedEnergyHandler.insert(amount, transaction) : 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {

        return canExtract() ? wrappedEnergyHandler.extract(amount, transaction) : 0;
    }
    // endregion

}
