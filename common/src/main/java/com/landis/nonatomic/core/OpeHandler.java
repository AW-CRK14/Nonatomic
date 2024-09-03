package com.landis.nonatomic.core;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpeHandler {
    Codec<? extends OpeHandler> codec();

    void init();
    void login(ServerPlayer owner);

    void logout();


    Either<ServerPlayer,UUID> owner();

    List<Operator> deploying();

    List<Operator> deployingHistory();

    Collection<Operator> operators();


    void markDeployingChanged();


    boolean unlock(OperatorType type);

    boolean delete(Operator type);


    boolean fixDeploying(boolean redeployForListIncluded, boolean redeployForListExcluded);

    //WARN:不要直接调用以下方法！请使用operator内的方法

    /**
     * 向部署列添加一个干员
     *
     * @param ope      需要添加的干员
     * @param simulate 是否为模拟
     * @return 是否允许
     */
    @Deprecated
    boolean addDeploying(Operator ope, boolean simulate);

    @Deprecated
    void onRetreat(Operator operator);


    Optional<Operator> findOperator(Operator.Identifier identifier);
}
