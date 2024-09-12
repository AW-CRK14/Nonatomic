package com.landis.nonatomic;

import com.landis.nonatomic.core.OpeHandler;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.misc.EmptyEntityRenderer;
import com.landis.nonatomic.misc.VillagerKnightRender;
import com.landis.nonatomic.registry.EntityTypeRegistry;
import com.landis.nonatomic.registry.ItemRegistry;
import com.landis.nonatomic.registry.OperatorInfoRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import com.mojang.serialization.DataResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;
import java.util.function.Predicate;

public final class Nonatomic {
    public static final String MOD_ID = "nonatomic";

    public static void init() {

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> AttachedData.opeHandlerGroupProvider(serverPlayer.getServer()).login(serverPlayer));

        PlayerEvent.PLAYER_QUIT.register(serverPlayer -> AttachedData.opeHandlerGroupProvider(serverPlayer.getServer()).logout(serverPlayer));

        EntityAttributeRegistry.register(EntityTypeRegistry.TEST, Zombie::createAttributes);

        EntityRendererRegistry.register(EntityTypeRegistry.TEST, VillagerKnightRender::new);

        EntityEvent.ADD.register((entity, world) -> {
            if (world instanceof ServerLevel serverLevel && entity instanceof OperatorEntity opeEntity) {

                if (!AttachedData.opeHandlerGroupProvider(serverLevel.getServer()).initOperatorEntity(opeEntity)) {
                    return EventResult.interruptFalse();
                }
                opeEntity.opeInit();
                return EventResult.interruptTrue();
            }
            return EventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> (entity instanceof OperatorEntity && source.getEntity() instanceof Player) || (entity instanceof Player player && source.getEntity() instanceof OperatorEntity) ? EventResult.interruptFalse() : EventResult.pass());

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                OpeHandler handler = AttachedData.opeHandlerGroupProvider(player.getServer()).getDataFor(player);
                handler.deploying().forEach(o -> o.retreat(true));
            }
            return EventResult.pass();
        });

        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> {
            OpeHandler handler = AttachedData.opeHandlerGroupProvider(player.getServer()).getDataFor(player);
            handler.refresh(player);
            handler.deploying().stream()
                    .map(Operator::getEntity)
                    .filter(Objects::nonNull)
                    .forEach(OperatorEntity::transDimension);
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, source) -> AttachedData.opeHandlerGroupProvider(player.getServer()).getDataFor(player).refresh(player));

    }

    public static void registryInit() {
        ItemRegistry.REGISTER.register();
        EntityTypeRegistry.REGISTER.register();
        OperatorTypeRegistry.REGISTER.register();
        OperatorInfoRegistry.REGISTER.register();
    }
}
