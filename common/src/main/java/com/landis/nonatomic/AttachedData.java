package com.landis.nonatomic;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.NonnullDefault;

public class AttachedData {

    @ExpectPlatform
    @NonnullDefault
    public static PlayerOpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server){
        throw new UnsupportedOperationException();
    }
}
