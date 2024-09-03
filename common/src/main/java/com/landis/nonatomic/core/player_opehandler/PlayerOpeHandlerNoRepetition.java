package com.landis.nonatomic.core.player_opehandler;

import com.landis.nonatomic.Helper;
import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.OpeHandler;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.*;

public class PlayerOpeHandlerNoRepetition implements OpeHandler {
    public static final Codec<PlayerOpeHandlerNoRepetition> CODEC = RecordCodecBuilder.create(a -> a.group(
            Registries.getOperatorTypeRegistry().byNameCodec().listOf().fieldOf("deploying").forGetter(i -> i.deploying),
            Registries.getOperatorTypeRegistry().byNameCodec().listOf().fieldOf("history").forGetter(i -> i.lastDeployingList),
            Operator.CODEC.listOf().xmap(list -> Helper.listElementAsValue(list, o -> o.identifier.type()), map -> map.values().stream().toList()).fieldOf("operators").forGetter(i -> i.operators),
            Codec.INT.fieldOf("max_deploying").forGetter(i -> i.maxDeployingListSize),
            Codec.BOOL.fieldOf("max_deploying").forGetter(i -> i.allowPlacePlaceholder),
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(i -> i.uuid)
    ).apply(a, PlayerOpeHandlerNoRepetition::new));


    public final List<OperatorType> deploying = new ArrayList<>();
    public final List<OperatorType> lastDeployingList = new ArrayList<>();

    public final Map<OperatorType, Operator> operators = new HashMap<>();

    public final int maxDeployingListSize;
    public final boolean allowPlacePlaceholder;

    @Nullable
    private ServerPlayer owner;
    @NonnullDefault
    private UUID uuid;

    public PlayerOpeHandlerNoRepetition(List<OperatorType> deploying, List<OperatorType> lastDeployingList, Map<OperatorType, Operator> operators, int maxDeployingListSize, boolean allowPlacePlaceholder, UUID playerUUID) {
        this.maxDeployingListSize = maxDeployingListSize;
        this.allowPlacePlaceholder = allowPlacePlaceholder;
        this.deploying.addAll(deploying);
        this.lastDeployingList.addAll(lastDeployingList);
        this.operators.putAll(operators);
        this.uuid = playerUUID;
    }

    public PlayerOpeHandlerNoRepetition(int maxDeployingListSize, boolean allowPlacePlaceholder) {
        this.maxDeployingListSize = maxDeployingListSize;
        this.allowPlacePlaceholder = allowPlacePlaceholder;
    }


    @Override
    public Codec<? extends OpeHandler> codec() {
        return CODEC;
    }

    @Override
    public Either<ServerPlayer, UUID> owner() {
        return owner == null ? Either.right(uuid) : Either.left(owner);
    }


    // ---[初始化与登出]---


    @Override
    public void init() {
        this.operators.values().forEach(o -> o.init(this));
    }

    @Override
    public void login(ServerPlayer owner) {
        this.owner = owner;
        if (uuid == null) {
            uuid = owner.getUUID();
        }
        this.operators.values().forEach(o -> o.login(owner));

        boolean flag = false;
        List<OperatorType> types = new ArrayList<>(deploying);
        for (int i = 0; i < types.size(); i++) {
            OperatorType type = types.get(i);
            if (type == OperatorTypeRegistry.PLACE_HOLDER.get()) continue;
            Operator operator = operators.get(type);
            if (operator == null || operator.getEntity() == null) {
                flag = true;
                deploying.set(i, OperatorTypeRegistry.PLACE_HOLDER.get());
            }
        }

        if (flag) {
            lastDeployingList.clear();
            lastDeployingList.addAll(types);
        }
    }

    @Override
    public void logout() {
        this.operators.values().forEach(Operator::logout);
        this.owner = null;
    }

    @Override
    public List<Operator> deploying() {
        return deploying.stream().map(operators::get).toList();
    }

    @Override
    public List<Operator> deployingHistory() {
        return lastDeployingList.stream().map(operators::get).toList();
    }

    @Override
    public Collection<Operator> operators() {
        return operators.values();
    }

    @Override
    public boolean unlock(OperatorType type) {
        if (operators.containsKey(type)) return false;
        operators.put(type, type.createDefaultInstance(this));
        return true;
    }

    @Override
    public boolean delete(Operator type) {
        if (!operators.containsValue(type)) return false;
        type.disconnectWithEntity();
        operators.remove(type.identifier.type());
        return true;
    }

    @Override
    public boolean fixDeploying(boolean redeployForListIncluded, boolean redeployForListExcluded) {
        //创建标记与缓存
        boolean flag = false;
        ArrayList<OperatorType> cache = new ArrayList<>(deploying);

        //寻找不在部署行列的
        List<Operator> notDeploying = operators.keySet().stream().filter(type -> !deploying.contains(type)).map(operators::get).toList();

        //遍历在部署行列的
        for (int i = 0; i < deploying.size(); i++) {
            //跳过占位符
            OperatorType type = deploying.get(i);
            if (type == OperatorTypeRegistry.PLACE_HOLDER.get()) continue;
            //获取对应的干员 如果为null或找不到实体，标记并修改部署列
            Operator operator = operators.get(type);
            if (operator == null || operator.getEntity() == null) {
                flag = true;
                deploying.set(i, OperatorTypeRegistry.PLACE_HOLDER.get());
                if (operator != null) {
                    //如果干员非null 要求自检
                    operator.checkSelf();
                    if (redeployForListIncluded) {//如果请求重部署，为其标记
                        operator.skipResting();
                        operator.deploy(false, true);
                    }
                }
            }
        }

        //遍历没在list里但是也被标记为部署的
        for (Operator operator : notDeploying) {
            if (operator.getStatus() == Operator.STATUS_TRACKING) {
                operator.disconnectWithEntity();
                flag = true;
                if (redeployForListExcluded) operator.deploy(false, true);
            }
        }

        if (flag) {
            lastDeployingList.clear();
            lastDeployingList.addAll(cache);
        }

        return flag;
    }

    @Override
    public boolean addDeploying(Operator ope, boolean simulate) {
        if (allowPlacePlaceholder) {
            for (int i = 0; i < deploying.size(); i++) {
                OperatorType type = deploying.get(i);
                if (type == OperatorTypeRegistry.PLACE_HOLDER.get()) {
                    if (!simulate) deploying.set(i, ope.identifier.type());
                    return true;
                }
            }
        }
        if (maxDeployingListSize == Integer.MAX_VALUE || deploying.size() < maxDeployingListSize) {
            if (!simulate) deploying.add(ope.identifier.type());
            return true;
        }
        return false;
    }

    @Override
    public void onRetreat(Operator operator) {

    }

    @Override
    public Optional<Operator> findOperator(Operator.Identifier identifier) {
        return Optional.ofNullable(operators.get(identifier.type()));
    }

    public void markDeployingChanged() {
        this.lastDeployingList.clear();
        this.lastDeployingList.addAll(this.deploying);
    }
}
