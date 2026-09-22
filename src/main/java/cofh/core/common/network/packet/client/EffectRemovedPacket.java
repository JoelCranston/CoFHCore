package cofh.core.common.network.packet.client;

import cofh.core.common.network.data.client.EffectRemovedPayload;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static cofh.lib.util.Utils.getRegistryName;

public class EffectRemovedPacket {

    public static final EffectRemovedPacket INSTANCE = new EffectRemovedPacket();

    public static EffectRemovedPacket get() {

        return INSTANCE;
    }

    public void handle(final EffectRemovedPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            int id = payload.entityId();
            Holder<MobEffect> effectType = BuiltInRegistries.MOB_EFFECT.getHolder(payload.effect()).orElse(null);

            if (ProxyUtils.getClientWorld().getEntity(id) instanceof LivingEntity entity && !entity.equals(ProxyUtils.getClientPlayer())) {
                MobEffectInstance existing = entity.removeEffectNoUpdate(effectType);
                if (existing != null) {
                    entity.onEffectRemoved(existing);
                }
            }
        });
    }

    public static void sendToClient(LivingEntity entity, MobEffectInstance effect) {

        if (entity == null || effect == null) {
            return;
        }
        Utils.sendNear(entity, new EffectRemovedPayload(entity.getId(), effect.getEffect().unwrapKey().map(ResourceKey::location).orElse(null)));
    }

    public static void sendToClient(LivingEntity entity, MobEffect effect) {

        if (entity == null || effect == null) {
            return;
        }
        Utils.sendNear(entity, new EffectRemovedPayload(entity.getId(), getRegistryName(effect)));
    }

}
