package cofh.core.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cofh.core.init.CoreMobEffects.*;
import static cofh.lib.init.tags.DamageTypeTagsCoFH.IS_MAGIC;
import static net.minecraft.tags.DamageTypeTags.*;

/**
 * @author King Lemming and Hek
 */
@Mixin (LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject (
            method = "canBeAffected(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            at = @At ("HEAD"),
            cancellable = true
    )
    private void cancelEffectApplication(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> callback) {

        LivingEntity living = (LivingEntity) (Object) this;

        if (effectInstance.getEffect() == CHILLED && living.hasEffect(COLD_RESISTANCE)) {
            callback.setReturnValue(false);
        }
        if (effectInstance.getEffect() == SHOCKED && living.hasEffect(LIGHTNING_RESISTANCE)) {
            callback.setReturnValue(false);
        }
    }

    @Inject (
            method = "canFreeze()Z",
            at = @At ("HEAD"),
            cancellable = true
    )
    private void cancelFreeze(CallbackInfoReturnable<Boolean> callback) {

        LivingEntity living = (LivingEntity) (Object) this;

        if (living.hasEffect(COLD_RESISTANCE)) {
            callback.setReturnValue(false);
        }
    }

    @Inject (
            method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            at = @At ("HEAD"),
            cancellable = true
    )
    private void cancelHurt(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {

        LivingEntity living = (LivingEntity) (Object) this;

        if (source.is(IS_FIRE)) {
            living.removeEffect(CHILLED);
        }
        if (!source.is(BYPASSES_ENCHANTMENTS)) {
            if (source.is(IS_EXPLOSION) && living.hasEffect(EXPLOSION_RESISTANCE)) {
                callback.setReturnValue(false);
            }
            if (source.is(IS_MAGIC) && living.hasEffect(MAGIC_RESISTANCE)) {
                callback.setReturnValue(false);
            }
            if (source == level.damageSources().freeze() && living.hasEffect(COLD_RESISTANCE)) {
                callback.setReturnValue(false);
            }
            if (source == level.damageSources().lightningBolt() && living.hasEffect(LIGHTNING_RESISTANCE)) {
                callback.setReturnValue(false);
            }
        }
    }

    @Inject (
            method = "updateInvisibilityStatus()V",
            at = @At ("HEAD"),
            cancellable = true
    )
    private void trueInvisibility(CallbackInfo callback) {

        LivingEntity living = (LivingEntity) (Object) this;
        if (living.hasEffect(TRUE_INVISIBILITY)) {
            living.removeEffectParticles();
            living.setInvisible(true);
            callback.cancel();
        }
    }

}
