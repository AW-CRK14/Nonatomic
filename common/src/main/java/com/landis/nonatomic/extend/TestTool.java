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

public class TestTool extends Item {
    public TestTool() {
        super(new Properties());
    }

    public static class I1 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                OpeHandlerNoRepetition.LevelContainer container = AttachedData.opeHandlerGroupProvider(level.getServer());
                BlockPos pos = OperatorTypeRegistry.TEST_OPERATOR.get().findPlaceForGenerate((ServerPlayer) player, interactionHand == InteractionHand.OFF_HAND ? player.blockPosition().north(3) : null);
                OperatorEntity entity = new OperatorEntity(EntityTypeRegistry.TEST.get(), (ServerLevel) level, player, new Operator(new Operator.Identifier(OperatorTypeRegistry.TEST_OPERATOR.get())));
                if(!entity.isRemoved()){
                    entity.setPos(pos.getX(), pos.getY(), pos.getZ());
                    level.addFreshEntity(entity);
                }
                System.out.println("testT1 run");
            }
            return super.use(level, player, interactionHand);
        }
    }

    public static class I2 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                AttachedData.opeHandlerGroupProvider(level.getServer()).getDataFor((ServerPlayer) player).unlock(OperatorTypeRegistry.TEST_OPERATOR.get());

                System.out.println("testT2 run");
            }
            return super.use(level, player, interactionHand);
        }
    }



}
