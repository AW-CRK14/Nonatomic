package com.phasetranscrystal.nonatomic;

import com.phasetranscrystal.nonatomic.core.OpeHandler;
import com.phasetranscrystal.nonatomic.core.Operator;
import com.phasetranscrystal.nonatomic.core.OperatorEntity;
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

import java.util.Objects;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class GameBusConsumer {

    @SubscribeEvent()
    public static void preventOperatorPlayerDamage(LivingIncomingDamageEvent event) {
        if ((event.getEntity() instanceof OperatorEntity && event.getSource().getEntity() instanceof Player) ||
                (event.getEntity() instanceof Player && event.getSource().getEntity() instanceof OperatorEntity))
            event.setCanceled(true);
    }


    public static void registerHandlerEvents(OpeHandler.GroupProvider handlerProvider) {
        IEventBus bus = NeoForge.EVENT_BUS;

        bus.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            handlerProvider.withPlayer(player).ifPresent(o -> o.login(player));
        });

        bus.addListener(PlayerEvent.PlayerLoggedOutEvent.class,
                event -> handlerProvider.withPlayer((ServerPlayer) event.getEntity()).ifPresent(OpeHandler::logout)
        );

        //用于处理干员的部署
        bus.addListener(EventPriority.LOW, EntityJoinLevelEvent.class, event -> {
            if (event.getLevel() instanceof ServerLevel serverLevel && event.getEntity() instanceof OperatorEntity opeEntity) {
                if (opeEntity.getIdentifier() != null && opeEntity.getBelongingUUID() != null &&
                        handlerProvider.withUUID(opeEntity.getBelongingUUID(), serverLevel.getServer())//寻找玩家对应的干员容器
                                .flatMap(handler -> handler.findOperator(opeEntity.getIdentifier()))//根据特征找到对应干员
                                .map(ope -> ope.entityCreated(opeEntity))//请求记录创建的实体
                                .orElse(false)) {
                    opeEntity.opeInit();//初始化数据填充完成的干员实体
                } else event.setCanceled(true);
            }
        });

        //用于处理玩家的死亡
        bus.addListener(EventPriority.LOWEST, LivingDeathEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                handlerProvider.withPlayer(player).map(OpeHandler::filteredDeploying).ifPresent(list -> list.stream().filter(Objects::nonNull).forEach(o -> o.retreat(true)));
            } else if (event.getEntity() instanceof OperatorEntity operator) {
                operator.getOperator().onOperatorDead();
            }
        });

        //玩家切换维度
        bus.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> handlerProvider.withPlayer((ServerPlayer) event.getEntity()).ifPresent(handler -> {
            handler.refresh((ServerPlayer) event.getEntity());
            handler.filteredDeploying().stream()
                    .map(Operator::getEntity)
                    .filter(Objects::nonNull)
                    .forEach(OperatorEntity::transDimension);
        }));

        //玩家重生
        bus.addListener(PlayerEvent.PlayerRespawnEvent.class, event ->
                handlerProvider.withPlayer((ServerPlayer) event.getEntity()).ifPresent(handler -> handler.refresh((ServerPlayer) event.getEntity()))
        );

    }

}
