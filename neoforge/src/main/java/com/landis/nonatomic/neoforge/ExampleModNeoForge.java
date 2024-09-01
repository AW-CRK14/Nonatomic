package com.landis.nonatomic.neoforge;

import net.neoforged.fml.common.Mod;

import com.landis.nonatomic.Nonatomic;

@Mod(Nonatomic.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        Nonatomic.init();
    }
}
