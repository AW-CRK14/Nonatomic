package com.landis.nonatomic.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.extend.TestOperator;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;

public class OperatorTypeRegistry {
    public static final DeferredRegister<OperatorType> REGISTER = DeferredRegister.create(Nonatomic.MOD_ID, Registries.Keys.OPERATOR_TYPE);

    public static final RegistrySupplier<OperatorType.Placeholder> PLACE_HOLDER = REGISTER.register("placeholder", OperatorType.Placeholder::new);
    public static final RegistrySupplier<TestOperator> TEST_OPERATOR = REGISTER.register("test", TestOperator::new);
}
