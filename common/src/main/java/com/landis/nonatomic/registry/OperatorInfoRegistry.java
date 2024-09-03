package com.landis.nonatomic.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.OperatorInfo;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;

public class OperatorInfoRegistry {
    public static final DeferredRegister<Codec<? extends OperatorInfo>> REGISTRY = DeferredRegister.create(Nonatomic.MOD_ID, Registries.Keys.OPERATOR_INFO);

//    public static final RegistrySupplier<Codec<Deploy>> DEPLOY = REGISTRY.register("deploy", () -> Deploy.CODEC);

//    public static final RegistrySupplier<Codec<OperatorInfo<?>>> DEPLOY = REGISTRY.register("deploy", () -> getOperatorInfoCodec(Deploy.CODEC));
//
//
//    public static Codec<OperatorInfo<?>> getOperatorInfoCodec(Codec<? extends OperatorInfo<?>> codec) {
//        return (Codec<OperatorInfo<?>>) codec;
//    }
}
