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
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * <p>干员</p>
 * <p>在本库中，干员是玩家拥有的完整持久化单位的最小单位，在原则上不需要继承并创建子类。
 * 干员负责存储一系列数据，包括标定本干员的特征信息等。</p>
 * <p>在干员部署时，默认跟随状态下会存储干员实体，其它状态下只会标定干员的最后位置与实体uuid。
 * 所有数据原则上不存储在所属玩家身上——而是应该存储在维度中，以便于实体创建时的同步和防止玩家数据量过大。</p>
 * <p>在生物实体被创建时，会尝试与其对应的干员实例建立联系，验证合法性，引用数据与同步最后位置。</p>
 */
public class Operator {
    //芝士Codec
    public static final Codec<Operator> CODEC = RecordCodecBuilder.create(n -> n.group(
            Identifier.CODEC.fieldOf("identifier").forGetter(i -> i.identifier),
            OperatorInfo.CODEC.listOf().fieldOf("infos").forGetter(i -> i.infos.values().stream().toList()),
            EntityFinderInfo.CODEC.optionalFieldOf("finder").forGetter(i -> i.entityFinderInfo),
            Codec.BOOL.fieldOf("redeploy").forGetter(i -> i.redeployFlag)
    ).apply(n, Operator::new));

    //预配的状态 默认只有STATUS_WORKING会保存实体
    public static final ResourceLocation STATUS_READY = new ResourceLocation(Nonatomic.MOD_ID, "ready");//空闲状态
    public static final ResourceLocation STATUS_REST = new ResourceLocation(Nonatomic.MOD_ID, "rest");//休息状态
    public static final ResourceLocation STATUS_TRACKING = new ResourceLocation(Nonatomic.MOD_ID, "tracking");//跟随状态
    public static final ResourceLocation STATUS_ALERT = new ResourceLocation(Nonatomic.MOD_ID, "alert");//警戒状态
    public static final ResourceLocation STATUS_WORKING = new ResourceLocation(Nonatomic.MOD_ID, "working");//工作状态
    public static final ResourceLocation STATUS_DISPATCHING = new ResourceLocation(Nonatomic.MOD_ID, "dispatching");//外派状态

    // ---[从这里开始]---

    /**
     * - {@link Operator#identifier}是干员实例特征的唯一标识符，必须包含干员种类以及一个选含的UUID，用于便于{@link OpeHandler 干员管理器}管理与记录。<p>
     * - {@link Operator#infos}存储该干员附加的所有信息——全部以类似组件的形式存在。在干员实体中，只要通过了合法性验证，就可以直接访问干员从而访问数据。<p>
     * - {@link Operator#opeHandler}就是我们上面提到过的干员管理器。干员所属的玩家与总部署状态表都可以通过该实例获取。在init方法中被加载。<p>
     * - {@link Operator#entity}在需要记录实体的时候，这个变量将被对应的实体填充。在默认情况下，只有跟随状态干员会记录。
     * 请不要随意记录其它状态的干员——除非您明白您在干什么——这可能导致干员在被世界移除后一直被持续引用。<p>
     * - {@link Operator#entityFinderInfo}记录实体的uuid与最后位置，用于帮助玩家找到实体以及验证实体合法性。<p>
     */
    public final Identifier identifier;
    public final HashMap<Codec<? extends OperatorInfo>, OperatorInfo> infos = new HashMap<>();

    private OpeHandler opeHandler;

    @Nullable
    private OperatorEntity entity;
    private @SuppressWarnings("all") Optional<EntityFinderInfo> entityFinderInfo;
    private boolean redeployFlag = false;

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
    public Operator(Identifier identifier, List<? extends OperatorInfo> infos, Optional<EntityFinderInfo> entityFinderInfo, boolean redeployFlag) {
        this.identifier = identifier;
        this.entityFinderInfo = entityFinderInfo;
        this.redeployFlag = redeployFlag;
        for (OperatorInfo info : infos) {
            this.infos.put(info.codec(), info);
        }
    }


    public void init(OpeHandler handler) {
        this.opeHandler = handler;
        infos.values().forEach(info -> info.init(this));
    }

    public void login(ServerPlayer player) {
        if (redeployFlag) deploy(false, false);

        infos.values().forEach(OperatorInfo::login);
    }

