package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.core.OperatorType;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class RegistriesImpl {

    public static final Registry<Codec<? extends OperatorInfo>> OPERATOR_INFO = new RegistryBuilder<>(Registries.Keys.OPERATOR_INFO).sync(true).create();
    public static final Registry<OperatorType> OPERATOR_TYPE = new RegistryBuilder<>(Registries.Keys.OPERATOR_TYPE).sync(true).create();

    public static Registry<Codec<? extends OperatorInfo>> getOperatorInfoRegistry() {
        return OPERATOR_INFO;
    }

    public static Registry<OperatorType> getOperatorTypeRegistry(){
        return OPERATOR_TYPE;
    };
}
