package cofh.core.common.effect;

import cofh.core.common.capability.CoreCapabilities;
import cofh.lib.common.effect.MobEffectCoFH;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class EnergyChargeMobEffect extends MobEffectCoFH {

    private final int amount;

    public EnergyChargeMobEffect(MobEffectCategory typeIn, int liquidColorIn, int amount) {

        super(typeIn, liquidColorIn);
        this.amount = amount;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entityLivingBaseIn, int amplifier) {

        if (entityLivingBaseIn instanceof ServerPlayer player) {

            if (amount <= 0) {
                drainForgeEnergy(player, -amount);
                drainRedstoneFlux(player, -amount);
            } else {
                chargeForgeEnergy(player, amount);
                chargeRedstoneFlux(player, amount);
            }
        }
        return true;
    }

    // region HELPERS
    private void chargeForgeEnergy(ServerPlayer player, final int chargeAmount) {

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (player.getInventory().getItem(i).isEmpty()) {
                continue;
            }
            EnergyHandler cap = ItemAccess.forPlayerSlot(player, i).getCapability(Capabilities.Energy.ITEM);
            if (cap != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    cap.insert(chargeAmount, transaction);
                    transaction.commit();
                }
            }
        }
    }

    private void chargeRedstoneFlux(ServerPlayer player, final int chargeAmount) {

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            var cap = player.getInventory().getItem(i).getCapability(CoreCapabilities.RedstoneFluxStorage.ITEM);
            if (cap != null) {
                cap.receiveEnergy(chargeAmount, false);
            }
        }
    }

    private void drainForgeEnergy(ServerPlayer player, final int drainAmount) {

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            if (player.getInventory().getItem(i).isEmpty()) {
                continue;
            }
            EnergyHandler cap = ItemAccess.forPlayerSlot(player, i).getCapability(Capabilities.Energy.ITEM);
            if (cap != null) {
                try (Transaction transaction = Transaction.openRoot()) {
                    cap.extract(drainAmount, transaction);
                    transaction.commit();
                }
            }
        }
    }

    private void drainRedstoneFlux(ServerPlayer player, final int drainAmount) {

        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            var cap = player.getInventory().getItem(i).getCapability(CoreCapabilities.RedstoneFluxStorage.ITEM);
            if (cap != null) {
                cap.extractEnergy(drainAmount, false);
            }
        }
    }
    // endregion
}
