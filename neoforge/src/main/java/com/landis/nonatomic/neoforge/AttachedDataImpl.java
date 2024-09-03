package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.neoforge.registry.DataAttachmentRegistry;
import net.minecraft.server.MinecraftServer;

public class AttachedDataImpl {

    public static OpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server) {
        return server.overworld().getData(DataAttachmentRegistry.OPE_HANDLER.get());
    }
}
