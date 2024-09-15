package com.phasetranscrystal.nonatomic.core.player_opehandler;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phasetranscrystal.nonatomic.EventHooks;
import com.phasetranscrystal.nonatomic.Helper;
import com.phasetranscrystal.nonatomic.Nonatomic;
import com.phasetranscrystal.nonatomic.Registries;
import com.phasetranscrystal.nonatomic.core.OpeHandler;
import com.phasetranscrystal.nonatomic.core.Operator;
import com.phasetranscrystal.nonatomic.core.OperatorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.*;

public class OpeHandlerNoRepetition implements OpeHandler {
    public static final Codec<OpeHandlerNoRepetition> CODEC = RecordCodecBuilder.create(a -> a.group(
            Registries.OPERATOR_TYPE.byNameCodec().listOf().fieldOf("deploying").forGetter(i -> i.deploying),
            Registries.OPERATOR_TYPE.byNameCodec().listOf().fieldOf("history").forGetter(i -> i.lastDeployingList),
            Helper.mapLikeWithKeyProvider(Operator.CODEC, Operator::getType).fieldOf("operators").forGetter(i -> i.operators),
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(i -> i.uuid),
            ResourceLocation.CODEC.fieldOf("container_id").forGetter(i -> i.containerId)
    ).apply(a, OpeHandlerNoRepetition::new));


    public final List<OperatorType> deploying;
    public final List<OperatorType> lastDeployingList = new ArrayList<>();

    public final Map<OperatorType, Operator> operators = new HashMap<>();
    public final ResourceLocation containerId;


    @Nullable
    private ServerPlayer owner;
    @NonnullDefault
    private UUID uuid;

    public OpeHandlerNoRepetition(List<OperatorType> deploying, List<OperatorType> lastDeployingList, Map<OperatorType, Operator> operators, UUID playerUUID, ResourceLocation containerId) {
        this.containerId = containerId;
        this.deploying = new ArrayList<>(deploying);
        this.lastDeployingList.addAll(lastDeployingList);
        this.operators.putAll(operators);
        this.uuid = playerUUID;

        init();
    }

    public OpeHandlerNoRepetition(int maxDeployingListSize, ResourceLocation containerId) {
        this.containerId = containerId;
        this.deploying = new ArrayList<>();
        for (int i = 0; i < maxDeployingListSize; i++) {
            deploying.add(Nonatomic.PLACE_HOLDER.get());
        }

        init();
    }

    public OpeHandlerNoRepetition(int maxDeployingListSize, ServerPlayer player, ResourceLocation containerId) {
        this.containerId = containerId;
        this.deploying = new ArrayList<>();
        for (int i = 0; i < maxDeployingListSize; i++) {
            deploying.add(Nonatomic.PLACE_HOLDER.get());
        }

        init();

        login(player);
    }


    @Override
    public Codec<? extends OpeHandler> codec() {
        return CODEC;
    }

    @Override
    public @Nullable ServerPlayer owner() {
        return owner;
    }

    @Override
    public UUID ownerUUId() {
        return uuid;
    }

    @Override
    public ResourceLocation containerId() {
        return containerId;
    }

    public int maxDeployingCount() {
        return deploying.size();
    }

    // ---[初始化与登出]---


    @Override
    public void init() {
        this.operators.values().forEach(o -> o.init(this));
    }

