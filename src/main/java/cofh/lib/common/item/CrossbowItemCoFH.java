package cofh.lib.common.item;

import cofh.core.common.capability.CoreCapabilities;
import cofh.core.common.capability.templates.ArcheryAmmoItemWrapper;
import cofh.core.util.ProxyUtils;
import cofh.core.util.helpers.ArcheryHelper;
import cofh.lib.api.capability.IArcheryAmmoItem;
import cofh.lib.api.item.ICoFHItem;
import cofh.lib.util.Utils;
import cofh.lib.util.helpers.MathHelper;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

public class CrossbowItemCoFH extends CrossbowItem implements ICoFHItem {

    protected float accuracyModifier = 1.0F;
    protected float damageModifier = 1.0F;
    protected float velocityModifier = 1.0F;

    public CrossbowItemCoFH(Properties builder) {

        super(builder);
        ProxyUtils.registerItemModelProperty(this, Identifier.parse("pull"), this::getPullModelProperty);
        ProxyUtils.registerItemModelProperty(this, Identifier.parse("ammo"), this::getAmmoModelProperty);
    }

    public CrossbowItemCoFH(ToolMaterial material, Properties builder) {

        this(builder.enchantable(material.enchantmentValue()));
        setParams(material);
    }

    public CrossbowItemCoFH(int enchantability, float accuracyModifier, float damageModifier, float velocityModifier, Properties builder) {

        this(builder.enchantable(enchantability));
        setParams(accuracyModifier, damageModifier, velocityModifier);
    }

    public CrossbowItemCoFH setParams(ToolMaterial material) {

        this.damageModifier = material.attackDamageBonus() / 4;
        this.velocityModifier = material.speed() / 20;
        return this;
    }

