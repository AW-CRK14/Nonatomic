package com.landis.nonatomic.core;

import com.landis.nonatomic.Registries;
import com.landis.nonatomic.core.info.IAttributesProvider;
import com.landis.nonatomic.core.info.IBelongingOperatorProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.util.*;

public abstract class OperatorInfo implements IBelongingOperatorProvider {
    public static final Codec<OperatorInfo> CODEC = Registries.getOperatorInfoRegistry().byNameCodec().dispatch(OperatorInfo::codec, i -> i);


    //empty -> 该数据为外部数据  否则为干员持久化数据
    @NonnullDefault
    public Operator operator;


    public abstract Codec<? extends OperatorInfo> codec();

    /**
     * 标记数据合并 传入数据为不完全数据
     *
     * @param newData 实体保有的数据，用于和此数据合并
     */
    public abstract <T extends OperatorInfo> void merge(final T newData);

    protected abstract <T extends OperatorInfo> T createExternal();


    public void login() {
    }

    public void init(Operator operator) {
        this.operator = operator;
    }

    public void logout() {
    }

    public void entityInit(OperatorEntity entity){
    }

    public void entityCreated(OperatorEntity entity){
        entityInit(entity);
    }



    public Operator getBelonging(){
        return operator;
    }



}
