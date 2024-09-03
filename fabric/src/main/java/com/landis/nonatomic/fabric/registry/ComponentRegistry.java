package com.landis.nonatomic.fabric.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

public class ComponentRegistry implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(OPE_HANDLER, OpeHandler::new, RespawnCopyStrategy.CHARACTER);
    }

    public static final ComponentKey<OpeHandler> OPE_HANDLER = ComponentRegistryV3.INSTANCE.getOrCreate(new ResourceLocation(Nonatomic.MOD_ID, "ope_holder"), OpeHandler.class);


    public static class OpeHandler extends Component<PlayerOpeHandlerNoRepetition>{
        public OpeHandler(LivingEntity entity) {
            super(entity, PlayerOpeHandlerNoRepetition::new, PlayerOpeHandlerNoRepetition.CODEC);
        }
    }

    public static class Component<T> implements ComponentV3, AutoSyncedComponent {
        public LivingEntity getEntity() {
            return entity;
        }

        public T getData() {
            return data;
        }

        private final LivingEntity entity;
        private final Codec<T> codec;
        private T data;

        public Component(LivingEntity entity, Supplier<T> instanceCreator, Codec<T> codec) {
            this.entity = entity;
            this.data = instanceCreator.get();
            this.codec = codec;
        }


        @Override
        public void readFromNbt(CompoundTag tag) {
            data = codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, e -> {
            });
        }

        @Override
        public void writeToNbt(CompoundTag tag) {
            codec.encode(data, NbtOps.INSTANCE, tag);
        }
    }

}
