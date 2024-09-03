package com.landis.nonatomic.fabric;

import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import net.fabricmc.api.ModInitializer;

public final class Nonatomic implements ModInitializer {
    @Override
    public void onInitialize() {
        RegistriesImpl.bootstrap();

        // Run our common setup.
        com.landis.nonatomic.Nonatomic.init();
    }
}
