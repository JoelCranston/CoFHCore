package cofh.core.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static cofh.lib.init.tags.BlockTagsCoFH.MINEABLE_WITH_SICKLE;

public class SickleItem extends Item implements ICoFHItem {

    private static final float DEFAULT_ATTACK_DAMAGE = 2.5F;
    private static final float DEFAULT_ATTACK_SPEED = -2.6F;
    private static final int DEFAULT_BASE_RADIUS = 2;
    private static final int DEFAULT_BASE_HEIGHT = 0;

    public final int radius;
    public final int height;

    public SickleItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, int radius, int height, Properties builder) {

        super(builder.tool(scaleDurability(material, 4), MINEABLE_WITH_SICKLE, attackDamageIn, attackSpeedIn, 0.0F).component(DataComponents.TOOL, createTool(material)));
        this.radius = radius;
        this.height = height;
    }

    public SickleItem(ToolMaterial material, float attackDamageIn, float attackSpeedIn, Properties builder) {

        this(material, attackDamageIn, attackSpeedIn, DEFAULT_BASE_RADIUS, DEFAULT_BASE_HEIGHT, builder);
    }

    public SickleItem(ToolMaterial material, float attackDamageIn, Properties builder) {

        this(material, attackDamageIn, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_RADIUS, DEFAULT_BASE_HEIGHT, builder);
    }

    public SickleItem(ToolMaterial material, Properties builder) {

        this(material, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, DEFAULT_BASE_RADIUS, DEFAULT_BASE_HEIGHT, builder);
    }

    private static Tool createTool(ToolMaterial material) {

        HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        return new Tool(List.of(
                Tool.Rule.overrideSpeed(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
                Tool.Rule.deniesDrops(blocks.getOrThrow(material.incorrectBlocksForDrops())),
                Tool.Rule.minesAndDrops(blocks.getOrThrow(MINEABLE_WITH_SICKLE), material.speed())
        ), 1.0F, 1, true);
    }

    private static ToolMaterial scaleDurability(ToolMaterial material, int multiplier) {

        return new ToolMaterial(material.incorrectBlocksForDrops(), material.durability() * multiplier, material.speed(), material.attackDamageBonus(), material.enchantmentValue(), material.repairItems());
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public SickleItem setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
