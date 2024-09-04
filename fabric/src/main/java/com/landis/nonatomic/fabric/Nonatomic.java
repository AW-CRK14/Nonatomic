package com.landis.nonatomic.fabric;

import net.fabricmc.api.ModInitializer;

public final class Nonatomic implements ModInitializer {
    @Override
    public void onInitialize() {
        RegistriesImpl.bootstrap();

        // Run our common setup.
        com.landis.nonatomic.Nonatomic.init();

        com.landis.nonatomic.Nonatomic.registryInit();
    }
}
