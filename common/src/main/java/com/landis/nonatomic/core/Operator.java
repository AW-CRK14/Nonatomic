package com.landis.nonatomic.core;

import com.landis.nonatomic.EventHooks;
import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.info.IAttributesProvider;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Operator {

    public static final Codec<Operator> CODEC = RecordCodecBuilder.create(n -> n.group(
            Identifier.CODEC.fieldOf("identifier").forGetter(i -> i.identifier),
            OperatorInfo.CODEC.listOf().fieldOf("infos").forGetter(i -> i.infos.values().stream().toList()),
            EntityFinderInfo.CODEC.optionalFieldOf("finder").forGetter(i -> i.entityFinderInfo),
            Codec.INT.fieldOf("login_action").forGetter(i -> i.loginActionFlag)
    ).apply(n, Operator::new));

    public static final ResourceLocation STATUS_READY = new ResourceLocation(Nonatomic.MOD_ID, "ready");//空闲状态
    public static final ResourceLocation STATUS_REST = new ResourceLocation(Nonatomic.MOD_ID, "rest");//休息状态
    public static final ResourceLocation STATUS_TRACKING = new ResourceLocation(Nonatomic.MOD_ID, "tracking");//跟随状态
    public static final ResourceLocation STATUS_ALERT = new ResourceLocation(Nonatomic.MOD_ID, "alert");//警戒状态
    public static final ResourceLocation STATUS_WORKING = new ResourceLocation(Nonatomic.MOD_ID, "working");//工作状态
    public static final ResourceLocation STATUS_DISPATCHING = new ResourceLocation(Nonatomic.MOD_ID, "dispatching");//外派状态

    // ---[从这里开始]---

    public final Identifier identifier;
    public final HashMap<Codec<? extends OperatorInfo>, OperatorInfo> infos = new HashMap<>();

    private OpeHandler opeHandler;

    @Nullable
    private OperatorEntity entity;
    private @SuppressWarnings("all") Optional<EntityFinderInfo> entityFinderInfo;
    private int loginActionFlag = 0;

    private ResourceLocation status;

    public ResourceLocation getStatus() {
        return status;
    }

    public void skipResting() {
        if (status == STATUS_REST) status = STATUS_READY;
    }

    public @Nullable OperatorEntity getEntity() {
        return entity;
    }

    public Optional<EntityFinderInfo> getEntityFinderInfo() {
        return entityFinderInfo;
    }

    public OpeHandler getOpeHandler() {
        return opeHandler;
    }

    public OperatorType getType() {
        return identifier.type();
    }

    public void refreshLastPos(LevelAndPosRecorder recorder) {
        entityFinderInfo.ifPresent(info -> info.posRecorder = recorder);
    }

    // ---[构造函数]---

    public Operator(OperatorType operatorType) {
        this.identifier = new Identifier(operatorType);
    }

    public Operator(Identifier identifier) {
        this.identifier = identifier;
    }

    @SuppressWarnings("all")
    public Operator(Identifier identifier, List<? extends OperatorInfo> infos, Optional<EntityFinderInfo> entityFinderInfo, int loginActionFlag) {
        this.identifier = identifier;
        this.entityFinderInfo = entityFinderInfo;
        this.loginActionFlag = loginActionFlag;
        for (OperatorInfo info : infos) {
            this.infos.put(info.codec(), info);
        }
    }


    public void init(OpeHandler handler) {
        this.opeHandler = handler;
        infos.values().forEach(info -> info.init(this));
    }

    public void login(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            switch (loginActionFlag) {
                case 0 -> disconnectWithEntity();
                case 1 -> deploy(false, false);
//                case 2 ->
//                        findEntity(serverPlayer.getServer()).ifPresentOrElse(this::initTrackingEntity, this::disconnectWithEntity);
            }
        }

        infos.values().forEach(OperatorInfo::login);
    }

    public void logout() {
        if (status == STATUS_TRACKING) {
            if (entity == null)
                disconnectWithEntity();
            int value = EventHooks.allowOperatorStayDeploying(opeHandler.owner().left().get(), this, entity);
            switch (value & 0b11) {
                case 0b11, 0b01 -> {//保持存在
                    if (entityFinderInfo.isPresent()) {
                        entityFinderInfo.get().posRecorder = new LevelAndPosRecorder(entity);
                        loginActionFlag = 2;
                    } else {
                        if (checkEntityLegality(entity)) {
                            this.entityFinderInfo = Optional.of(new EntityFinderInfo(entity.getUUID(), new LevelAndPosRecorder(entity)));
                            loginActionFlag = 2;
                        } else disconnectWithEntity();
                    }
                }
                case 0b00 -> //不存在也不进行再部署
                        retreat(true, null, true);
                case 0b10 -> {//不存在但进行再部署
                    retreat(true, null, false);
                    loginActionFlag = 1;
                }
            }
        }
        infos.values().forEach(OperatorInfo::logout);
    }


    // ---[实体处理部分]---


    public OperatorEntity getEntityTrackingOnly() {
        if (entity != null || opeHandler.owner().left().isEmpty()) return entity;
        Optional<OperatorEntity> e = findEntity(opeHandler.owner().left().get().getServer());
        if (status == STATUS_TRACKING && e.isPresent()) {
            this.entity = e.get();
        }
        return e.orElse(null);
    }

    //尝试寻找实体
    public Optional<OperatorEntity> findEntity(MinecraftServer server) {
        return entityFinderInfo.map(info -> server.getLevel(info.posRecorder.level()).getEntity(info.entityUUID))
                .map(entity -> entity instanceof OperatorEntity operator && checkEntityLegality(operator) ? operator : null);
    }

    //检查实体合法性
    @SuppressWarnings("all") //TODO
    public boolean checkEntityLegality(OperatorEntity entity) {
        if (entity == null || entityFinderInfo.isEmpty() ||
                entity.getUUID() != entityFinderInfo.get().entityUUID
        ) return false;

        return true;
    }

    //生物实体的数据同步 对于干员实体，也使用这个给自己同步即可
    //原则上不缓存attribute变更
    public void entityCreated(OperatorEntity entity, boolean isNew){
        if(checkEntityLegality(entity)){
            //TODO 干员引用数据
            if(status == STATUS_TRACKING) this.entity = entity;
            if(isNew){
                this.entityFinderInfo = Optional.of(new EntityFinderInfo(entity.getUUID(), new LevelAndPosRecorder(entity)));
                infos.values().forEach(info -> {
                    if(info instanceof IAttributesProvider p) p.getAttributes().forEach(re -> re.attach(entity));
                });
            }
        }
    }

    public void requestMerge(OperatorEntity entity) {
        if (checkEntityLegality(entity)) {
            mergeDataFromEntity(entity);
        }
    }

    //合并实体信息
    private void mergeDataFromEntity(@Nullable OperatorEntity entity) {
        if (entity == null) entity = this.entity;
        //TODO 进行实体数据合并
    }


    public void disconnectWithEntity() {
        disconnectWithEntity(Entity.RemovalReason.DISCARDED, STATUS_REST);
    }

    //清除实体信息或格式化状态信息
    public void disconnectWithEntity(Entity.RemovalReason reason, ResourceLocation status) {
        if (entity != null && reason != null) {
            if (EventHooks.allowDataMerge(reason, entity, this, false)) mergeDataFromEntity(entity);
            //TODO 清除实体数据绑定
            if (reason != Entity.RemovalReason.KILLED) entity.remove(reason);
            identifier.type.onRetreat(opeHandler.owner(), this);
            EventHooks.onRetreat(this, reason);
            entity = null;
        }
        entityFinderInfo = Optional.empty();
        loginActionFlag = -1;
        this.status = status;
    }

    public void checkSelf() {
        if (status == STATUS_REST || status == STATUS_READY) {
            disconnectWithEntity(Entity.RemovalReason.DISCARDED, status);
        } else if (status == STATUS_TRACKING) {
            if (entityFinderInfo.isEmpty() || (opeHandler.owner().left().isPresent() && !checkEntityLegality(entity)))
                disconnectWithEntity();
        } else if (status == STATUS_WORKING || status == STATUS_ALERT) {
            if (entityFinderInfo.isEmpty()) disconnectWithEntity();
            entity = null;
        } else if (status == STATUS_DISPATCHING) {
            disconnectWithEntity(Entity.RemovalReason.DISCARDED, STATUS_DISPATCHING);
        }
    }

    // ---[部署与撤退]---

    @SuppressWarnings("all")
    public boolean deploy(boolean focus, boolean markWhenNoPlayer) {//TODO
        if (((entity != null || status != STATUS_READY) && !focus) || opeHandler.owner().left().isEmpty()) return false;

        if (identifier.type.allowDeploy(opeHandler.owner().left().get(), this) &&
                opeHandler.addDeploying(this, true)) {

            boolean flag = true;

            //find deploy pos
            //post event

            flag = flag && retreat(true, null, false);

            if (!flag) return false;

            //执行部署行为
            //deploy in opeHandler
            //post event

//        lastPos = //标记新的部署位置
            status = STATUS_TRACKING;
//        entity =
//        deployID =
            return true;
        }
        return false;
    }

    /**
     * 使该干员撤退 不包含自然撤退情况 不要在客户端调用这个
     *
     * @param focus       是否强制撤退
     * @param otherEntity 选配，目标实体。一般给工作或巡逻状态干员用的。
     * @param safeMode    若开启，则对于不需要执行操作的分支也会进行操作以保证数据存储的正确性
     * @return true -> 成功撤退  false -> 不允许撤退
     */
    @SuppressWarnings("deprecation")
    public boolean retreat(boolean focus, OperatorEntity otherEntity, boolean safeMode) {
        if (!focus && opeHandler.owner().left().isEmpty()) return false;

        if (checkEntityLegality(otherEntity)) this.entity = otherEntity;

        //为什么这会需要撤退呢
        if (status == STATUS_READY || status == STATUS_REST) {
            if (safeMode) disconnectWithEntity();
            return false;
        }


        Optional<ResourceLocation> state = identifier.type.allowRetreat(entity, opeHandler.owner(), this) ? EventHooks.allowOperatorRetreat(opeHandler.owner().left().get(), this, otherEntity) : Optional.empty();
        if (state.isPresent()) {
            disconnectWithEntity(Entity.RemovalReason.UNLOADED_WITH_PLAYER, state.get());
            opeHandler.onRetreat(this);
            return true;
        }

        if (focus) {
            disconnectWithEntity(Entity.RemovalReason.DISCARDED, STATUS_REST);
            opeHandler.onRetreat(this);
            return true;
        }
        return false;
    }

    //当干员死亡 不处理死亡行为本身
    @SuppressWarnings("deprecation")
    public void onOperatorDead(OperatorEntity entity) {
        disconnectWithEntity(Entity.RemovalReason.KILLED, STATUS_REST);
        opeHandler.onRetreat(this);
    }

    // ---[属性附加处理器]---

    public void modifyAttribute(boolean remove, IAttributesProvider.MarkedModifier modifier) {//TODO
        if(entity != null){
            if (remove) modifier.remove(entity);
            else modifier.attach(entity);
        }
    }


    public record Identifier(OperatorType type, Optional<UUID> uuid) {
        public static final Codec<Identifier> CODEC = RecordCodecBuilder.create(n -> n.group(
                Registries.getOperatorTypeRegistry().byNameCodec().fieldOf("type").forGetter(Identifier::type),
                UUIDUtil.CODEC.optionalFieldOf("uuid").forGetter(Identifier::uuid)
        ).apply(n, Identifier::new));

        public Identifier(OperatorType type) {
            this(type, Optional.empty());
        }

        public Identifier(OperatorType type, UUID uuid) {
            this(type, Optional.of(uuid));
        }
    }

    public static class EntityFinderInfo {
        public static final Codec<EntityFinderInfo> CODEC = RecordCodecBuilder.create(n -> n.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(i -> i.entityUUID),
                LevelAndPosRecorder.CODEC.fieldOf("pos").forGetter(i -> i.posRecorder)
        ).apply(n, EntityFinderInfo::new));
        public final UUID entityUUID;
        public @NotNull LevelAndPosRecorder posRecorder;

        public EntityFinderInfo(UUID entityUUID, @NotNull LevelAndPosRecorder posRecorder) {
            this.entityUUID = entityUUID;
            this.posRecorder = posRecorder;
        }
    }
}
