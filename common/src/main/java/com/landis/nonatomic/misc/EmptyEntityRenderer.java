package com.landis.nonatomic.misc;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class EmptyEntityRenderer<T extends Entity> extends EntityRenderer<T> {
    public EmptyEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(Entity entity, Frustum frustum, double d, double e, double f) {
        return true;
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return new ResourceLocation("minecraft","empty");
    }
}
