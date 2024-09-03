package com.landis.nonatomic.fabric;

import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.core.OperatorType;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public class RegistriesImpl {
    public static void bootstrap(){}

    public static final MappedRegistry<Codec<? extends OperatorInfo>> OPERATOR_INFO = FabricRegistryBuilder.createSimple(Registries.Keys.OPERATOR_INFO).buildAndRegister();
    public static final MappedRegistry<OperatorType> OPERATOR_TYPE = FabricRegistryBuilder.createSimple(Registries.Keys.OPERATOR_TYPE).buildAndRegister();

    public static Registry<Codec<? extends OperatorInfo>> getOperatorInfoRegistry() {
        return OPERATOR_INFO;
    }

    public static Registry<OperatorType> getOperatorTypeRegistry(){
        return OPERATOR_TYPE;
    };
}
