package com.landis.nonatomic.fabric.registry;

import com.landis.nonatomic.Nonatomic;
import com.landis.nonatomic.core.player_opehandler.PlayerOpeHandlerNoRepetition;
import com.mojang.serialization.Codec;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class ComponentRegistry implements WorldComponentInitializer {
    @Override
    public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
        registry.register(OPE_HANDLER, level -> new OpeHandler());
    }

    public static final ComponentKey<OpeHandler> OPE_HANDLER = ComponentRegistryV3.INSTANCE.getOrCreate(new ResourceLocation(Nonatomic.MOD_ID, "ope_holder"), OpeHandler.class);



    public static class OpeHandler extends Component<PlayerOpeHandlerNoRepetition.LevelContainer>{
        public OpeHandler() {
            super(()-> new PlayerOpeHandlerNoRepetition.LevelContainer(4,true), PlayerOpeHandlerNoRepetition.LevelContainer.CODEC);
        }
    }

    public static class Component<T> implements ComponentV3, AutoSyncedComponent {

        public T getData() {
            return data;
        }

        private final Codec<T> codec;
        private T data;

        public Component(Supplier<T> instanceCreator, Codec<T> codec) {
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

    public record ComponentAgency<T>(T target) implements ComponentV3, AutoSyncedComponent {
        @Override
        public void readFromNbt(CompoundTag tag) {

        }

        @Override
        public void writeToNbt(CompoundTag tag) {

        }
    }

}
