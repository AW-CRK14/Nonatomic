package com.phasetranscrystal.nonatomic.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface OpeHandler {

    Codec<? extends OpeHandler> codec();

    void init();

    void login(ServerPlayer owner);

    void logout();

    void refresh(ServerPlayer owner);

    void dead();


    @Nullable
    ServerPlayer owner();

    UUID ownerUUId();

    default Either<ServerPlayer, UUID> ownerOrUUID() {
        return owner() == null ? Either.right(ownerUUId()) : Either.left(owner());
    }


    List<Operator> deploying();

    default List<Operator> filteredDeploying() {
        return deploying().stream().filter(Objects::nonNull).toList();
    }

    List<Operator> deployingHistory();

    Collection<Operator> operators();


    void markDeployingChanged();


    boolean unlock(OperatorType type);

    boolean delete(Operator type);

    //WARN:不要直接调用以下方法！请使用operator内的方法

    /**
     * 向部署列添加一个干员
     *
     * @param ope           需要添加的干员
     * @param exceptIndex   请求的索引位置 <0代表不进行额外请求
     * @param simulate      是否为模拟
     * @param allowDispatch 是否允许调度 在目标位置不被允许的情况下尝试找到其它位置
     * @return 找到的位置索引 -1表示未找到
     */
    @Deprecated
    int addDeploying(Operator ope, int exceptIndex, boolean simulate, boolean allowDispatch);

    @Deprecated
    default int addDeploying(Operator ope, boolean simulate) {
        return addDeploying(ope, -1, simulate, true);
    }

    ;

    //返回部署位置的索引 没有找到请返回-1 其它情况请根据需求自己设定
    @Deprecated
    int onRetreat(Operator operator);


    Optional<Operator> findOperator(Operator.Identifier identifier);

    interface GroupProvider {
        Optional<? extends OpeHandler> withUUID(UUID playerUUID, MinecraftServer server);

        default Optional<? extends OpeHandler> withPlayer(ServerPlayer player) {
            return withUUID(player.getUUID(), player.getServer());
        }

        ;
    }
}
