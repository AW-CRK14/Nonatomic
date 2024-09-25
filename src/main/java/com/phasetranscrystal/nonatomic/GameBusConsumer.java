package com.phasetranscrystal.nonatomic;

import com.phasetranscrystal.nonatomic.core.OpeHandler;
import com.phasetranscrystal.nonatomic.core.Operator;
import com.phasetranscrystal.nonatomic.core.OperatorEntity;
import com.phasetranscrystal.nonatomic.event.EntityUninstallByChunkEvent;
import com.phasetranscrystal.nonatomic.event.OperatorEvent;
import com.phasetranscrystal.nonatomic.misc.LevelAndPosRecorder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class GameBusConsumer {
    public static final Logger LOGGER = LogManager.getLogger("BreaNona:EventConsumer");

    @SubscribeEvent
    public static void onEntityUnloadByChunk(final EntityUninstallByChunkEvent event) {
        if (event.entity instanceof OperatorEntity entity) {
            Operator operator = entity.getOperator();
            if (operator.getStatus().equals(Operator.STATUS_TRACKING) && !entity.isRemoved()) {
                if (entity.getOwner() != null) {
                    entity.moveTo(entity.getOwner().getX(), entity.getOwner().getY(), entity.getOwner().getZ());
                    operator.markLastPos(new LevelAndPosRecorder(entity));
                    event.setCanceled(true);
                } else {//正常情况下不会被执行
                    operator.disconnectWithEntity();
                }
            } else {
                operator.markLastPos(new LevelAndPosRecorder(entity));
                operator.setEntityNull();
            }
        }
    }

    @SubscribeEvent()
    public static void preventOperatorPlayerDamage(LivingIncomingDamageEvent event) {
        if ((event.getEntity() instanceof OperatorEntity && event.getSource().getEntity() instanceof Player) ||
                (event.getEntity() instanceof Player && event.getSource().getEntity() instanceof OperatorEntity))
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void checkOperatorEntity(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof OperatorEntity opeEntity) {
            if (opeEntity.getIdentifier() != null && opeEntity.getBelongingUUID() != null && opeEntity.getContainerID() != null) {
                Operator operator = EventHooks.findOperator(serverLevel.getServer(), opeEntity);
                if (operator == null) {
                    LOGGER.warn("Unable to find operator for {}, skipped.", opeEntity.getIdentifier());
                } else if (event.loadedFromDisk()) {
                    if (operator.entityCreated(opeEntity, false)) {
                        opeEntity.opeInit();
                        EventHooks.onLoad(operator, opeEntity, operator.getOpeHandler().owner());
                        return;
                    }
                } else {
                    if (EventHooks.preDeploy(operator, opeEntity, operator.getOpeHandler().owner()) && operator.entityCreated(opeEntity, true)) {
                        opeEntity.opeInit();//初始化数据填充完成的干员实体
                        EventHooks.onDeploy(operator, opeEntity, operator.getOpeHandler().owner());
                        return;
                    } else EventHooks.deployFailed(operator, operator.getOpeHandler().owner(), -4);
                }
            }
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof OperatorEntity operator) {
            operator.getOperator().onOperatorDead();
        }
    }

    public static void registerHandlerEvents(Function<MinecraftServer, OpeHandler.GroupProvider> provider) {
        IEventBus bus = NeoForge.EVENT_BUS;

        Function<ServerPlayer, Optional<? extends OpeHandler>> handlerProvider = player -> provider.apply(player.getServer()).withPlayer(player);

        bus.addListener(OperatorEvent.FindOperator.class, event -> {
            if (event.found()) return;
            //belong pass
            Optional<? extends OpeHandler> handler = provider.apply(event.server).withUUID(event.entity.getBelongingUUID());
            handler.ifPresent(h -> {
                //containerID pass
                if (h.containerId().equals(event.entity.getContainerID())) {
                    //identifier pass
                    h.findOperator(event.entity.getIdentifier()).ifPresent(event::setResult);
                }
            });
        });

        bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            handlerProvider.apply(player).ifPresent(o -> o.login(player));
        });

        bus.addListener(PlayerEvent.PlayerLoggedOutEvent.class,
                event -> handlerProvider.apply((ServerPlayer) event.getEntity()).ifPresent(OpeHandler::logout)
        );

        //用于处理玩家的死亡
        bus.addListener(EventPriority.LOWEST, LivingDeathEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                handlerProvider.apply(player).map(OpeHandler::filteredDeploying).ifPresent(list -> list.forEach(o -> o.retreat(true, Operator.RetreatReason.PLAYER_FAILED)));
            }
        });

        //玩家切换维度
        bus.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> handlerProvider.apply((ServerPlayer) event.getEntity())
                .ifPresent(handler -> {
                    handler.refresh((ServerPlayer) event.getEntity());
                    handler.filteredDeploying().stream()
                            .map(Operator::getEntity)
                            .filter(Objects::nonNull)
                            .forEach(OperatorEntity::teleportToPlayer);
                }));

        //玩家重生
        bus.addListener(PlayerEvent.PlayerRespawnEvent.class, event ->
                handlerProvider.apply((ServerPlayer) event.getEntity()).ifPresent(handler -> handler.refresh((ServerPlayer) event.getEntity()))
        );

    }

}
