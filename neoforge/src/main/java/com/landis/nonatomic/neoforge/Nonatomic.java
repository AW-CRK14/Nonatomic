package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.neoforge.registry.DataAttachmentRegistry;
import dev.architectury.platform.hooks.EventBusesHooks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(com.landis.nonatomic.Nonatomic.MOD_ID)
public final class Nonatomic {
    public Nonatomic(IEventBus bus) {
        DataAttachmentRegistry.REGISTER.register(bus);

        // Run our common setup.
        com.landis.nonatomic.Nonatomic.init();
    }
}
