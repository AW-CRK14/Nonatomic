package com.landis.nonatomic.extend;

import com.landis.nonatomic.AttachedData;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import net.minecraft.core.BlockPos;
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
                var info = AttachedData.opeHandlerGroupProvider(level.getServer()).getDataFor((ServerPlayer) player);
                StringBuilder builder = new StringBuilder();
                builder.append("[");
                for(int i = 0; i < info.deploying.size(); i++) {

                }
            }
            return super.use(level, player, interactionHand);
        }
    }

    public static class I2 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                AttachedData.opeHandlerGroupProvider(player.getServer()).getDataFor((ServerPlayer) player).unlock(OperatorTypeRegistry.TEST_OPERATOR.get());
                System.out.println("t2");
            }
            return super.use(level, player, interactionHand);
        }
    }

    public static class I3 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                AttachedData.opeHandlerGroupProvider(player.getServer()).getDataFor((ServerPlayer) player).operators().forEach(Operator::skipResting);
            }
            return super.use(level, player, interactionHand);
        }
    }



}
