package cofh.core.common.item;

import cofh.lib.common.item.ShovelItemCoFH;
import net.minecraft.world.item.ToolMaterial;

public class ExcavatorItem extends ShovelItemCoFH {

    private static final float DEFAULT_ATTACK_DAMAGE = 2.0F;
    private static final float DEFAULT_ATTACK_SPEED = -3.2F;
    private static final int DEFAULT_BASE_AREA = 1;

    public final int radius;

    public ExcavatorItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, int radius, Properties builder) {

        super(scaleDurability(material, 4), attackDamageIn, attackSpeedIn, builder);
        this.radius = radius;
    }

    public ExcavatorItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        this(material, attackDamageIn, attackSpeedIn, DEFAULT_BASE_AREA, builder);
    }

    public ExcavatorItem(ToolMaterial material, float attackDamageIn, Properties builder) {

        this(material, attackDamageIn, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_AREA, builder);
    }

    public ExcavatorItem(ToolMaterial material, Properties builder) {

        this(material, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_AREA, builder);
    }

    private static ToolMaterial scaleDurability(ToolMaterial material, int multiplier) {

        return new ToolMaterial(material.incorrectBlocksForDrops(), material.durability() * multiplier, material.speed(), material.attackDamageBonus(), material.enchantmentValue(), material.repairItems());
    }

}