    public CrossbowItemCoFH setParams(float accuracyModifier, float damageModifier, float velocityModifier) {

        this.accuracyModifier = accuracyModifier;
        this.damageModifier = damageModifier;
        this.velocityModifier = velocityModifier;
        return this;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {

        if (isLoaded(stack)) {
            tooltip.accept((Component.translatable("info.cofh.crossbow_loaded")).append(" ").append(getLoadedAmmo(stack).getDisplayName()));
        }
    }

    public float getPullModelProperty(ItemStack stack, Level level, LivingEntity entity, int seed) {

        if (entity == null || !entity.getUseItem().equals(stack)) {
            return 0.0F;
        }
        int baseDuration = getUseDuration(stack, entity);
        int duration = baseDuration - entity.getUseItemRemainingTicks();

        return MathHelper.clamp((float) (duration) / baseDuration, 0.0F, 1.0F);
    }

    public float getAmmoModelProperty(ItemStack stack, Level level, LivingEntity entity, int seed) {

        if (getLoadedAmmo(stack).isEmpty()) {
            return 0F;
        }
        if (getLoadedAmmo(stack).getItem() instanceof FireworkRocketItem) {
            return 0.5F;
        }
        return 1.0F;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {

        ItemStack stack = player.getItemInHand(hand);
        if (isLoaded(stack)) {
            setLoaded(stack, !shootLoadedAmmo(level, player, hand, stack));
            return InteractionResult.CONSUME;
        } else if (!ArcheryHelper.findAmmo(player, stack).isEmpty() || player.abilities.instabuild) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity living) {

        return Math.max(0, getChargeDuration(stack, living)) + 1;
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int durationRemaining) {

        if (!level.isClientSide()) {
            int totalDuration = getUseDuration(stack, living);
            int duration = totalDuration - durationRemaining;

            if (duration == totalDuration / 4) {
                level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_START, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
            if (duration == totalDuration / 2) {
                level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity living, int durationRemaining) {

        if (durationRemaining < 0 && !isLoaded(stack) && loadAmmo(living, stack)) {
            level.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.CROSSBOW_LOADING_END, living instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
            return true;
        }
        return false;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {

        return true;
    }

    // region HELPER
    public boolean loadAmmo(LivingEntity living, ItemStack crossbow) {

        if (living instanceof Player player) {
            ItemStack ammo = ArcheryHelper.findAmmo(player, crossbow);
            if (!ammo.isEmpty() && ammo.getItem() instanceof FireworkRocketItem) {
                boolean success = loadAmmo(player, crossbow, ammo);
                if (success && !player.abilities.instabuild) {
                    ammo.shrink(1);
                }
                return success;
            }
            IArcheryAmmoItem ammoCap = ammo.getCapability(CoreCapabilities.ArcheryHandler.AMMO);

            if (ammoCap == null) {
                ammoCap = new ArcheryAmmoItemWrapper(ammo);
            }
            boolean infinite = player.abilities.instabuild
                    || ammoCap.isInfinite(crossbow, player)
                    || (ArcheryHelper.isArrow(ammo) && ((ArrowItem) ammo.getItem()).isInfinite(ammo, crossbow, player));
            if (!ammo.isEmpty() || infinite) {
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(Items.ARROW);
                }
                boolean success = loadAmmo(player, crossbow, ammo);
                if (success && !infinite) {
                    ammoCap.onArrowLoosed(player);
                    if (ammo.isEmpty()) {
                        player.inventory.removeItem(ammo);
                    }
                }
                return success;
            }
        }
        return false;
    }

    public boolean loadAmmo(Player player, ItemStack crossbow, ItemStack ammo) {

        crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(List.of(ammo.copy())));
        return true;
    }

    public ItemStack getLoadedAmmo(ItemStack crossbow) {

        List<ItemStack> loaded = crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).itemCopies();
        return loaded.isEmpty() ? ItemStack.EMPTY : loaded.get(0);
    }

    public void removeLoadedAmmo(ItemStack crossbow) {

        crossbow.remove(DataComponents.CHARGED_PROJECTILES);
    }

    // Overrideable forms of isCharged() and setCharged() in CrossbowItem.
    public void setLoaded(ItemStack crossbow, boolean loaded) {

        if (!loaded) {
            removeLoadedAmmo(crossbow);
        }
    }

    public boolean isLoaded(ItemStack crossbow) {

        return !crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty();
    }

    // Returns true if the shot succeeded (i.e. if the crossbow should be unloaded after this method is called).
    public boolean shootLoadedAmmo(Level level, LivingEntity living, InteractionHand hand, ItemStack crossbow) {

        // TODO: dmg/acc/vel modifiers
        if (living instanceof Player shooter) {
            ItemStack ammo = getLoadedAmmo(crossbow);
            if (!ammo.isEmpty()) {
                int multishot = Utils.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
                int damage = 0;
                for (int i = -multishot; i <= multishot; ++i) {
                    if (!ammo.isEmpty()) {
                        Projectile projectile;
                        if (ammo.getCapability(CoreCapabilities.ArcheryHandler.AMMO) != null || ammo.getItem() instanceof ArrowItem) {
                            AbstractArrow arrow = ArcheryHelper.createArrow(level, ammo, shooter);
                            projectile = adjustArrow(crossbow, arrow, shooter.abilities.instabuild || i != 0);
                            ++damage;
                        } else if (ammo.getItem() instanceof FireworkRocketItem) {
                            projectile = new FireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - (double) 0.15F, shooter.getZ(), true);
                            damage += 3;
                        } else {
                            return false;
                        }
                        level.addFreshEntity(shootProjectile(shooter, projectile, getBaseSpeed(ammo), 1.0F, i * 10.F));
                        float pitch = level.getRandom().nextFloat() * 0.32F + 0.865F;
                        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, pitch);
                    }
                }
                onCrossbowShot(shooter, hand, crossbow, damage);
            }
        }
        removeLoadedAmmo(crossbow);
        return true;
    }

    public float getBaseSpeed(ItemStack ammo) {

        return ammo.getItem() instanceof FireworkRocketItem ? 1.6F : 3.15F;
    }

    public AbstractArrow adjustArrow(ItemStack crossbow, AbstractArrow arrow, boolean creativePickup) {

        arrow.setCritArrow(true);
        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        arrow.firedFromWeapon = crossbow.copy();
        if (arrow.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.onProjectileSpawned(serverLevel, crossbow, arrow, item -> {});
        }
        if (creativePickup) {
            arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        }
        return arrow;
    }

    public Projectile shootProjectile(Player shooter, Projectile projectile, float speed, float inaccuracy, float angle) {

        Vec3 vec31 = shooter.getUpVector(1.0F);
        Quaternionf quaternionf = (new Quaternionf()).setAngleAxis((angle * ((float) Math.PI / 180F)), vec31.x, vec31.y, vec31.z);
        Vec3 vec3 = shooter.getViewVector(1.0F);
        Vector3f vector3f = vec3.toVector3f().rotate(quaternionf);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), speed, inaccuracy);
        return projectile;
    }

    // Override to change behavior to e.g. extract energy instead of using durability.
    public void onCrossbowShot(Player shooter, InteractionHand hand, ItemStack crossbow, int damage) {

        crossbow.hurtAndBreak(damage, shooter, hand.asEquipmentSlot());
        if (shooter instanceof ServerPlayer) {
            if (!shooter.level.isClientSide()) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger((ServerPlayer) shooter, crossbow);
            }
            shooter.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }
    }
    // endregion

    // region DISPLAY
    protected String modId = "";

    @Override
    public CrossbowItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
