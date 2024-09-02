package com.landis.nonatomic.core;

import com.landis.nonatomic.core.player_opehandler.OpeHandler;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class LevelInfoHolder {

    public final HashMap<UUID, HashMap<Operator.Identifier, Optional<UUID>>> playerOpeDeployMap = new HashMap<>();

    public final HashMap<UUID, HashMap<Operator.Identifier, Optional<LevelAndPosRecorder>>> playerOpeLastPlaceMap = new HashMap<>();

    public void postDeployID(Optional<UUID> opeID, Operator operator) {
        playerOpeDeployMap.computeIfAbsent(operator.player.getUUID(), k -> new HashMap<>()).put(operator.identifier, opeID);
    }

    public void postLastPlace(OperatorEntity entity) {//TODO
        Optional<LevelAndPosRecorder> optional = Optional.of(new LevelAndPosRecorder(entity));
    }

    public void postDeath(OperatorEntity entity) {//TODO
        Optional<LevelAndPosRecorder> optional = Optional.empty();
    }

    public void synToPlayer(ServerPlayer player) {
        //TODO 从什么地方拿到数据
        OpeHandler handler = null;

        HashMap<Operator.Identifier, Optional<LevelAndPosRecorder>> map = playerOpeLastPlaceMap.get(player.getUUID());
        if (map != null) {
            map.forEach((identifier, levelAndPosRecorder) -> {
                handler.findOperator(identifier).ifPresent(operator -> operator.deploy.lastPos = levelAndPosRecorder);
            });
        }
    }
}
