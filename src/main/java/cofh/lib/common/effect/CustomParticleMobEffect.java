package cofh.lib.common.effect;

import cofh.core.common.network.packet.client.EffectAddedPacket;
import cofh.core.common.network.packet.client.EffectRemovedPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class CustomParticleMobEffect extends MobEffectCoFH {

    public CustomParticleMobEffect(MobEffectCategory typeIn, int liquidColorIn) {

        super(typeIn, liquidColorIn);
    }

    @Override
    public void onApply(LivingEntity entity, MobEffectInstance instance) {

        EffectAddedPacket.sendToClient(entity, instance);
    }

    @Override
    public void onTrack(LivingEntity entity, MobEffectInstance instance, Player tracker) {

        EffectAddedPacket.sendToClient(entity, instance, tracker);
    }

    @Override
    public void onRemove(LivingEntity entity, MobEffectInstance instance) {

        EffectRemovedPacket.sendToClient(entity, instance);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity living, int amplifier) {

        if (level.getRandom().nextInt(getChance()) == 0) {
            level.sendParticles(getParticle(), living.getRandomX(1.0D), living.getRandomY(), living.getRandomZ(1.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        return true;
    }

    // Particles are spawned in applyEffectTick; null suppresses the vanilla swirl.
    @Override
    public ParticleOptions createParticleOptions(MobEffectInstance instance) {

        return null;
    }

    public abstract ParticleOptions getParticle();

    public int getChance() {

        return 3;
    }

}
