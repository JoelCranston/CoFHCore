package cofh.core.init;

import cofh.core.common.entity.ElectricField;
import cofh.core.common.entity.FrostField;
import cofh.core.common.entity.ThrownKnife;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;

import static cofh.core.CoFHCore.ENTITIES;
import static cofh.core.util.references.CoreIDs.*;

public class CoreEntities {

    private CoreEntities() {

    }

    public static void register() {

    }

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownKnife>> THROWN_KNIFE = ENTITIES.register(ID_KNIFE, id -> EntityType.Builder.<ThrownKnife>of(ThrownKnife::new, MobCategory.MISC).sized(0.5F, 0.5F).fireImmune().build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<ElectricField>> ELECTRIC_FIELD = ENTITIES.register(ID_ELECTRIC_FIELD, id -> EntityType.Builder.<ElectricField>of(ElectricField::new, MobCategory.MISC).sized(1.0F, 1.0F).fireImmune().noSave().build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    public static final DeferredHolder<EntityType<?>, EntityType<FrostField>> FROST_FIELD = ENTITIES.register(ID_FROST_FIELD, id -> EntityType.Builder.<FrostField>of(FrostField::new, MobCategory.MISC).sized(1.0F, 1.5F).fireImmune().noSave().build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

}
