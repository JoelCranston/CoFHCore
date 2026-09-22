package cofh.lib.util.random;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WeightedRandomDrop {

    public final Item item;
    private final int weight;

    public WeightedRandomDrop(Item item, int itemWeightIn) {

        this.item = item;
        this.weight = itemWeightIn;
    }

    public int getWeight() {

        return weight;
    }

    public ItemStack toItemStack(int count) {

        return new ItemStack(item, count);
    }

}
