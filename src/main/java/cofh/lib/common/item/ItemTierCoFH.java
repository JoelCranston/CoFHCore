package cofh.lib.common.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public class ItemTierCoFH {

    private final int level;
    private final ToolMaterial material;

    public ItemTierCoFH(int level, int uses, float speed, float damage, int enchantmentValue, TagKey<Block> incorrectBlocksForDrops, TagKey<Item> repairItems) {

        this.level = level;
        this.material = new ToolMaterial(incorrectBlocksForDrops, uses, speed, damage, enchantmentValue, repairItems);
    }

    public int getLevel() {

        return this.level;
    }

    public ToolMaterial getMaterial() {

        return this.material;
    }

}
