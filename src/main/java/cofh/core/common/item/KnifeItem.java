package cofh.core.common.item;

import cofh.core.common.entity.ThrownKnife;
import cofh.lib.common.item.SwordItemCoFH;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class KnifeItem extends SwordItemCoFH implements ProjectileItem {

    private static final int DEFAULT_ATTACK_DAMAGE = 1;
    private static final float DEFAULT_ATTACK_SPEED = -2.0F;

    public KnifeItem(ToolMaterial material, int attackDamageIn, float attackSpeedIn, Properties builder) {

        super(material, attackDamageIn, attackSpeedIn, builder);

        DispenserBlock.registerProjectileBehavior(this);
    }

    public KnifeItem(ToolMaterial material, Properties builder) {

        this(material, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, builder);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {

        return enchantment.is(Enchantments.LOYALTY) || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity living) {

        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {

        return ItemUseAnimation.SPEAR;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity living, int durationRemaining) {

        if (living instanceof Player player) {
            float power = BowItem.getPowerForTime(this.getUseDuration(stack, living) - durationRemaining);
            if (power < 0.1D) {
                return false;
            }
            if (!world.isClientSide()) {
                ThrownKnife knife = new ThrownKnife(world, player, stack);
                knife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power * 3.0F, 0.1F);
                if (player.getAbilities().instabuild) {
                    knife.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                world.addFreshEntity(knife);
            }
            world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F / (MathHelper.RANDOM.nextFloat() * 0.4F + 1.2F) + power * 0.5F);
            if (!player.getAbilities().instabuild) {
                player.getInventory().removeItem(stack);
            }
            player.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }
        return false;
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public KnifeItem setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion

    // region DISPENSER BEHAVIOR
    @Override
    public Projectile asProjectile(Level worldIn, Position position, ItemStack stackIn, Direction direction) {

        ThrownKnife knife = new ThrownKnife(worldIn, position.x(), position.y(), position.z(), stackIn);
        knife.pickup = AbstractArrow.Pickup.ALLOWED;
        return knife;
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {

        ProjectileItem.DispenseConfig defaults = ProjectileItem.super.createDispenseConfig();
        return new ProjectileItem.DispenseConfig(defaults.positionFunction(), 3.0F, defaults.power(), defaults.overrideDispenseEvent());
    }
    // endregion
}
