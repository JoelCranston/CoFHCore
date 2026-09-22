package cofh.core.common.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;

import static cofh.core.util.references.CoreIDs.ID_HOLDING;
import static cofh.lib.util.Utils.getEnchantment;
import static cofh.lib.util.constants.ModIds.ID_COFH_CORE;
import static cofh.lib.util.constants.NBTTags.TAG_ENCHANTMENTS;

public abstract class AbstractMinecartCoFH extends AbstractMinecart {

    protected ItemEnchantments enchantments = ItemEnchantments.EMPTY;

    protected AbstractMinecartCoFH(EntityType<?> type, Level worldIn) {

        super(type, worldIn);
    }

    protected AbstractMinecartCoFH(EntityType<?> type, Level worldIn, double posX, double posY, double posZ) {

        super(type, worldIn, posX, posY, posZ);
    }

    public AbstractMinecartCoFH onPlaced(ItemStack stack) {

        this.enchantments = stack.getEnchantments();
        return this;
    }

    protected float getHoldingMod(Map<Enchantment, Integer> enchantmentMap) {

        int holding = enchantmentMap.getOrDefault(getEnchantment(ID_COFH_CORE, ID_HOLDING), 0);
        return 1 + holding / 2F;
    }

    public ItemStack createItemStackTag(ItemStack stack) {

        if (this.hasCustomName()) {
            stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        }
        if (!this.enchantments.isEmpty()) {
            stack.set(DataComponents.ENCHANTMENTS, this.enchantments);
        }
        return stack;
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {

        super.readAdditionalSaveData(input);

        enchantments = input.read(TAG_ENCHANTMENTS, ItemEnchantments.CODEC).orElse(ItemEnchantments.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {

        super.addAdditionalSaveData(output);

        if (!enchantments.isEmpty()) {
            output.store(TAG_ENCHANTMENTS, ItemEnchantments.CODEC, enchantments);
        }
    }

    @Override
    public void destroy(DamageSource source) {

        this.remove(Entity.RemovalReason.KILLED);
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            ItemStack stack = createItemStackTag(getPickResult());
            this.spawnAtLocation(stack);
        }
    }

}
