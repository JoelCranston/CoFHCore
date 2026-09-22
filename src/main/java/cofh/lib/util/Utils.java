package cofh.lib.util;

import cofh.core.util.ProxyUtils;
import cofh.lib.init.tags.ItemTagsCoFH;
import cofh.lib.util.helpers.MathHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static cofh.lib.util.Constants.MAX_CAPACITY;
import static cofh.lib.util.Constants.NETWORK_UPDATE_DISTANCE;

public class Utils {

    private Utils() {

    }

    public static boolean isModLoaded(String modid) {

        return ModList.get().isLoaded(modid);
    }

    public static boolean isClientWorld(Level world) {

        return world.isClientSide;
    }

    public static boolean isServerWorld(Level world) {

        return !world.isClientSide;
    }

    public static boolean isFakePlayer(Entity entity) {

        return entity instanceof FakePlayer;
    }

    public static boolean isCreativePlayer(Entity entity) {

        return entity instanceof Player player && player.isCreative();
    }

    public static EquipmentSlot handToEquipSlot(InteractionHand hand) {

        return hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    public static InteractionHand otherHand(InteractionHand hand) {

        return hand.equals(InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    public static EquipmentSlot otherHand(EquipmentSlot slot) {

        return slot.equals(EquipmentSlot.MAINHAND) ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
    }

    public static String createPrettyJSON(String jsonString) {

        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(json);
    }

    public static boolean spawnLightningBolt(Level world, BlockPos pos) {

        return spawnLightningBolt(world, pos, null);
    }

    public static boolean spawnLightningBolt(Level world, BlockPos pos, Entity caster) {

        if (Utils.isServerWorld(world)) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            bolt.setCause(caster instanceof ServerPlayer player ? player : null);
            world.addFreshEntity(bolt);
        }
        return true;
    }

    public static void spawnXpOrbs(Level world, int xp, Vec3 pos) {

        if (world == null) {
            return;
        }
        while (xp > 0) {
            int orbAmount = ExperienceOrb.getExperienceValue(xp);
            xp -= orbAmount;
            world.addFreshEntity(new ExperienceOrb(world, pos.x, pos.y, pos.z, orbAmount));
        }
    }

    public static boolean destroyBlock(Level world, BlockPos pos, boolean dropBlock, @Nullable Entity entityIn) {

        BlockState state = world.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(world, pos) < 0 || (entityIn instanceof Player player && state.getDestroyProgress(player, world, pos) < 0)) {
            return false;
        } else {
            FluidState ifluidstate = world.getFluidState(pos);
            if (dropBlock) {
                BlockEntity tileentity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;
                Block.dropResources(state, world, pos, tileentity, entityIn, ItemStack.EMPTY);
            }
            return world.setBlock(pos, ifluidstate.createLegacyBlock(), 3);
        }
    }

    public static boolean isWrench(ItemStack item) {

        return item.is(ItemTagsCoFH.TOOLS_WRENCH);
    }

    @Nullable
    public static String getKeynameFromKeycode(int code) {

        return GLFW.glfwGetKeyName(code, -1);
    }

    @Nullable
    public static String getKeyNameFromScanCode(int code) {

        return GLFW.glfwGetKeyName(-1, code);
    }

    public static void openEntityScreen(ServerPlayer player, MenuProvider containerSupplier, Entity entity) {

        player.openMenu(containerSupplier, buf -> buf.writeVarInt(entity.getId()));
    }

    public static <E extends Entity> E getEntityFromBuf(FriendlyByteBuf buf, Class<E> type) {

        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer.");
        }
        if (FMLEnvironment.dist.isClient()) {
            if (Minecraft.getInstance().level == null) {
                throw new IllegalStateException("Client world is null.");
            }
            int entityId = buf.readVarInt();
            Entity e = Minecraft.getInstance().level.getEntity(entityId);
            if (type.isInstance(e)) {
                // noinspection unchecked
                return (E) e;
            }
            throw new IllegalStateException("Client could not locate entity (id: " + entityId + ")  for entity container or the entity was of an invalid type. This is likely caused by a mod breaking client side entity lookup.");
        }
        return null;
    }

    public static boolean hurt(Entity target, DamageSource source, float amount, int invuln) {

        if (target instanceof PartEntity<?> part) {
            target = part.getParent();
        }
        int time = target.invulnerableTime;
        boolean hurt = target.hurt(source, amount);
        target.invulnerableTime = Math.max(time, invuln);
        return hurt;
    }

    public static DamageSource source(DamageSources sources, ResourceKey<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, Vec3 location) {

        return new DamageSource(sources.damageTypes.getHolderOrThrow(type), directEntity, causingEntity, location);
    }

    public static Item.Properties itemProperties() {

        return new Item.Properties();
    }

