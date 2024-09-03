package com.landis.nonatomic.core.player_opehandler;

import com.landis.nonatomic.Helper;
import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorType;
import com.landis.nonatomic.datagroup.Deploy;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.system.NonnullDefault;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerOpeHandlerNoRepetition implements OpeHandler {


    public static final Codec<PlayerOpeHandlerNoRepetition> CODEC = RecordCodecBuilder.create(a -> a.group(
            Registries.getOperatorTypeRegistry().byNameCodec().listOf().fieldOf("deploying").forGetter(i -> i.deploying),
            Registries.getOperatorTypeRegistry().byNameCodec().listOf().fieldOf("history").forGetter(i -> i.lastDeployingList),
            Operator.CODEC.listOf().xmap(list -> Helper.listElementAsValue(list, o -> o.identifier.type()), map -> map.values().stream().toList()).fieldOf("operators").forGetter(i -> i.operators)
    ).apply(a, PlayerOpeHandlerNoRepetition::new));


    public final List<OperatorType> deploying = new ArrayList<>();
    public final List<OperatorType> lastDeployingList = new ArrayList<>();

    public final Map<OperatorType, Operator> operators = new HashMap<>();

    public PlayerOpeHandlerNoRepetition(List<OperatorType> deploying, List<OperatorType> lastDeployingList, Map<OperatorType, Operator> operators) {
        this.deploying.addAll(deploying);
        this.lastDeployingList.addAll(lastDeployingList);
        this.operators.putAll(operators);
    }

    public PlayerOpeHandlerNoRepetition() {
    }

    @NonnullDefault
    public Player owner;

    public void init(Player owner) {
        this.owner = owner;
        operators.values().forEach(operator -> operator.init(owner, this));
        if (!deploying.isEmpty()) {
            List<OperatorType> cache = List.copyOf(deploying);
            boolean flag = false;
            Operator ope;
            for (int i = 0; i < deploying.size(); i++) {
                ope = operators.get(deploying.get(i));
                if (ope == null || ope.deploy.getEntity() == null) {
                    flag = true;
                    deploying.set(i, OperatorTypeRegistry.PLACE_HOLDER.get());
                    if (ope != null) {
                        ope.deploy.retreat(true, false, null, true);
                    }
                }
            }
            if (flag) {
                lastDeployingList.clear();
                lastDeployingList.addAll(cache);
            }
        }
    }

    //return -> 是否执行了任何操作
    public boolean fixDeploying(boolean redeploy) {
        if (owner == null || !(owner instanceof ServerPlayer)) {
            return false;
        }
        List<OperatorType> cache = List.copyOf(deploying);
        AtomicBoolean cacheFlag = new AtomicBoolean(false);
        AtomicBoolean flag = new AtomicBoolean(false);
        operators.forEach((operatorType, operator) -> {
            if (deploying.contains(operatorType)) {
                //如果没有找到标记的跟随状态实体
                if (operator.deploy.getEntity() == null) {
                    operator.deploy.retreat(true, false, null, true);
                    for (int i = 0; i < deploying.size(); i++) {
                        if (deploying.get(i).equals(operatorType)) {
                            deploying.set(i, OperatorTypeRegistry.PLACE_HOLDER.get());
                            break;
                        }
                    }
                    cacheFlag.set(true);
                    flag.set(true);
                }
            } else {
                if (operator.deploy.status == Deploy.STATUS_TRACKING) {
                    operator.deploy.retreat(true, false, null, true);
                    flag.set(true);
                }
            }
        });
        if (cacheFlag.get()) {
            lastDeployingList.clear();
            lastDeployingList.addAll(cache);
        }
        return flag.get();
    }

    @Override
    public List<Operator> getDeploying() {
        return deploying.stream().map(operators::get).toList();
    }

    @Override
    public List<OperatorType> getDeployingHistory() {
        return lastDeployingList;
    }

    @Override
    public Collection<Operator> getOperators() {
        return operators.values();
    }

    @Override
    public Optional<Operator> findOperator(Operator.Identifier identifier) {
        return Optional.ofNullable(operators.get(identifier.type()));
    }

    //WARN: 设置部署的方法不包含进行部署行为本身，仅是标记部署列表

    public void setDeploying(List<Operator> deploying) {
        setDeployingTypes(deploying.stream().map(o -> o.identifier.type()).toList());
    }

    public void setDeployingTypes(List<OperatorType> deploying) {
        markDeployingChanged();

        HashSet<OperatorType> set = new HashSet<>();
        this.deploying.clear();
        this.deploying.addAll(deploying.stream().filter(type -> type == OperatorTypeRegistry.PLACE_HOLDER.get() || set.add(type)).toList());
    }

    public boolean addDeploying(Operator operator, boolean fillPlaceholder, boolean expandList) {
        return addDeploying(operator.identifier.type(), fillPlaceholder, expandList);
    }

    public boolean addDeploying(OperatorType type, boolean fillPlaceholder, boolean expandList) {
        if (deploying.contains(type)) return false;
        if (!fillPlaceholder && !expandList) return false;
        if (fillPlaceholder) {
            for (int i = 0; i < deploying.size(); i++) {
                if (deploying.get(i).equals(OperatorTypeRegistry.PLACE_HOLDER.get())) {
                    deploying.set(i, type);
                    markDeployingChanged();
                    return true;
                }
            }
        }
        if (expandList) {
            try {
                deploying.add(type);
                markDeployingChanged();
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    public void markDeployingChanged() {
        this.lastDeployingList.clear();
        this.lastDeployingList.addAll(this.deploying);
    }


    @Override
    public Codec<? extends OpeHandler> codec() {
        return CODEC;
    }

    @Override
    public Player owner() {
        return owner;
    }
}
