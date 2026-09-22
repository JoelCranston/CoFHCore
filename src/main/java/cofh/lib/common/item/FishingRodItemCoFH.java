package cofh.lib.common.item;

import cofh.lib.api.item.ICoFHItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.Random;

public class FishingRodItemCoFH extends FishingRodItem implements ICoFHItem {

    protected static Random random = new Random();
    protected int luckModifier;
    protected int speedModifier;

    public FishingRodItemCoFH(Properties builder) {

        super(builder);
    }

    public FishingRodItemCoFH(ItemTierCoFH tier, Properties builder) {

        super(builder.enchantable(tier.getMaterial().enchantmentValue()));
        setParams(tier.getLevel() / 2, (int) tier.getMaterial().speed() / 3);
    }

    public FishingRodItemCoFH setParams(int luckModifier, int speedModifier) {

        this.luckModifier = luckModifier;
        this.speedModifier = speedModifier;
        return this;
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {

        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.fishing != null) {
            if (!worldIn.isClientSide()) {
                int i = playerIn.fishing.retrieve(stack);
                stack.hurtAndBreak(i, playerIn, handIn.asEquipmentSlot());
            }
            playerIn.swing(handIn);
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        } else {
            worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            if (worldIn instanceof ServerLevel serverLevel) {
                int luck = EnchantmentHelper.getFishingLuckBonus(serverLevel, stack, playerIn) + luckModifier;
                int speed = Math.round(EnchantmentHelper.getFishingTimeReduction(serverLevel, stack, playerIn)) + speedModifier;
                worldIn.addFreshEntity(new FishingHook(playerIn, worldIn, luck, speed));
            }

            playerIn.swing(handIn);
            playerIn.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResult.SUCCESS;
    }

    // region DISPLAY
    protected String modId = "";

    @Override
    public FishingRodItemCoFH setModId(String modId) {

        this.modId = modId;
        return this;
    }

    @Override
    public String getCreatorModId(HolderLookup.Provider registries, ItemStack itemStack) {

        return modId == null || modId.isEmpty() ? super.getCreatorModId(registries, itemStack) : modId;
    }
    // endregion
}
