package com.landis.nonatomic.misc;

import com.landis.nonatomic.core.OperatorEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class VillagerKnightModel<T extends OperatorEntity> extends HumanoidModel<T> {
    public VillagerKnightModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void prepareMobModel(T mobEntity, float f, float g, float h) {
        this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        ItemStack itemStack = mobEntity.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.is(Items.BOW) && mobEntity.isAggressive()) {
            if (mobEntity.getMainArm() == HumanoidArm.RIGHT) {
                this.rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            } else {
                this.leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }
        super.prepareMobModel(mobEntity, f, g, h);
    }

    public boolean isAttacking(T entity) {
        return entity.isAggressive();
    }
}
