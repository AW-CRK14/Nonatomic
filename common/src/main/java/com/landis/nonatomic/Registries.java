package com.landis.nonatomic;

import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.core.OperatorType;
import com.mojang.serialization.Codec;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class Registries {
    @ExpectPlatform
    public static Registry<Codec<? extends OperatorInfo>> getOperatorInfoRegistry(){
        throw new UnsupportedOperationException();
    };

    @ExpectPlatform
    public static Registry<OperatorType> getOperatorTypeRegistry(){
        throw new UnsupportedOperationException();
    };

    public static class Keys{

        public static final ResourceKey<Registry<Codec<? extends OperatorInfo>>> OPERATOR_INFO = create("operator_info");
        public static final ResourceKey<Registry<OperatorType>> OPERATOR_TYPE = create("operator_type");

        public static <T> ResourceKey<Registry<T>> create(String name) {
            return ResourceKey.createRegistryKey(new ResourceLocation(Nonatomic.MOD_ID, name));
        }
    }


}
