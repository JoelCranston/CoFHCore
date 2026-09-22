package cofh.lib.common.entity;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

import static cofh.lib.util.constants.NBTTags.*;

public abstract class AbstractSpell extends Entity implements TraceableEntity {

    @Nullable
    protected Entity owner;
    @Nullable
    protected UUID ownerUUID;
    public float power;

    public AbstractSpell(EntityType<? extends AbstractSpell> type, Level level) {

        super(type, level);
        this.noPhysics = true;
    }

    public AbstractSpell(EntityType<? extends AbstractSpell> type, Level level, Vec3 pos, @Nullable Entity owner, float power) {

        this(type, level);
        this.noPhysics = true;
        moveTo(pos);
        if (owner != null) {
            this.owner = owner;
            this.ownerUUID = owner.getUUID();
        }
        this.power = power;
    }

    @Override
    public SoundSource getSoundSource() {

        Entity owner = getOwner();
        return owner == null ? SoundSource.NEUTRAL : owner.getSoundSource();
    }

    @Override
    public void tick() {

        if (firstTick) {
            onCast();
            this.firstTick = false;
        }
        if (!level.isClientSide() && tickCount > getDuration()) {
            onExpire();
            this.discard();
        } else {
            activeTick();
        }
    }

    protected abstract int getDuration();

    protected float getPower() {

        return power;
    }

    protected void onCast() {

    }

    protected void activeTick() {

    }

    protected void onExpire() {

    }

    @Override
    public Entity getOwner() {

        if (owner != null && !owner.isRemoved()) {
            return owner;
        }
        if (ownerUUID != null && level instanceof ServerLevel serverLevel && serverLevel.getEntity(this.ownerUUID) instanceof LivingEntity living) {
            this.owner = living;
            return this.owner;
        }
        return null;
    }

    @Override
    public boolean fireImmune() {

        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {

        output.storeNullable(TAG_OWNER, UUIDUtil.CODEC, ownerUUID);
        output.putFloat(TAG_POWER, this.power);
        output.putInt(TAG_AGE, this.tickCount);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {

        input.read(TAG_OWNER, UUIDUtil.CODEC).ifPresent(uuid -> this.ownerUUID = uuid);
        this.power = input.getFloatOr(TAG_POWER, 0.0F);
        this.tickCount = input.getIntOr(TAG_AGE, 0);
    }

    public boolean shouldRenderAtSqrDistance(double distSqr) {

        double d0 = 64.0D * getViewScale();
        return distSqr < d0 * d0;
    }

}
