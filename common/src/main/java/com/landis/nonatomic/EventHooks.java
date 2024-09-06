package com.landis.nonatomic;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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

    //在干员创建时触发，判断是否允许记录
    public static boolean allowRecordEntity( Operator operator, OperatorEntity entity, ResourceLocation status) {
        return status == Operator.STATUS_DISPATCHING;
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

    public static void onDeploy(ServerPlayer player, Operator operator, OperatorEntity entity){//TODO
    }

    @SafeVarargs
    public static Either<Boolean, List<Codec<? extends OperatorInfo>>> allowDataMerge(boolean followingLogout, OperatorEntity entity, Operator operator, @Nullable Codec<? extends OperatorInfo>... types) {
        return Either.left(true);//TODO
    }
}
