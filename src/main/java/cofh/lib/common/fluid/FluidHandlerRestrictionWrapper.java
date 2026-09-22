package cofh.lib.common.fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;

public class FluidHandlerRestrictionWrapper implements IFluidHandler, ResourceHandler<FluidResource> {

    protected IFluidHandler wrappedHandler;
    protected ResourceHandler<FluidResource> wrappedResourceHandler;
    protected boolean canFill;
    protected boolean canDrain;

    public <T extends IFluidHandler & ResourceHandler<FluidResource>> FluidHandlerRestrictionWrapper(T wrappedHandler, boolean canFill, boolean canDrain) {

        this.wrappedHandler = wrappedHandler;
        this.wrappedResourceHandler = wrappedHandler;
        this.canFill = canFill;
        this.canDrain = canDrain;
    }

    @Override
    public int getTanks() {

        return wrappedHandler.getTanks();
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank) {

        return wrappedHandler.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {

        return wrappedHandler.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {

        return wrappedHandler.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {

        if (!canFill) {
            return 0;
        }
        return wrappedHandler.fill(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {

        if (!canDrain) {
            return FluidStack.EMPTY;
        }
        return wrappedHandler.drain(resource, action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {

        if (!canDrain) {
            return FluidStack.EMPTY;
        }
        return wrappedHandler.drain(maxDrain, action);
    }

    // region ResourceHandler
    @Override
    public int size() {

        return wrappedResourceHandler.size();
    }

    @Override
    public FluidResource getResource(int index) {

        return wrappedResourceHandler.getResource(index);
    }

    @Override
    public long getAmountAsLong(int index) {

        return wrappedResourceHandler.getAmountAsLong(index);
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {

        return wrappedResourceHandler.getCapacityAsLong(index, resource);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {

        return wrappedResourceHandler.isValid(index, resource);
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {

        return canFill ? wrappedResourceHandler.insert(index, resource, amount, transaction) : 0;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {

        return canDrain ? wrappedResourceHandler.extract(index, resource, amount, transaction) : 0;
    }
    // endregion

}
