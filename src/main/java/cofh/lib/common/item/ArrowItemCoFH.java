package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.DispenserBlock;

import static cofh.lib.util.Utils.getItemEnchantmentLevel;

public class ArrowItemCoFH extends ArrowItem implements ICoFHItem {

    protected final IArrowFactory<? extends AbstractArrow> factory;
    protected boolean infinitySupport = false;

    public ArrowItemCoFH(IArrowFactory<? extends AbstractArrow> factory, Properties builder) {

        super(builder);
        this.factory = factory;

        // The custom per-item ProjectileDispenseBehavior subclass pattern is gone - dispensing is
        // now driven by the ProjectileItem interface (which ArrowItem already implements) via
        // asProjectile(...) below; registerProjectileBehavior wires that up for this item.
        DispenserBlock.registerProjectileBehavior(this);
    }

    public ArrowItemCoFH setInfinitySupport(boolean infinitySupport) {

        this.infinitySupport = infinitySupport;
        return this;
    }

    @Override
    public AbstractArrow createArrow(Level worldIn, ItemStack stack, LivingEntity shooter, @Nullable ItemStack weapon) {

        return factory.createArrow(worldIn, shooter);
    }

    // Position-based spawn for dispensers (no shooter entity) - replaces the old dispenser
    // behavior's getProjectile(Level, Position, ItemStack) override.
    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack stack, Direction direction) {

        AbstractArrow arrow = factory.createArrow(level, position.x(), position.y(), position.z());
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, LivingEntity shooter) {

        return infinitySupport && getItemEnchantmentLevel(Enchantments.INFINITY, bow) > 0 || super.isInfinite(stack, bow, shooter);
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public ArrowItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
    }
    // endregion

    // region FACTORY
    public interface IArrowFactory<T extends AbstractArrow> {

        T createArrow(Level world, LivingEntity living);

        T createArrow(Level world, double posX, double posY, double posZ);

    }
    // endregion
}
