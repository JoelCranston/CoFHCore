package cofh.core.common.item;

import cofh.lib.api.item.IColorableItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

public class SpawnEggItemCoFH extends SpawnEggItem implements IColorableItem {

    protected final int primaryColor;
    protected final int secondaryColor;

    public SpawnEggItemCoFH(Supplier<EntityType<? extends Mob>> typeSupIn, int primaryColorIn, int secondaryColorIn, Properties builder) {

        super(builder.spawnEgg(typeSupIn.get()));
        this.primaryColor = primaryColorIn;
        this.secondaryColor = secondaryColorIn;
    }

    public int getColor(ItemStack item, int colorIndex) {

        return colorIndex == 0 ? primaryColor : secondaryColor;
    }

}
