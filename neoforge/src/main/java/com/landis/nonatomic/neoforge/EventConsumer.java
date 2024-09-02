package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.Nonatomic;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NewRegistryEvent;

public class EventConsumer {
    @Mod.EventBusSubscriber(modid = Nonatomic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public class ModBusConsumer {
        @SubscribeEvent
        public static void newRegistry(NewRegistryEvent event) {
            event.register(RegistriesImpl.OPERATOR_INFO);
            event.register(RegistriesImpl.OPERATOR_TYPE);
        }
    }
}
