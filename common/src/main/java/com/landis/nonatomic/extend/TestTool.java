package com.landis.nonatomic.extend;

import com.landis.nonatomic.AttachedData;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.core.player_opehandler.OpeHandlerNoRepetition;
import com.landis.nonatomic.registry.EntityTypeRegistry;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class TestTool extends Item {
    public TestTool() {
        super(new Properties());
    }

    public static class I1 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                AttachedData.opeHandlerGroupProvider(level.getServer()).deploy(OperatorTypeRegistry.TEST_OPERATOR.get(),(ServerPlayer) player,interactionHand == InteractionHand.MAIN_HAND ? player.blockPosition().north(3) : null);
            }
            return super.use(level, player, interactionHand);
        }
    }

    public static class I2 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                BlockPos pos;
                int safeFlag = 0;
                for (int x = -8; x <= 8; x++) {
                    for (int z = -8; z <= 8; z++) {
                        for (int y = 4 + 2; y >= -1 - 1; y--) {
                            pos = player.blockPosition().offset(x, y, z);
                            BlockState state = level.getBlockState(pos);
                            if (safeFlag != 2) {
                                if (level.getBlockState(pos).isAir() || state.getCollisionShape(level,pos).isEmpty()) {
                                    level.setBlock(pos,Blocks.YELLOW_STAINED_GLASS.defaultBlockState(),3);
                                    safeFlag++;
                                } else safeFlag = 0;
                            } else {
                                if (!state.isAir() && !state.getCollisionShape(level,pos).isEmpty()) {
                                    if (state.getFluidState().isEmpty()) {
                                        level.setBlock(pos, Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
                                    }
                                    safeFlag = 0;
                                }
                            }
                        }
                        safeFlag = 0;
                    }
                }
            }
            return super.use(level, player, interactionHand);
        }
    }



}
