package com.landis.nonatomic.core.player_opehandler;

import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OpeHandler {
    Codec<? extends OpeHandler> codec();

    void init(Player owner);

    Player owner();

    boolean addDeploying(Operator ope, boolean fillPlaceholder, boolean expandList);

    void setDeploying(List<Operator> deploying);

    boolean fixDeploying(boolean redeploy);

    List<Operator> getDeploying();

    List<OperatorType> getDeployingHistory();

    Collection<Operator> getOperators();

    Optional<Operator> findOperator(Operator.Identifier identifier);
}
