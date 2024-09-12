package com.landis.nonatomic;

import com.landis.nonatomic.core.OpeHandler;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

public final class Nonatomic {
    public static final String MOD_ID = "nonatomic";

    public static void init() {
        //阻止干员与玩家之间的伤害
        EntityEvent.LIVING_HURT.register((entity, source, amount) ->
                (entity instanceof OperatorEntity && source.getEntity() instanceof Player) || (entity instanceof Player && source.getEntity() instanceof OperatorEntity)
                        ? EventResult.interruptFalse() : EventResult.pass()
        );
        TestObjects.initTest();
    }

    public static void registryInit() {
        OperatorTypeRegistry.REGISTER.register();
        TestObjects.initTestRegistry();
    }

    /**
     * 自动注册一组干员系统处理事件。由于无法设置优先级，这在模组兼容上可能产生不可预料的bug。<p>
     */
    public static void registerHandlerEventsArchLike(OpeHandler.GroupProvider handlerProvider) {

        PlayerEvent.PLAYER_JOIN.register(serverPlayer -> handlerProvider.withPlayer(serverPlayer).ifPresent(o -> o.login(serverPlayer)));

        PlayerEvent.PLAYER_QUIT.register(serverPlayer -> handlerProvider.withPlayer(serverPlayer).ifPresent(OpeHandler::logout));

        //用于处理干员的部署
        EntityEvent.ADD.register((entity, world) -> {
            if (world instanceof ServerLevel serverLevel && entity instanceof OperatorEntity opeEntity) {
                if (opeEntity.getIdentifier() != null && opeEntity.getBelongingUUID() != null &&
                        handlerProvider.withUUID(opeEntity.getBelongingUUID(), serverLevel.getServer())//寻找玩家对应的干员容器
                                .flatMap(handler -> handler.findOperator(opeEntity.getIdentifier()))//根据特征找到对应干员
                                .map(ope -> ope.entityCreated(opeEntity))//请求创建实体
                                .orElse(false)) {
                    opeEntity.opeInit();//初始化数据填充完成的干员实体
                    return EventResult.interruptTrue();
                }
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        //用于处理玩家的死亡  干员的死亡在干员实体自己里面
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ServerPlayer player) {
                handlerProvider.withPlayer(player).map(OpeHandler::deploying).ifPresent(list -> list.forEach(o -> o.retreat(true)));
            }
            return EventResult.pass();
        });

        //玩家切换维度
        PlayerEvent.CHANGE_DIMENSION.register((player, oldLevel, newLevel) -> handlerProvider.withPlayer(player).ifPresent(handler -> {
            handler.refresh(player);
            handler.deploying().stream()
                    .map(Operator::getEntity)
                    .filter(Objects::nonNull)
                    .forEach(OperatorEntity::transDimension);
        }));

        //玩家重生
        PlayerEvent.PLAYER_RESPAWN.register((player, source) -> handlerProvider.withPlayer(player).ifPresent(handler -> handler.refresh(player)));
    }
}
