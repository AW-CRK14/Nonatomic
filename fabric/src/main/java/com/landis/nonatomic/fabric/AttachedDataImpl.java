package com.landis.nonatomic.fabric;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.landis.nonatomic.fabric.registry.ComponentRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.system.NonnullDefault;

public class AttachedDataImpl {

    @NonnullDefault
    public static PlayerOpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server) {
        return ComponentRegistry.OPE_HANDLER.get(server.overworld()).getData();
    }
}
