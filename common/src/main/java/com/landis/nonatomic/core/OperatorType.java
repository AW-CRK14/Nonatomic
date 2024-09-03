package com.landis.nonatomic.core;

import net.minecraft.world.entity.player.Player;

//WARN: 不要在此处写任何变量形式的内容 就像block item那样
public abstract class OperatorType {
    public abstract Operator createDefaultInstance();

    public static class Placeholder extends OperatorType {
        @Override
        public Operator createDefaultInstance() {
            throw new UnsupportedOperationException();
        }
    }

    public boolean allowDeploy(Player player, Operator operator) {
        return true;
    }

    public void onDeploy(OperatorEntity entity, Player player, Operator operator) {
    }

    public boolean allowRetreat(OperatorEntity entity, Player player, Operator operator) {
        return true;
    }

    public void onRetreat(Player player, Operator operator) {
    }
}
