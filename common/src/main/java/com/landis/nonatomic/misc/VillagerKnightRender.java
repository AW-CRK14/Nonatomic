package com.landis.nonatomic.misc;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.OperatorEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class VillagerKnightRender<T extends OperatorEntity> extends HumanoidMobRenderer<T, VillagerKnightModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Nonatomic.MOD_ID, "textures/entity/villager_knight.png");
    public VillagerKnightRender(EntityRendererProvider.Context context) {
        this(context, ModelLayers.PLAYER, ModelLayers.PLAYER_INNER_ARMOR, ModelLayers.PLAYER_OUTER_ARMOR);
    }

    public VillagerKnightRender(EntityRendererProvider.Context ctx, ModelLayerLocation layer, ModelLayerLocation legsArmorLayer, ModelLayerLocation bodyArmorLayer) {
        this(ctx, new VillagerKnightModel<>(ctx.bakeLayer(layer)), new VillagerKnightModel<>(ctx.bakeLayer(legsArmorLayer)), new VillagerKnightModel<>(ctx.bakeLayer(bodyArmorLayer)));
    }
    protected VillagerKnightRender(EntityRendererProvider.Context ctx, VillagerKnightModel<T> bodyModel, VillagerKnightModel<T> legsArmorModel, VillagerKnightModel<T> bodyArmorModel) {
        super(ctx, bodyModel, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, legsArmorModel, bodyArmorModel, ctx.getModelManager()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(OperatorEntity entity) {
        return TEXTURE;
    }

}


