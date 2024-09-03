package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.landis.nonatomic.neoforge.registry.DataAttachmentRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class AttachedDataImpl {

    public static PlayerOpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server) {
        return server.overworld().getData(DataAttachmentRegistry.OPE_HANDLER.get());
    }
}
