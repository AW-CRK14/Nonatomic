package com.landis.nonatomic.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

//WARN: 不要在此处写任何变量形式的内容 就像block item那样
public abstract class OperatorType {
    public abstract Operator createDefaultInstance(OpeHandler handler);

    public static class Placeholder extends OperatorType {
        @Override
        public Operator createDefaultInstance(OpeHandler handler) {
            throw new UnsupportedOperationException();
        }
    }

    public boolean allowDeploy(Player player, Operator operator) {
        return true;
    }

    public void onDeploy(OperatorEntity entity, Player player, Operator operator) {
    }

    public boolean allowRetreat(OperatorEntity entity, Either<ServerPlayer,UUID> player, Operator operator) {
        return true;
    }

    public void onRetreat(Either<ServerPlayer,UUID> player, Operator operator) {
    }
}
