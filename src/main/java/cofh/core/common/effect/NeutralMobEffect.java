package cofh.core.common.effect;

import cofh.lib.common.effect.MobEffectCoFH;
import net.minecraft.world.effect.MobEffectCategory;

public class NeutralMobEffect extends MobEffectCoFH {

    public NeutralMobEffect(MobEffectCategory typeIn, int liquidColorIn) {

        super(typeIn, liquidColorIn);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {

        return false;
    }

}
