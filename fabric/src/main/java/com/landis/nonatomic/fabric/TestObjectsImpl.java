package com.landis.nonatomic.fabric;

import com.landis.nonatomic.TestObjects;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.fabric.registry.ComponentRegistry;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.NonnullDefault;

public class TestObjectsImpl {
    @NonnullDefault
    public static OpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server) {
        return ComponentRegistry.OPE_HANDLER.get(server.overworld()).getData();
    }
}
