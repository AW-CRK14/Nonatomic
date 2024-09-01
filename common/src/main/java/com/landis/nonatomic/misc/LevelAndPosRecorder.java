package com.landis.nonatomic.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record LevelAndPosRecorder(ResourceKey<Level> level, BlockPos pos) {
    public static final Codec<LevelAndPosRecorder> CODEC = RecordCodecBuilder.create(a -> a.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(LevelAndPosRecorder::level),
            BlockPos.CODEC.fieldOf("pos").forGetter(LevelAndPosRecorder::pos)
    ).apply(a, LevelAndPosRecorder::new));

    public LevelAndPosRecorder(Entity entity) {
        this(entity.level().dimension(), entity.blockPosition());
    }
}
