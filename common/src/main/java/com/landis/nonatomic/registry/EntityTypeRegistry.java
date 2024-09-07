package com.landis.nonatomic.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.OperatorEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<OperatorEntity>> TEST = REGISTER.register("test", () -> EntityType.Builder.<OperatorEntity>of(OperatorEntity::new, MobCategory.MISC).sized(0.6F,1.9F).clientTrackingRange(32).canSpawnFarFromPlayer().build("test"));

}