    // region TIME CHECKS
    public static final int TIME_CONSTANT = 40;
    public static final int TIME_CONSTANT_HALF = TIME_CONSTANT / 2;
    public static final int TIME_CONSTANT_QUARTER = TIME_CONSTANT / 4;

    private static int timeConstant = 0;
    private static int timeConstantHalf = 0;
    private static int timeConstantQuarter = 0;

    public static void tickTimeConstants() {

        if (++timeConstant >= TIME_CONSTANT) {
            timeConstant = 0;
        }
        if (++timeConstantHalf >= TIME_CONSTANT_HALF) {
            timeConstantHalf = 0;
        }
        if (++timeConstantQuarter >= TIME_CONSTANT_QUARTER) {
            timeConstantQuarter = 0;
        }
    }

    public static boolean timeCheck() {

        return timeConstant == 0;
    }

    public static boolean timeCheckHalf() {

        return timeConstantHalf == 0;
    }

    public static boolean timeCheckQuarter() {

        return timeConstantQuarter == 0;
    }
    // endregion

    // region PARTICLE UTILS
    public static void spawnBlockParticlesClient(Level world, ParticleOptions particle, BlockPos pos, RandomSource rand, int count) {

        for (int i = 0; i < count; ++i) {
            double d0 = (double) pos.getX() + rand.nextDouble();
            double d1 = (double) pos.getY() + rand.nextDouble();
            double d2 = (double) pos.getZ() + rand.nextDouble();
            double d3 = (rand.nextDouble() - 0.5D) * 0.5D;
            double d4 = (rand.nextDouble() - 0.5D) * 0.5D;
            double d5 = (rand.nextDouble() - 0.5D) * 0.5D;
            world.addParticle(particle, d0, d1, d2, d3, d4, d5);
        }
    }

