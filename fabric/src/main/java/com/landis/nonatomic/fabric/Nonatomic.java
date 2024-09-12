package com.landis.nonatomic.fabric;

import net.fabricmc.api.ModInitializer;

public final class Nonatomic implements ModInitializer {
    @Override
    public void onInitialize() {
        RegistriesImpl.bootstrap();

        com.landis.nonatomic.Nonatomic.registryInit();

        // Run our common setup.
        com.landis.nonatomic.Nonatomic.init();

    }
}
