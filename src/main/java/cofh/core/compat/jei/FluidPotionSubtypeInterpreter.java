package cofh.core.compat.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;


public class FluidPotionSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {

    public static final FluidPotionSubtypeInterpreter INSTANCE = new FluidPotionSubtypeInterpreter();

    private FluidPotionSubtypeInterpreter() {

    }

    @Override
    public String apply(FluidStack ingredient, UidContext context) {

        // The potion is one component now, and its getAllEffects() already merges the potion's
        // own effects with any custom ones - no need to read the two halves separately.
        PotionContents contents = ingredient.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return IIngredientSubtypeInterpreter.NONE;
        }
        StringBuilder stringBuilder = new StringBuilder(Potion.getName(contents.potion(), ""));
        for (MobEffectInstance effect : contents.getAllEffects()) {
            stringBuilder.append(";").append(effect);
        }
        return stringBuilder.toString();
    }

}
