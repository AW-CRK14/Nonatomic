package com.landis.nonatomic.datagroup;

import com.landis.nonatomic.EventHooks;
import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.OperatorInfo;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Deploy extends OperatorInfo<Deploy> {
    public static final Codec<Deploy> CODEC = RecordCodecBuilder.create(a -> a.group(
            ResourceLocation.CODEC.fieldOf("operator_id").forGetter(i -> i.operatorId),
            LevelAndPosRecorder.CODEC.optionalFieldOf("last_pos").forGetter(i -> i.lastPos),
            ResourceLocation.CODEC.fieldOf("status").forGetter(i -> i.status),
            ExtraCodecs.either(Codec.BOOL, UUIDUtil.CODEC).fieldOf("login_event").forGetter(i -> i.playerLoginAction),
            UUIDUtil.CODEC.optionalFieldOf("deploy_uuid").forGetter(i -> i.deployID)
    ).apply(a, Deploy::new));
    public final ResourceLocation operatorId;

    public Optional<LevelAndPosRecorder> lastPos;
    @Nullable
    public OperatorEntity entity;
    public ResourceLocation status;
    public Either<Boolean, UUID> playerLoginAction;//left:true -> 请求登录重新部署 right -> 登录后检索实体
    public Optional<UUID> deployID;//empty -> 未被部署 用于标记

    public Deploy(ResourceLocation operatorId, Optional<LevelAndPosRecorder> pos, ResourceLocation status, Either<Boolean, UUID> playerLoginAction, Optional<UUID> deployID) {
        this.operatorId = operatorId;
        this.lastPos = pos;
        this.status = status;
        this.playerLoginAction = playerLoginAction;
        this.deployID = deployID;
    }

    public Deploy(ResourceLocation operatorId) {
        this.operatorId = operatorId;
        this.lastPos = Optional.empty();
        this.status = STATUS_LEISURE;
        this.playerLoginAction = Either.left(false);
        this.deployID = Optional.empty();
    }


    @Override
    public Codec<Deploy> codec() {
        return CODEC;
    }

    @Override
    public Deploy copy() {
        return new Deploy(operatorId, lastPos, status, playerLoginAction, deployID);
    }

    public Optional<UUID> forceRedeploy() {
        if (deployID.isPresent()) {
            UUID id = UUID.randomUUID();
            deployID = Optional.of(id);
            return deployID;
        }
        return Optional.empty();
    }

    public void pullLastPos(@Nullable LevelAndPosRecorder recorder) {
        if (recorder != null) {
            this.lastPos = Optional.of(recorder);
        }
        //TODO 从缓存库中拉取最后位置信息
    }

    @Override
    public void preLogout() {
        super.preLogout();
        if (entity != null) {
            int value = EventHooks.allowOperatorStayDeploying(operator.player, operatorId, entity);
            if ((value & 1) != 1) {//决定是否存在
                entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
                lastPos = Optional.empty();
                //标记再部署
                playerLoginAction = Either.left((value & 2) == 2);
            } else {
                lastPos = Optional.of(new LevelAndPosRecorder(entity));
                playerLoginAction = Either.right(entity.getUUID());
            }
        }
    }

    @Override
    public void init(Operator owner) {
        super.init(owner);
        //TODO
    }

    public static final ResourceLocation STATUS_LEISURE = new ResourceLocation(Nonatomic.MOD_ID, "leisure");//空闲状态
    public static final ResourceLocation STATUS_REST = new ResourceLocation(Nonatomic.MOD_ID, "rest");//休息状态
    public static final ResourceLocation STATUS_TRACKING = new ResourceLocation(Nonatomic.MOD_ID, "tracking");//跟随状态
    public static final ResourceLocation STATUS_ALERT = new ResourceLocation(Nonatomic.MOD_ID, "alert");//警戒状态
    public static final ResourceLocation STATUS_WORKING = new ResourceLocation(Nonatomic.MOD_ID, "working");//工作状态
    public static final ResourceLocation STATUS_DISPATCHING = new ResourceLocation(Nonatomic.MOD_ID, "dispatching");//外派状态
}
