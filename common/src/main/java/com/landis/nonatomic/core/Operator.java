package com.landis.nonatomic.core;

import com.landis.nonatomic.EventHooks;
import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.misc.LevelAndPosRecorder;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.Optional;
import java.util.UUID;

public class Operator {
//    public final ResourceLocation ID;
//    public final OperatorPattern ROOT;

    @NonnullDefault
    public ServerPlayer player;

    public void init(ServerPlayer player){
        this.player = player;
    }


}
