package com.landis.nonatomic;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

//TODO
public class EventHooks {
    /**
     * 是否允许干员在玩家登录时重新部署
     */
    public static boolean allowOperatorRedeployWhenLogin(ServerPlayer player, Operator operator, OperatorEntity entity) {//TODO
        return true;
    }

    public static boolean onOperatorEntityUninstall(OperatorEntity entity) {
        Operator operator = entity.getOperator();
        if (operator.getStatus().equals(Operator.STATUS_TRACKING) && !entity.isRemoved()) {
            if (entity.getOwner() != null) {
                entity.moveTo(entity.getOwner().getX(), entity.getOwner().getY(), entity.getOwner().getZ());
                operator.markLastPos(new LevelAndPosRecorder(entity));
                return true;
            } else {//正常情况下不会被执行
                operator.disconnectWithEntity();
                return false;
            }
        } else {
            operator.markLastPos(new LevelAndPosRecorder(entity));
            operator.setEntityNull();
            return false;
        }
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

    public static boolean allowOperatorDeploy(ServerPlayer player, Operator operator) {//TODO
        return true;
    }

    public static void onDeploy(ServerPlayer player, Operator operator, OperatorEntity entity, boolean isRedeploy) {//TODO
    }

    @SafeVarargs
    public static Either<Boolean, List<Codec<? extends OperatorInfo>>> allowDataMerge(boolean followingLogout, OperatorEntity entity, Operator operator, @Nullable Codec<? extends OperatorInfo>... types) {
        return Either.left(true);//TODO
    }

    public static void redeployFailed(ServerPlayer player, Operator operator, int flag) {
    }

    public static boolean ifTakeDeployPlace(Operator operator,ResourceLocation status){
        return status.equals(Operator.STATUS_TRACKING);
    }
}
