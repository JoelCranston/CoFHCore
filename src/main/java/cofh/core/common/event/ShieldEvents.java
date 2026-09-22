package cofh.core.common.event;

import cofh.core.common.capability.CoreCapabilities;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;
import static net.neoforged.bus.api.EventPriority.HIGH;

@EventBusSubscriber (modid = ID_COFH_CORE)
public class ShieldEvents {

    private ShieldEvents() {

    }

    @SubscribeEvent (priority = HIGH)
    public static void handleShieldBlock(LivingShieldBlockEvent event) {

        if (!event.getBlocked()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        var shield = entity.getUseItem().getCapability(CoreCapabilities.ShieldHandler.ITEM);
        if (shield != null) {
            event.setBlockedDamage(shield.onBlock(entity, event.getDamageSource(), event.getBlockedDamage()));
        }
    }

}