    public void logout() {
        if (status == STATUS_TRACKING) {
            if (entity == null) disconnectWithEntity();
            else {
                if (EventHooks.allowOperatorRedeployWhenLogin(opeHandler.owner().left().get(), this, entity))
                    this.redeployFlag = true;
                mergeDataFromEntity(entity, true);
                disconnectWithEntity(Entity.RemovalReason.UNLOADED_WITH_PLAYER, STATUS_TRACKING);
            }
        }
        infos.values().forEach(OperatorInfo::logout);
    }


    // ---[实体处理部分]---

    public OperatorEntity tryFindEntity() {
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
                .map(entity -> entity instanceof OperatorEntity operator && checkEntityLegality(operator, false) ? operator : null);
    }

    //检查实体合法性
    @SuppressWarnings("all")
    public boolean checkEntityLegality(OperatorEntity entity, boolean newCreated) {
        return entity != null && entity.getBelongingUUID() == opeHandler.owner().map(ServerPlayer::getUUID, p -> p) &&
                entity.getIdentifier() == this.identifier &&
                (newCreated || (this.entityFinderInfo.isPresent() && this.entityFinderInfo.get().match(entity)));
    }

    //生物实体的数据同步 对于干员实体，也使用这个给自己同步即可
    //记得创建生物实体前先给operator设status
    //原则上不缓存attribute变更
    public void entityCreated(OperatorEntity entity, boolean isNew) {
        if (checkEntityLegality(entity, isNew)) {
            entity.setOperator(this);
            if (EventHooks.allowRecordEntity(this, entity, status)) this.entity = entity;
            if (isNew) {
                this.entityFinderInfo = Optional.of(new EntityFinderInfo(entity));
                infos.values().forEach(info -> {
                    info.entityCreated(entity);
                    if (info instanceof IAttributesProvider p) p.getAttributes().forEach(re -> re.attach(entity));
                });
            } else {
                infos.values().forEach(info -> info.entityInit(entity));
            }
        }
    }

    public void requestMerge(OperatorEntity entity, @Nullable Codec<? extends OperatorInfo>... types) {
        if (checkEntityLegality(entity, false)) {
            mergeDataFromEntity(entity, false, types);
        }
    }

    //合并实体信息
    @SafeVarargs
    private void mergeDataFromEntity(@Nullable OperatorEntity entity, boolean followingLogout, @Nullable Codec<? extends OperatorInfo>... types) {
        if (entity == null) entity = this.entity;
        if (entity == null) return;
        Function<Codec<? extends OperatorInfo>, Boolean> permission = EventHooks.allowDataMerge(followingLogout, entity, this, types).map(b -> codec -> b, c -> codec -> codec == c);
        entity.requestExternalData().stream().filter(info -> permission.apply(info.codec())).forEach(i -> {
            if (infos.containsKey(i.codec())) infos.get(i.codec()).merge(i);
            else infos.put(i.codec(), i.copy());
        });
    }


    public void disconnectWithEntity() {
        disconnectWithEntity(Entity.RemovalReason.DISCARDED, STATUS_REST);
    }

    //清除实体信息或格式化状态信息
    public void disconnectWithEntity(Entity.RemovalReason reason, ResourceLocation status) {
        if (entity != null && reason != null) {
            mergeDataFromEntity(entity, false);
            if (reason != Entity.RemovalReason.KILLED) entity.remove(reason);
            identifier.type.onRetreat(opeHandler.owner(), this);
            EventHooks.onRetreat(this, reason);
            entity = null;
        }
        entityFinderInfo = Optional.empty();
        redeployFlag = false;
        this.status = status;
    }

    public void checkSelf() {
        if (status == STATUS_REST || status == STATUS_READY) {
            disconnectWithEntity(Entity.RemovalReason.DISCARDED, status);
        } else if (status == STATUS_TRACKING) {
            if (entityFinderInfo.isEmpty() || (opeHandler.owner().left().isPresent() && !checkEntityLegality(entity, false)))
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

        if (checkEntityLegality(otherEntity, false)) this.entity = otherEntity;

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

    public void modifyAttribute(boolean remove, IAttributesProvider.MarkedModifier modifier) {
        if (entity != null) {
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

        public EntityFinderInfo(LivingEntity entity) {
            this.entityUUID = entity.getUUID();
            this.posRecorder = new LevelAndPosRecorder(entity);
        }

        public boolean match(OperatorEntity entity) {
            return entity.getUUID() == this.entityUUID;
        }
    }
}
