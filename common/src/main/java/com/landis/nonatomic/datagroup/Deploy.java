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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class Deploy extends OperatorInfo {
    public static final Either<Boolean, UUID> LOGIN_ACTION_NONE = Either.left(false);
    public static final Either<Boolean, UUID> LOGIN_ACTION_REDEPLOY = Either.left(true);

    public static final Codec<Deploy> CODEC = RecordCodecBuilder.create(a -> a.group(
            LevelAndPosRecorder.CODEC.optionalFieldOf("last_pos").forGetter(i -> i.lastPos),
            ResourceLocation.CODEC.fieldOf("status").forGetter(i -> i.status),
            ExtraCodecs.either(Codec.BOOL, UUIDUtil.CODEC).fieldOf("login_event").orElse(LOGIN_ACTION_NONE).forGetter(i -> i.playerLoginAction),
            UUIDUtil.CODEC.optionalFieldOf("deploy_uuid").forGetter(i -> i.deployID)
    ).apply(a, Deploy::new));

    public Optional<LevelAndPosRecorder> lastPos;
    @Nullable//仅在跟随状态记录实体存在性
    public OperatorEntity entity;
    public ResourceLocation status;
    public Either<Boolean, UUID> playerLoginAction;//left:true -> 请求登录重新部署 right -> 登录后检索实体
    public Optional<UUID> deployID;//empty -> 未被部署 用于标记


    public Deploy(Optional<LevelAndPosRecorder> pos, ResourceLocation status, Either<Boolean, UUID> playerLoginAction, Optional<UUID> deployID) {

        this.lastPos = pos;
        this.status = status;
        this.playerLoginAction = playerLoginAction;
        this.deployID = deployID;
    }

    public Deploy() {
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
        return new Deploy(lastPos, status, playerLoginAction, deployID);
    }


    // ---[DeployID部分]---

    public Optional<UUID> refreshDeployID(boolean executeOnlyWhenPresent, boolean disconnectWithEntity) {
        if (deployID.isPresent() || executeOnlyWhenPresent) {
            if (disconnectWithEntity && entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
                this.entity = null;
            }
            deployID = Optional.of(UUID.randomUUID());
            deployIDChanged();
            return deployID;
        }
        return Optional.empty();
    }

    public void cleanDeployID() {
        if (deployID.isPresent()) {
            deployID = Optional.empty();
            deployIDChanged();
        }
    }

    public void deployIDChanged() {
        //TODO
    }

    //

    public void pullLastPos(@Nullable LevelAndPosRecorder recorder) {
        if (recorder != null) {
            this.lastPos = Optional.of(recorder);
        }
        //TODO 从缓存库中拉取最后位置信息 仅工作状态干员
    }

    @Override
    public void preLogout() {
        super.preLogout();
        if (entity != null && operator.player instanceof ServerPlayer s) {
            int value = EventHooks.allowOperatorStayDeploying(s, operator.identifier.type(), operator, entity);
            switch (value & 0b11) {
                case 0b11, 0b01 -> {//保持存在
                    lastPos = Optional.of(new LevelAndPosRecorder(entity));
                    playerLoginAction = Either.right(entity.getUUID());
                }
                case 0b00 -> //不存在也不进行再部署
                    retreat(true, false, null, true);
                case 0b10 -> {//不存在但进行再部署
                    retreat(true, false, null, false);
                    playerLoginAction = LOGIN_ACTION_REDEPLOY;
                }
            }
        }
    }

    @Override
    public void init(Operator owner) {
        super.init(owner);
        //若无任何
        if (!playerLoginAction.left().orElse(true)) {
            cleanEntityInfo();
        }
        //TODO 寻找实体或重新部署 仅跟随状态干员
        tryFindTargetEntity();
    }

    //ture -> entity found
    public boolean tryFindTargetEntity() {
        return false;//TODO
    }

    //ture -> deploy finish
    public boolean deploy(boolean focus, boolean cleanOldIfFailed) {//TODO
        if ((entity != null || status == STATUS_REST) && !focus) return false;

        boolean flag = true;
        if (status != STATUS_LEISURE)
            flag = retreat(focus, false, null, false);


        if (flag) {
            //执行部署行为
            //...
        }


        if (entity != null && (focus || flag || cleanOldIfFailed)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            cleanEntityInfo();
        }

        if (!flag) return false;

//        lastPos = //标记新的部署位置
        status = STATUS_TRACKING;
//        entity =
//        deployID =
        return true;
    }

    /**
     * 使该干员撤退
     *
     * @param focus    是否强制撤退
     * @param dead     是否为死亡原因 死亡本身移除实体不由此处理
     * @param entity   选配，目标实体。一般给工作状态干员用的
     * @param safeMode 若开启，则对于不需要执行操作的分支也会进行操作以保证数据存储的正确性
     * @return true -> 成功撤退  false -> 不允许撤退
     */
    public boolean retreat(boolean focus, boolean dead, OperatorEntity entity, boolean safeMode) {
        if (!(operator.player instanceof ServerPlayer)) return false;

        if (entity == null) entity = this.entity;

        if (status == STATUS_LEISURE || status == STATUS_REST || (!focus && entity == null)) {
            if (safeMode) {
                cleanEntityInfo();
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            return false;
        }

        //如果死亡则允许强制撤退
        if (dead) {
            cleanEntityInfo();
            status = STATUS_REST;
            return true;
        }

        //对于跟随状态干员，根据玩家状态决定是否允许撤退
        if (status == STATUS_TRACKING && (focus || EventHooks.allowOperatorRetreat((ServerPlayer) operator.player, operator.identifier.type(), operator, entity))) {
            if (entity != null) {
                entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            }
            cleanEntityInfo();
            status = STATUS_REST;
            return true;
        }

        //对于工作与警戒状态干员，允许撤退
        if (status == STATUS_WORKING || status == STATUS_ALERT) {
            if (entity != null) {
                entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
            }
            cleanEntityInfo();
            status = STATUS_LEISURE;
            return true;
        }

        //对于外派状态干员，若强制则允许撤退
        if (status == STATUS_DISPATCHING && focus) {

            //TODO 外派状态设置
            if (safeMode) {
                if (entity != null) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
                cleanEntityInfo();
            }
            status = STATUS_LEISURE;
            return true;
        }
        return false;
    }

    //——如果你明白你在干什么的话
    public void cleanEntityInfo() {
        this.entity = null;
        cleanDeployID();
        lastPos = Optional.empty();
        playerLoginAction = LOGIN_ACTION_NONE;
    }


    public static final ResourceLocation STATUS_LEISURE = new ResourceLocation(Nonatomic.MOD_ID, "leisure");//空闲状态
    public static final ResourceLocation STATUS_REST = new ResourceLocation(Nonatomic.MOD_ID, "rest");//休息状态
    public static final ResourceLocation STATUS_TRACKING = new ResourceLocation(Nonatomic.MOD_ID, "tracking");//跟随状态
    public static final ResourceLocation STATUS_ALERT = new ResourceLocation(Nonatomic.MOD_ID, "alert");//警戒状态
    public static final ResourceLocation STATUS_WORKING = new ResourceLocation(Nonatomic.MOD_ID, "working");//工作状态
    public static final ResourceLocation STATUS_DISPATCHING = new ResourceLocation(Nonatomic.MOD_ID, "dispatching");//外派状态


}
