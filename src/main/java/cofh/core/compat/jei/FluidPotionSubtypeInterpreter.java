package cofh.core.compat.jei;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;


public class FluidPotionSubtypeInterpreter implements ISubtypeInterpreter<FluidStack> {

    public static final FluidPotionSubtypeInterpreter INSTANCE = new FluidPotionSubtypeInterpreter();

    private FluidPotionSubtypeInterpreter() {

    }

    @Override
    public Object getSubtypeData(FluidStack ingredient, UidContext context) {

        PotionContents contents = ingredient.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(contents.potion().map(potion -> potion.value().name()).orElse("empty"));
        for (MobEffectInstance effect : contents.getAllEffects()) {
            stringBuilder.append(";").append(effect);
        }
        return stringBuilder.toString();
    }

}
