package cofh.core.common.network.packet.client;

import cofh.core.common.network.data.client.EffectAddedPayload;
import cofh.core.util.ProxyUtils;
import cofh.lib.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class EffectAddedPacket {

    public static final EffectAddedPacket INSTANCE = new EffectAddedPacket();

    public static EffectAddedPacket get() {

        return INSTANCE;
    }

    public void handle(final EffectAddedPayload payload, final IPayloadContext context) {

        context.enqueueWork(() -> {
            int id = payload.entityId();
            Holder<MobEffect> effectType = BuiltInRegistries.MOB_EFFECT.getHolder(payload.effect()).orElse(null);
            int effectDur = payload.duration();

            MobEffectInstance effect = effectType != null ? new MobEffectInstance(effectType, effectDur) : null;

            if (effect == null) {
                return;
            }
            Level level = ProxyUtils.getClientWorld();
            if (level == null) {
                return;
            }
            Entity entity = level.getEntity(id);
            if (entity instanceof LivingEntity living && !entity.equals(ProxyUtils.getClientPlayer())) {
                living.forceAddEffect(effect, null);
            }
        });
    }

    public static void sendToClient(LivingEntity entity, MobEffectInstance effect) {

        if (entity == null || effect == null) {
            return;
        }
        Utils.sendNear(entity, new EffectAddedPayload(entity.getId(), effect.getEffect().unwrapKey().map(ResourceKey::location).orElse(null), effect.getDuration()));

    }

    public static void sendToClient(LivingEntity entity, MobEffectInstance effect, Player player) {

        if (entity == null || effect == null) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new EffectAddedPayload(entity.getId(), effect.getEffect().unwrapKey().map(ResourceKey::location).orElse(null), effect.getDuration()));
        }
    }

}
