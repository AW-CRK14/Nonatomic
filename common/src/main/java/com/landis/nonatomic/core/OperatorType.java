package com.landis.nonatomic.core;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

//WARN: 不要在此处写任何变量形式的内容 就像block item那样
public abstract class OperatorType {
    public abstract Operator createDefaultInstance(OpeHandler handler);

    public abstract @Nullable EntityType<? extends OperatorEntity> getEntityType();

    public OperatorEntity createEntityInstance(Operator operator, ServerPlayer belonging) {
        return new OperatorEntity(getEntityType(), (ServerLevel) belonging.level(), belonging, operator);
    }

    public BlockPos findPlaceForGenerate(ServerPlayer player, @Nullable BlockPos pos) {
        if (getEntityType() == null) return null;

        Level level = player.level();
        if (pos != null) {
            if (getEntityType() != null && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()) {
                BlockState stateBelow = level.getBlockState(pos.below());
                if (!stateBelow.isAir() && stateBelow.getFluidState().isEmpty() && !getEntityType().isBlockDangerous(stateBelow))
                    return pos;
            }
        }
        List<BlockPos> safePos = new ArrayList<>();
        int safeFlag = 0;
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                for (int y = 4 + 2; y >= -1 - 1; y--) {
                    pos = player.blockPosition().offset(x, y, z);
                    if (safeFlag != 2) {
                        if (level.getBlockState(pos).isAir()) safeFlag++;
                        else safeFlag = 0;
                    } else {
                        BlockState state = level.getBlockState(pos);
                        if (!state.isAir()) {
                            if (state.getFluidState().isEmpty() && !getEntityType().isBlockDangerous(state)) {
                                safePos.add(pos.above());
                            }
                            safeFlag = 0;
                        }
                    }
                }
                safeFlag = 0;
            }
        }
        if(safePos.isEmpty()) return null;
        else return safePos.get(new Random().nextInt(0, safePos.size()));
    }

    public static class Placeholder extends OperatorType {
        @Override
        public Operator createDefaultInstance(OpeHandler handler) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable EntityType<? extends OperatorEntity> getEntityType() {
            throw new UnsupportedOperationException();
        }
    }

    public boolean allowDeploy(ServerPlayer player, Operator operator) {
        return getEntityType() != null;
    }

    public void onDeploy(OperatorEntity entity, Player player, Operator operator) {
    }

    public boolean allowRetreat(OperatorEntity entity, Either<ServerPlayer, UUID> player, Operator operator) {
        return true;
    }

    public void onRetreat(Either<ServerPlayer, UUID> player, Operator operator) {
    }
}
