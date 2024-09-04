package com.landis.nonatomic;

import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.registry.OperatorInfoRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import dev.architectury.event.events.common.PlayerEvent;

public final class Nonatomic {
    public static final String MOD_ID = "nonatomic";

    public static void init() {

        OperatorTypeRegistry.REGISTER.register();
        OperatorInfoRegistry.REGISTER.register();

        // Write common init code here.
        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> {
            OperatorType type = Registries.getOperatorTypeRegistry().get(OperatorTypeRegistry.PLACE_HOLDER.getId());
            OpeHandlerNoRepetition.LevelContainer container = AttachedData.opeHandlerGroupProvider(serverPlayer.getServer());
            OpeHandlerNoRepetition value = container.getDataFor(serverPlayer);
            System.out.println("event handled");
        });

    }
}
