package cofh.core.common.item;

import cofh.core.common.entity.ThrownKnife;
import cofh.lib.common.item.SwordItemCoFH;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BowItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class KnifeItem extends SwordItemCoFH implements ProjectileItem {

    private static final int DEFAULT_ATTACK_DAMAGE = 1;
    private static final float DEFAULT_ATTACK_SPEED = -2.0F;

    public KnifeItem(Tier tier, int attackDamageIn, float attackSpeedIn, Properties builder) {

        super(tier, attackDamageIn, attackSpeedIn, builder);

        // Custom per-item dispense-behavior subclasses are gone - ProjectileItem (implemented
        // below) plus this registration call is the modern equivalent (see ArrowItemCoFH).
        DispenserBlock.registerProjectileBehavior(this);
    }

    public KnifeItem(Tier tier, Properties builder) {

        this(tier, DEFAULT_ATTACK_DAMAGE, DEFAULT_ATTACK_SPEED, builder);
    }

    // 1.21: what an enchantment can go on is datapack data (an enchantment's supported_items
    // tag). NeoForge keeps a per-item override for the extra cases; Loyalty is allowed here the
    // way it was before, on top of whatever the data says.
    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {

        return enchantment.is(Enchantments.LOYALTY) || super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity living) {

        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {

        return UseAnim.SPEAR;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int durationRemaining) {

        if (living instanceof Player player) {
            float power = BowItem.getPowerForTime(this.getUseDuration(stack, living) - durationRemaining);
            if (power < 0.1D) {
                return;
            }
            if (!world.isClientSide) {
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
        }
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public KnifeItem setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(itemStack) : modId;
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
