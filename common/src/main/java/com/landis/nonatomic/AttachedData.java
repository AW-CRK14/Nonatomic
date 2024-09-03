package com.landis.nonatomic;

import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.system.NonnullDefault;

public class AttachedData {

    @ExpectPlatform
    @NonnullDefault
    public static OpeHandlerNoRepetition.LevelContainer opeHandlerGroupProvider(MinecraftServer server){
        throw new UnsupportedOperationException();
    }
}
