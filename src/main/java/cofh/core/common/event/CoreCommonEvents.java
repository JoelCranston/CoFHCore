package cofh.core.common.event;

import cofh.core.common.config.CoreCommonConfig;
import cofh.core.common.config.CoreEnchantConfig;
import cofh.core.util.helpers.XpHelper;
import cofh.lib.util.Utils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static cofh.core.init.CoreMobEffects.SLIMED;
import static cofh.lib.util.Utils.getItemEnchantmentLevel;
import static cofh.lib.util.Utils.getMaxEquippedEnchantmentLevel;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;
import static net.minecraft.world.item.enchantment.Enchantments.FEATHER_FALLING;
import static net.minecraft.world.item.enchantment.Enchantments.MENDING;

@EventBusSubscriber (modid = ID_COFH_CORE)
public class CoreCommonEvents {

    private CoreCommonEvents() {

    }

    // Horse armor and shields have no enchantability of their own.
    @SubscribeEvent
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {

        for (Item item : List.of(Items.LEATHER_HORSE_ARMOR, Items.IRON_HORSE_ARMOR, Items.GOLDEN_HORSE_ARMOR, Items.DIAMOND_HORSE_ARMOR, Items.NETHERITE_HORSE_ARMOR, Items.WOLF_ARMOR)) {
            event.modify(item, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(15)));
        }
        event.modify(Items.SHIELD, builder -> builder.set(DataComponents.ENCHANTABLE, new Enchantable(1)));
    }

    @SubscribeEvent
    public static void handleFarmlandTrampleEvent(BlockEvent.FarmlandTrampleEvent event) {

        if (event.isCanceled()) {
            return;
        }
        if (!CoreEnchantConfig.improvedFeatherFalling()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            int encFeatherFalling = getMaxEquippedEnchantmentLevel((LivingEntity) entity, FEATHER_FALLING);
            if (encFeatherFalling > 0) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void handleLivingFallEvent(LivingFallEvent event) {

        if (event.isCanceled()) {
            return;
        }
        if (event.getDistance() >= 3.0) {
            LivingEntity living = event.getEntity();
            if (living.hasEffect(SLIMED)) {
                Vec3 motion = living.getDeltaMovement();
                living.setDeltaMovement(motion.x, 0.08 * Math.sqrt(event.getDistance() / 0.08), motion.z);
                living.hurtMarked = true;
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleItemFishedEvent(ItemFishedEvent event) {

        if (event.isCanceled()) {
            return;
        }
        if (!CoreCommonConfig.enableFishingExhaustion()) {
            return;
        }
        Entity player = event.getHookEntity().getOwner();
        if (!(player instanceof Player) || player instanceof FakePlayer) {
            return;
        }
        ((Player) player).causeFoodExhaustion(CoreCommonConfig.amountFishingExhaustion());
    }

    @SubscribeEvent (priority = EventPriority.LOW)
    public static void handlePickupXpEvent(PlayerXpEvent.PickupXp event) {

        if (event.isCanceled()) {
            return;
        }
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();

        // Improved Mending
        if (CoreEnchantConfig.improvedMending()) {
            player.takeXpDelay = 2;
            player.take(orb, 1);

            Map.Entry<EquipmentSlot, ItemStack> entry = getMostDamagedItem(player);
            if (entry != null) {
                ItemStack itemstack = entry.getValue();
                if (!itemstack.isEmpty() && itemstack.isDamaged()) {
                    int i = Math.min((int) (orb.getValue() * itemstack.getXpRepairRatio()), itemstack.getDamageValue());
                    orb.setValue(orb.getValue() - durabilityToXp(i));
                    itemstack.setDamageValue(itemstack.getDamageValue() - i);
                }
            }
            XpHelper.attemptStoreXP(player, orb);
            if (orb.getValue() > 0) {
                player.giveExperiencePoints(orb.getValue());
            }
            orb.discard();
            event.setCanceled(true);
            return;
        }
        XpHelper.attemptStoreXP(player, orb);
    }

    @SubscribeEvent (priority = EventPriority.LOWEST)
    public static void handleSaplingGrowTreeEvent(BlockGrowFeatureEvent event) {

        if (!CoreCommonConfig.enableSaplingGrowthMod()) {
            return;
        }
        if (event.getRandom().nextInt(CoreCommonConfig.amountSaplingGrowthMod()) != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void serverTick(ServerTickEvent.Pre event) {

        Utils.tickTimeConstants();
    }

    // region HELPERS
    private static Map.Entry<EquipmentSlot, ItemStack> getMostDamagedItem(Player player) {

        Map<EquipmentSlot, ItemStack> map = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack slotStack = player.getItemBySlot(slot);
            if (!slotStack.isEmpty()) {
                map.put(slot, slotStack);
            }
        }
        Map.Entry<EquipmentSlot, ItemStack> mostDamaged = null;
        if (map.isEmpty()) {
            return null;
        }
        double durability = 0.0D;

        for (Map.Entry<EquipmentSlot, ItemStack> entry : map.entrySet()) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty() && getItemEnchantmentLevel(MENDING, stack) > 0) {
                if (calcDurabilityRatio(stack) > durability) {
                    mostDamaged = entry;
                    durability = calcDurabilityRatio(stack);
                }
            }
        }
        return mostDamaged;
    }

    private static int durabilityToXp(int durability) {

        return durability / 2;
    }

    private static int xpToDurability(int xp) {

        return xp * 2;
    }

    private static double calcDurabilityRatio(ItemStack stack) {

        return (double) stack.getDamageValue() / stack.getMaxDamage();
    }
    // endregion
}