    @Override
    public void login(ServerPlayer owner) {
        if (uuid == null) {
            this.owner = owner;
            uuid = owner.getUUID();
        } else if (!owner.getUUID().equals(uuid)) {
            throw new IllegalArgumentException("The UUID is not the same as the owner UUID");
        } else {
            this.owner = owner;
        }
        this.operators.values().forEach(o -> o.login(owner));

        boolean flag = false;
        List<OperatorType> types = new ArrayList<>(deploying);
        for (int i = 0; i < types.size(); i++) {
            OperatorType type = types.get(i);
            if (type == Nonatomic.PLACE_HOLDER.get()) continue;
            Operator operator = operators.get(type);
            if (operator == null || operator.getEntity() == null) {
                flag = true;
                deploying.set(i, Nonatomic.PLACE_HOLDER.get());
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
    public void refresh(ServerPlayer owner) {
        if (owner.getUUID().equals(uuid)) {
            this.owner = owner;
        }
    }

    @Override
    public List<Operator> deploying() {
        return deploying.stream().map(operators::get).toList();
    }

    @Override
    public List<Operator> filteredDeploying() {
        return deploying.stream().map(operators::get).filter(Objects::nonNull).toList();
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
        Operator operator = type.createDefaultInstance(this);
        operator.init(this);
        if (owner != null) operator.login(owner);
        operators.put(type, operator);
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
    public int addDeploying(Operator ope, int exceptIndex, boolean simulate, boolean allowDispatch) {
        if (exceptIndex >= deploying.size() || deploying.contains(ope.getType())) return -1;
        if (exceptIndex >= 0) {
            if (deploying.get(exceptIndex) == Nonatomic.PLACE_HOLDER.get()) {
                if (!simulate) deploying.set(exceptIndex, ope.identifier.type());
                return exceptIndex;
            } else if (!allowDispatch) return -1;
        }
        for (int i = 0; i < deploying.size(); i++) {
            OperatorType type = deploying.get(i);
            if (type == Nonatomic.PLACE_HOLDER.get()) {
                if (!simulate) deploying.set(i, ope.identifier.type());
                return i;
            }
        }
        return -1;
    }

    @Override
    public int onRetreat(Operator operator) {
        for (int i = 0; i < deploying.size(); i++) {
            if (deploying.get(i) == operator.getType()) {
                deploying.set(i, Nonatomic.PLACE_HOLDER.get());
                return i;
            }
        }
        return -1;
    }

    @Override
    public Optional<Operator> findOperator(Operator.Identifier identifier) {
        return Optional.ofNullable(operators.get(identifier.type()));
    }

    public void markDeployingChanged() {
        this.lastDeployingList.clear();
        this.lastDeployingList.addAll(this.deploying);
    }

    public static class LevelContainer implements OpeHandler.GroupProvider {
        public static final Codec<LevelContainer> CODEC = RecordCodecBuilder.create(n -> n.group(
                Helper.mapLikeWithKeyProvider(OpeHandlerNoRepetition.CODEC, h -> h.uuid).fieldOf("data").forGetter(i -> i.data),
                Codec.INT.fieldOf("max_deploying").forGetter(i -> i.maxDeploying),
                ResourceLocation.CODEC.fieldOf("container_id").forGetter(i -> i.containerId)
        ).apply(n, LevelContainer::new));

        private final Map<UUID, OpeHandlerNoRepetition> data;
        public final int maxDeploying;
        public final ResourceLocation containerId;

        protected LevelContainer(Map<UUID, OpeHandlerNoRepetition> data, int maxDeploying, ResourceLocation containerId) {
            this.containerId = containerId;
            this.data = data;
            this.maxDeploying = maxDeploying;
        }

        public LevelContainer(int maxDeploying, ResourceLocation containerId) {
            this.containerId = containerId;
            this.data = new HashMap<>();
            this.maxDeploying = maxDeploying;
        }

        public boolean deploy(OperatorType type, ServerPlayer player, BlockPos expectPos) {
            return findOperator(type, player).map(o -> o.deploy(true, false, expectPos) >= 0).orElseGet(() -> {
                EventHooks.deployFailed(null, player, -7);
                return false;
            });
        }

        public Optional<Operator> findOperator(OperatorType type, ServerPlayer player) {
            return withPlayer(player).flatMap(handler -> handler.findOperator(new Operator.Identifier(type)));
        }

        @Override
        public Optional<? extends OpeHandler> withUUID(UUID playerUUID) {
            return Optional.ofNullable(data.get(playerUUID));
        }

        @Override
        public Optional<? extends OpeHandler> withPlayer(ServerPlayer player) {
            return Optional.of(data.computeIfAbsent(player.getUUID(), uuid -> new OpeHandlerNoRepetition(maxDeploying, player, containerId)));
        }
    }
}
