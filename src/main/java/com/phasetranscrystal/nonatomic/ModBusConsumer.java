package com.phasetranscrystal.nonatomic;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModBusConsumer {
    @SubscribeEvent
    public static void newRegistryType(NewRegistryEvent event){
        event.register(Registries.OPERATOR_INFO);
        event.register(Registries.OPERATOR_TYPE);
    }
}
