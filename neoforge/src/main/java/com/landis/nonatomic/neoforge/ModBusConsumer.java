package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.registry.OperatorInfoRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Nonatomic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusConsumer {
    private static boolean regFlag = true;


    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        event.register(RegistriesImpl.OPERATOR_INFO);
        event.register(RegistriesImpl.OPERATOR_TYPE);

    }

    @SubscribeEvent
    public static void registry(RegisterEvent event) {
        if (regFlag) {
            Nonatomic.registryInit();
            regFlag = false;
        }
    }

}

