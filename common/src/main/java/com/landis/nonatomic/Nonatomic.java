package com.landis.nonatomic;

import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.misc.EmptyEntityRenderer;
import com.landis.nonatomic.registry.EntityTypeRegistry;
import com.landis.nonatomic.registry.ItemRegistry;
import com.landis.nonatomic.registry.OperatorInfoRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.minecraft.world.entity.monster.Zombie;

public final class Nonatomic {
    public static final String MOD_ID = "nonatomic";

    public static void init() {

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> AttachedData.opeHandlerGroupProvider(serverPlayer.getServer()).login(serverPlayer));

        PlayerEvent.PLAYER_QUIT.register(serverPlayer -> AttachedData.opeHandlerGroupProvider(serverPlayer.getServer()).logout(serverPlayer));

        EntityAttributeRegistry.register(EntityTypeRegistry.TEST, Zombie::createAttributes);

        EntityRendererRegistry.register(EntityTypeRegistry.TEST, EmptyEntityRenderer::new);

    }

    public static void registryInit() {
        ItemRegistry.REGISTER.register();
        EntityTypeRegistry.REGISTER.register();
        OperatorTypeRegistry.REGISTER.register();
        OperatorInfoRegistry.REGISTER.register();
    }
}