    public static void spawnParticles(Level world, ParticleOptions particle, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {

        if (isServerWorld(world)) {
            ((ServerLevel) world).sendParticles(particle, posX, posY + 1.0D, posZ, particleCount, xOffset, yOffset, zOffset, speed);
        } else {
            world.addParticle(particle, posX + xOffset, posY + yOffset, posZ + zOffset, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, int count, Vec3 pos, float posVar, Vec3 velocity, float velVar) {

        for (int i = 0; i < count; ++i) {
            spawnParticles(level, particle, pos, posVar, velocity, velVar);
        }
    }

    public static void spawnParticles(Level level, ParticleOptions particle, Vec3 pos, float posVar, Vec3 velocity, float velVar) {

        RandomSource rand = level.getRandom();
        spawnParticles(level, particle, pos.add(rand.nextFloat() * posVar * 2 - posVar, rand.nextFloat() * posVar * 2 - posVar, rand.nextFloat() * posVar * 2 - posVar),
                velocity.add(rand.nextFloat() * velVar * 2 - velVar, rand.nextFloat() * velVar * 2 - velVar, rand.nextFloat() * velVar * 2 - velVar));
    }

    public static void spawnParticles(Level level, ParticleOptions particle, Vec3 pos, Vec3 velocity) {

        level.addParticle(particle, pos.x, pos.y, pos.z, velocity.x, velocity.y, velocity.z);
    }
    // endregion

    // region ENTITY UTILS
    public static boolean addToPlayerInventory(Player player, ItemStack stack) {

        if (stack.isEmpty() || player == null) {
            return false;
        }
        if (stack.getItem() instanceof ArmorItem armorItem) {
            int index = armorItem.getEquipmentSlot().getIndex();
            if (player.getInventory().armor.get(index).isEmpty()) {
                player.getInventory().armor.set(index, stack);
                return true;
            }
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); ++i) {
            if (inv.items.get(i).isEmpty()) {
                inv.items.set(i, stack.copy());
                return true;
            }
        }
        return false;
    }

    public static boolean addPotionEffectNoEvent(LivingEntity entity, MobEffectInstance effectInstanceIn) {

        if (!isPotionApplicableNoEvent(entity, effectInstanceIn)) {
            return false;
        } else {
            MobEffectInstance effectinstance = entity.getActiveEffectsMap().get(effectInstanceIn.getEffect());
            if (effectinstance == null) {
                entity.getActiveEffectsMap().put(effectInstanceIn.getEffect(), effectInstanceIn);
                entity.onEffectAdded(effectInstanceIn, null);
                return true;
            } else if (effectinstance.update(effectInstanceIn)) {
                entity.onEffectUpdated(effectinstance, true, null);
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean isPotionApplicableNoEvent(LivingEntity entity, MobEffectInstance potioneffectIn) {

        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
            Holder<MobEffect> effect = potioneffectIn.getEffect();
            return effect != MobEffects.REGENERATION && effect != MobEffects.POISON;
        }
        return true;
    }

    public static boolean dropItemStackIntoWorld(ItemStack stack, Level world, Vec3 pos) {

        return dropItemStackIntoWorld(stack, world, pos, false);
    }

    public static boolean dropItemStackIntoWorldWithRandomness(ItemStack stack, Level world, BlockPos pos) {

        return dropItemStackIntoWorld(stack, world, Vec3.atCenterOf(pos), true);
    }

    public static boolean dropItemStackIntoWorldWithRandomness(ItemStack stack, Level world, Vec3 pos) {

        return dropItemStackIntoWorld(stack, world, pos, true);
    }

    public static boolean dropItemStackIntoWorld(ItemStack stack, Level world, Vec3 pos, boolean velocity) {

        if (stack.isEmpty()) {
            return false;
        }
        float x2 = 0.5F;
        float y2 = 0.0F;
        float z2 = 0.5F;

        if (velocity) {
            x2 = world.random.nextFloat() * 0.8F + 0.1F;
            y2 = world.random.nextFloat() * 0.8F + 0.1F;
            z2 = world.random.nextFloat() * 0.8F + 0.1F;
        }
        ItemEntity entity = new ItemEntity(world, pos.x + x2, pos.y + y2, pos.z + z2, stack.copy());

        if (velocity) {
            entity.setDeltaMovement(world.random.nextGaussian() * 0.05F, world.random.nextGaussian() * 0.05F + 0.2F, world.random.nextGaussian() * 0.05F);
        } else {
            entity.setDeltaMovement(-0.05, 0, 0);
        }
        world.addFreshEntity(entity);

        return true;
    }

    public static boolean dropDismantleStackIntoWorld(ItemStack stack, Level world, BlockPos pos) {

        if (stack.isEmpty() || Utils.isClientWorld(world)) {
            return false;
        }
        float f = 0.3F;
        double x2 = world.random.nextFloat() * f + (1.0F - f) * 0.5D;
        double y2 = world.random.nextFloat() * f + (1.0F - f) * 0.5D;
        double z2 = world.random.nextFloat() * f + (1.0F - f) * 0.5D;
        ItemEntity dropEntity = new ItemEntity(world, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2, stack);
        dropEntity.setPickUpDelay(10);
        world.addFreshEntity(dropEntity);

        return true;
    }

    public static boolean teleportEntityTo(Entity entity, BlockPos pos) {

        return teleportEntityTo(entity, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }

    public static boolean teleportEntityTo(Entity entity, double x, double y, double z) {

        if (entity instanceof LivingEntity) {
            return teleportEntityTo((LivingEntity) entity, x, y, z);
        } else {
            entity.moveTo(x, y, z, entity.getYRot(), entity.getXRot());
            entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        }
        return true;
    }

    public static boolean teleportEntityTo(LivingEntity entity, double x, double y, double z) {

        EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(entity, x, y, z);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return false;
        }
        if (entity instanceof ServerPlayer player && !isFakePlayer(entity)) {
            if (player.connection.isAcceptingMessages() && !player.isSleeping()) {
                if (entity.isPassenger()) {
                    entity.stopRiding();
                }
                entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
                entity.fallDistance = 0.0F;
            }
        } else {
            if (entity.isPassenger()) {
                entity.stopRiding();
            }
            entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
            entity.fallDistance = 0.0F;
        }
        entity.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
        return true;
    }
    // endregion

    // region ENCHANT UTILS
    // Enchantments switched off in config read as level 0 in every lookup below.
    private static final Set<ResourceKey<Enchantment>> DISABLED_ENCHANTMENTS = ConcurrentHashMap.newKeySet();

    public static void setEnchantmentEnabled(ResourceKey<Enchantment> enchant, boolean enabled) {

        if (enabled) {
            DISABLED_ENCHANTMENTS.remove(enchant);
        } else {
            DISABLED_ENCHANTMENTS.add(enchant);
        }
    }

    public static boolean isEnchantmentEnabled(ResourceKey<Enchantment> enchant) {

        return enchant != null && !DISABLED_ENCHANTMENTS.contains(enchant);
    }

    public static ResourceKey<Enchantment> getEnchantment(String modId, String enchantId) {

        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(modId, enchantId));
    }

    @Nullable
    public static Holder<Enchantment> getEnchantmentHolder(ResourceKey<Enchantment> enchant) {

        if (enchant == null) {
            return null;
        }
        HolderLookup.Provider registries = ProxyUtils.registryAccess();
        return registries.lookup(Registries.ENCHANTMENT).flatMap(lookup -> lookup.get(enchant)).orElse(null);
    }

    public static int getEnchantedCapacity(int amount, int holding) {

        return MathHelper.clamp(amount + amount * holding / 2, 0, MAX_CAPACITY);
    }

    public static int getItemEnchantmentLevel(ResourceKey<Enchantment> enchant, ItemStack stack) {

        if (!isEnchantmentEnabled(enchant) || stack.isEmpty()) {
            return 0;
        }
        return getLevel(stack.getEnchantments(), enchant);
    }

    public static int getHeldEnchantmentLevel(LivingEntity living, ResourceKey<Enchantment> enchant) {

        if (!isEnchantmentEnabled(enchant)) {
            return 0;
        }
        return Math.max(getItemEnchantmentLevel(enchant, living.getMainHandItem()), getItemEnchantmentLevel(enchant, living.getOffhandItem()));
    }

    public static int getMaxEquippedEnchantmentLevel(LivingEntity living, ResourceKey<Enchantment> enchant) {

        if (!isEnchantmentEnabled(enchant)) {
            return 0;
        }
        int max = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            max = Math.max(max, getItemEnchantmentLevel(enchant, living.getItemBySlot(slot)));
        }
        return max;
    }

    public static void addEnchantment(ItemStack stack, ResourceKey<Enchantment> enchant, int level) {

        Holder<Enchantment> holder = getEnchantmentHolder(enchant);
        if (holder != null) {
            stack.enchant(holder, level);
        }
    }

    public static void removeEnchantment(ItemStack stack, ResourceKey<Enchantment> enchant) {

        ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
        if (enchantments == null || getLevel(enchantments, enchant) <= 0) {
            return;
        }
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
        mutable.removeIf(holder -> holder.is(enchant));
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
    }

    public static int getLevel(ItemEnchantments enchantments, ResourceKey<Enchantment> enchant) {

        if (enchant == null) {
            return 0;
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (entry.getKey().is(enchant)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }
    // endregion

    // region REGISTRY NAME
    public static ResourceLocation getRegistryName(Block block) {

        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public static ResourceLocation getRegistryName(Item item) {

        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation getRegistryName(Fluid fluid) {

        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static ResourceLocation getRegistryName(EntityType entity) {

        return BuiltInRegistries.ENTITY_TYPE.getKey(entity);
    }

    public static ResourceLocation getRegistryName(MobEffect effect) {

        return BuiltInRegistries.MOB_EFFECT.getKey(effect);
    }
    // endregion

    // region NAMESPACE
    public static String getModId(Block block) {

        ResourceLocation loc = getRegistryName(block);
        return loc == null ? "" : loc.getNamespace();
    }

    public static String getName(Block block) {

        ResourceLocation loc = getRegistryName(block);
        return loc == null ? "" : loc.getPath();
    }

    public static String getModId(Item item) {

        ResourceLocation loc = getRegistryName(item);
        return loc == null ? "" : loc.getNamespace();
    }

    public static String getModId(ItemStack stack) {

        ResourceLocation loc = getRegistryName(stack.getItem());
        return loc == null ? "" : loc.getNamespace();
    }

    public static String getName(Item item) {

        ResourceLocation loc = getRegistryName(item);
        return loc == null ? "" : loc.getPath();
    }

    public static String getName(ItemStack stack) {

        ResourceLocation loc = getRegistryName(stack.getItem());
        return loc == null ? "" : loc.getPath();
    }

    public static String getModId(Fluid fluid) {

        ResourceLocation loc = getRegistryName(fluid);
        return loc == null ? "" : loc.getNamespace();
    }

    public static String getModId(FluidStack stack) {

        ResourceLocation loc = getRegistryName(stack.getFluid());
        return loc == null ? "" : loc.getNamespace();
    }

    public static String getName(Fluid fluid) {

        ResourceLocation loc = getRegistryName(fluid);
        return loc == null ? "" : loc.getPath();
    }

    public static String getName(FluidStack stack) {

        ResourceLocation loc = getRegistryName(stack.getFluid());
        return loc == null ? "" : loc.getPath();
    }
    // endregion

    // region PACKET UTILS

    public static void sendNear(Entity entity, CustomPacketPayload payload) {

        sendNear(entity, NETWORK_UPDATE_DISTANCE, payload);
    }

    public static void sendNear(Entity entity, int radius, CustomPacketPayload payload) {

        if (entity.level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, entity.getX(), entity.getY(), entity.getZ(), radius, payload);
        }
    }

    public static void sendNear(Level level, BlockPos pos, CustomPacketPayload payload) {

        sendNear(level, pos, NETWORK_UPDATE_DISTANCE, payload);
    }

    public static void sendNear(Level level, BlockPos pos, int radius, CustomPacketPayload payload) {

        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersNear(serverLevel, null, pos.getX(), pos.getY(), pos.getZ(), radius, payload);
        }
    }
    // endregion
}
