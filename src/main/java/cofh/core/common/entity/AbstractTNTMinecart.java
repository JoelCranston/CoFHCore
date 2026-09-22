package cofh.core.common.entity;

import cofh.lib.api.IDetonatable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import static cofh.lib.util.constants.NBTTags.TAG_FUSE;

public abstract class AbstractTNTMinecart extends AbstractMinecartCoFH implements IDetonatable {

    protected static final int CLOUD_DURATION = 20;

    protected int radius = 8;
    protected int fuse = -1;
    public int effectAmplifier = 1;
    public int effectDuration = 300;
    protected boolean detonated = false;

    public AbstractTNTMinecart(EntityType<?> type, Level worldIn) {

        super(type, worldIn);
    }

    public AbstractTNTMinecart(EntityType<?> type, Level worldIn, double posX, double posY, double posZ) {

        super(type, worldIn, posX, posY, posZ);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {

        return getBlock().defaultBlockState();
    }

    @Override
    public void tick() {

        super.tick();

        if (this.fuse > 0) {
            --this.fuse;
            this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
        } else if (this.fuse == 0) {
            this.explodeCart(getDeltaMovement().horizontalDistanceSqr());
        }
        if (this.horizontalCollision) {
            double d0 = getDeltaMovement().horizontalDistanceSqr();
            if (d0 >= (double) 0.01F) {
                this.explodeCart(d0);
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {

        Entity entity = source.getDirectEntity();
        if (entity instanceof AbstractArrow arrowEntity) {
            if (arrowEntity.isOnFire()) {
                this.explodeCart(arrowEntity.getDeltaMovement().lengthSqr());
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public void destroy(ServerLevel level, DamageSource source) {

        double d0 = this.getDeltaMovement().horizontalDistanceSqr();
        if (!source.is(DamageTypeTags.IS_FIRE) && !source.is(DamageTypeTags.IS_EXPLOSION) && !(d0 >= (double) 0.01F)) {
            detonated = true;
            super.destroy(level, source);
            if (!source.is(DamageTypeTags.IS_EXPLOSION) && level.getGameRules().get(GameRules.ENTITY_DROPS)) {
                this.spawnAtLocation(level, getBlock());
            }
        } else {
            if (this.fuse < 0) {
                this.ignite();
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }
        }
    }

    @Override
    public boolean causeFallDamage(double distance, float damageMultiplier, DamageSource source) {

        if (distance >= 3.0F) {
            float f = (float) distance / 10.0F;
            this.explodeCart(f * f);
        }
        return super.causeFallDamage(distance, damageMultiplier, source);
    }

    @Override
    public void activateMinecart(ServerLevel level, int x, int y, int z, boolean receivingPower) {

        if (receivingPower && this.fuse < 0) {
            this.ignite();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {

        if (id == 10) {
            this.ignite();
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosionIn, BlockGetter worldIn, BlockPos pos, BlockState blockStateIn, FluidState p_180428_5_, float p_180428_6_) {

        return !this.isIgnited() || !blockStateIn.is(BlockTags.RAILS) && !worldIn.getBlockState(pos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(explosionIn, worldIn, pos, blockStateIn, p_180428_5_, p_180428_6_) : 0.0F;
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosionIn, BlockGetter worldIn, BlockPos pos, BlockState blockStateIn, float p_174816_5_) {

        return (!this.isIgnited() || !blockStateIn.is(BlockTags.RAILS) && !worldIn.getBlockState(pos.above()).is(BlockTags.RAILS)) && super.shouldBlockExplode(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {

        super.readAdditionalSaveData(input);

        this.fuse = input.getIntOr(TAG_FUSE, this.fuse);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {

        super.addAdditionalSaveData(output);

        output.putInt(TAG_FUSE, this.fuse);
    }

    public void ignite() {

        this.fuse = 80;
        if (!this.level.isClientSide()) {
            this.level.broadcastEntityEvent(this, (byte) 10);
            if (!this.isSilent()) {
                this.level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public int getFuseTicks() {

        return this.fuse;
    }

    public boolean isIgnited() {

        return this.fuse > -1;
    }

    public abstract Block getBlock();

    protected void explodeCart(double speed) {

        double d0 = Math.sqrt(speed);
        if (d0 > 5.0D) {
            d0 = 5.0D;
        }
        radius += (int) level.getRandom().nextDouble() * 1.5 * d0;
        detonated = true;
        explode();
    }

    protected void explode() {

        if (level.isClientSide()) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY(), this.getZ(), 1.0D, 0.0D, 0.0D);
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0F, (1.0F + (this.level.getRandom().nextFloat() - this.level.getRandom().nextFloat()) * 0.2F) * 0.7F, false);
        } else {
            this.detonate(this.position());
            this.discard();
            this.spawnAtLocation((ServerLevel) level, getPickResult());
        }
    }

}
