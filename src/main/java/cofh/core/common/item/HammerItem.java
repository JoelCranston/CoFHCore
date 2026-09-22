package cofh.core.common.item;

import cofh.lib.common.item.PickaxeItemCoFH;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ToolMaterial;

public class HammerItem extends PickaxeItemCoFH {

    private static final float DEFAULT_ATTACK_DAMAGE = 4.0F;
    private static final float DEFAULT_ATTACK_SPEED = -3.4F;
    private static final int DEFAULT_BASE_AREA = 1;

    public final int radius;

    public HammerItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, int radius, Properties builder) {

        // Disables shields for 5 seconds, as an axe does.
        super(builder.tool(scaleDurability(material, 4), BlockTags.MINEABLE_WITH_PICKAXE, attackDamageIn, attackSpeedIn, 5.0F));
        this.radius = radius;
    }

    public HammerItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        this(material, attackDamageIn, attackSpeedIn, DEFAULT_BASE_AREA, builder);
    }

    public HammerItem(ToolMaterial material, float attackDamageIn, Properties builder) {

        this(material, attackDamageIn, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_AREA, builder);
    }

    public HammerItem(ToolMaterial material, Properties builder) {

        this(material, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_AREA, builder);
    }

    private static ToolMaterial scaleDurability(ToolMaterial material, int multiplier) {

        return new ToolMaterial(material.incorrectBlocksForDrops(), material.durability() * multiplier, material.speed(), material.attackDamageBonus(), material.enchantmentValue(), material.repairItems());
    }

}
