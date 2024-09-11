package com.landis.nonatomic.extend;

import com.landis.nonatomic.AttachedData;
import com.landis.nonatomic.core.Operator;
import com.landis.nonatomic.core.OperatorEntity;
import com.landis.nonatomic.registry.OperatorTypeRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TestTool extends Item {
    public TestTool() {
        super(new Properties());
    }

    public static class I1 extends TestTool {
        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
            if (!level.isClientSide()) {
                AttachedData.opeHandlerGroupProvider(level.getServer()).findOperator(OperatorTypeRegistry.TEST_OPERATOR.get(), (ServerPlayer) player).get().deploy(true,false,-1,null,Operator.STATUS_WORKING);
                var info = AttachedData.opeHandlerGroupProvider(level.getServer()).getDataFor((ServerPlayer) player);
            }
            return super.use(level, player, interactionHand);
        }
    }

    public static class I2 extends TestTool {

        @Override
        public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
            if(player instanceof ServerPlayer p && livingEntity instanceof OperatorEntity ope) {
                AttachedData.opeHandlerGroupProvider(p.getServer()).findOperator(OperatorTypeRegistry.TEST_OPERATOR.get(), (ServerPlayer) player).get().retreat(false);
            }
            return super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
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
