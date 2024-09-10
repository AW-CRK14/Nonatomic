package com.landis.nonatomic.fabric.mixin;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Deprecated
@Mixin(ChunkMap.class)
public class ChunkMapMixin {

    @Inject(method = "method_18843",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V", shift = At.Shift.AFTER))
    private void scheduleUnload(ChunkHolder chunkHolder, CompletableFuture completableFuture, long l, ChunkAccess chunkAccess, CallbackInfo ci){
//        EventConsumer.onChunkUnload((LevelChunk) chunkAccess);
    }
}
