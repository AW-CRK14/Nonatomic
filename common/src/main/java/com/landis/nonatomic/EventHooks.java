package com.landis.nonatomic;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

//TODO
public class EventHooks {
    /**
     * 是否允许干员在玩家登录时重新部署
     */
    public static boolean allowOperatorRedeployWhenLogin(ServerPlayer player, Operator operator, OperatorEntity entity) {//TODO
        return true;
    }

    /**
     * 是否允许干员撤退
     *
     * @return 若为空表示不允许，否则为撤退后状态
     */
    public static Optional<ResourceLocation> allowOperatorRetreat(Player player, Operator operator, OperatorEntity entity) {//TODO
        return operator.getStatus() == Operator.STATUS_DISPATCHING ? Optional.empty() :
                Optional.of((operator.getStatus() == Operator.STATUS_WORKING || operator.getStatus() == Operator.STATUS_ALERT) ? Operator.STATUS_READY : Operator.STATUS_REST);
    }

    public static void onRetreat(Operator operator, Entity.RemovalReason focusDisconnect) {//TODO

    }

    public static boolean allowDataMerge(boolean followingLogout, OperatorEntity entity, Operator operator) {
        return true;//TODO
    }
}
