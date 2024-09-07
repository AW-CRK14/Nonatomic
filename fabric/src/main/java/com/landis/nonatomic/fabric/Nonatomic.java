package com.landis.nonatomic.fabric;

import com.landis.nonatomic.registry.EntityTypeRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.world.entity.monster.Zombie;

public final class Nonatomic implements ModInitializer {
    @Override
    public void onInitialize() {
        RegistriesImpl.bootstrap();

        // Run our common setup.
        com.landis.nonatomic.Nonatomic.init();

        com.landis.nonatomic.Nonatomic.registryInit();
    }
}
