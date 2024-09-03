package com.landis.nonatomic;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.VisibleForTesting;
import org.lwjgl.system.NonnullDefault;

public class AttachedData {

    @ExpectPlatform
    @VisibleForTesting
    @NonnullDefault
    public static PlayerOpeHandlerNoRepetition testInfoProvider(Player player){
        throw new UnsupportedOperationException();
    }
}
