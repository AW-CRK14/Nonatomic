package com.landis.nonatomic.core;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public abstract class OperatorEntity extends LivingEntity {
    protected OperatorEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
}
