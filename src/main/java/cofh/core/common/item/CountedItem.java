package cofh.core.common.item;

import cofh.core.util.ProxyUtils;
import net.minecraft.resources.Identifier;

public class CountedItem extends ItemCoFH {

    public CountedItem(Properties builder) {

        super(builder);

        ProxyUtils.registerItemModelProperty(this, Identifier.parse("count"), (stack, world, living, seed) -> ((float) stack.getCount()) / stack.getMaxStackSize());
    }

}
