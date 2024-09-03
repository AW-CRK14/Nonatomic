package com.landis.nonatomic.neoforge;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.landis.nonatomic.neoforge.registry.DataAttachmentRegistry;
import net.minecraft.world.entity.player.Player;

public class AttachedDataImpl {

    public static PlayerOpeHandlerNoRepetition testInfoProvider(Player player) {
        return player.getData(DataAttachmentRegistry.OPE_HANDLER.get());
    }
}
