package com.landis.nonatomic;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

//TODO
public class EventHooks {
    /**
     * 是否允许干员保持为部署状态
     *
     * @return 分位2决定是(1)否标记再部署 分位1决定是(1)否存在
     */
    public static int allowOperatorStayDeploying(ServerPlayer player, Operator operator, OperatorEntity entity) {//TODO
        return (player.getServer().isSingleplayer() || operator.getStatus() != Operator.STATUS_TRACKING) ? 0b01 : 0b10;
    }

    /**
     * 是否允许干员撤退
     *
     * @return 若为空表示不允许，否则为撤退后状态
     */
    public static Optional<ResourceLocation> allowOperatorRetreat(ServerPlayer player, Operator operator, OperatorEntity entity) {//TODO
        return operator.getStatus() == Operator.STATUS_DISPATCHING ? Optional.empty() :
                Optional.of((operator.getStatus() == Operator.STATUS_WORKING || operator.getStatus() == Operator.STATUS_ALERT) ? Operator.STATUS_READY : Operator.STATUS_REST);
    }

    public static void onRetreat(Operator operator, Entity.RemovalReason focusDisconnect) {//TODO

    }

    public static boolean allowDataMerge(Entity.RemovalReason reason, OperatorEntity entity, Operator operator){
        return true;//TODO
    }
}
