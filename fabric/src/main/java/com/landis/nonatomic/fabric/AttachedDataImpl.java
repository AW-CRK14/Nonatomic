package com.landis.nonatomic.fabric;

import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.landis.nonatomic.fabric.registry.ComponentRegistry;
import net.minecraft.world.entity.player.Player;

public class AttachedDataImpl {
    public static PlayerOpeHandlerNoRepetition testInfoProvider(Player player) {
        return ComponentRegistry.OPE_HANDLER.get(player).getData();
    }
}
