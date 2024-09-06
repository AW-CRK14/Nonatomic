package com.landis.nonatomic.extend;

import com.landis.nonatomic.core.OpeHandler;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.registry.EntityTypeRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public class TestOperator extends OperatorType {
    @Override
    public Operator createDefaultInstance(OpeHandler handler) {
        return new Operator(OperatorTypeRegistry.TEST_OPERATOR.get());
    }

    @Override
    public @Nullable EntityType<? extends OperatorEntity> getEntityType() {
        return EntityTypeRegistry.TEST.get();
    }
}
