package com.landis.nonatomic.core;

import com.landis.nonatomic.Registries;
import com.mojang.serialization.Codec;
import org.lwjgl.system.NonnullDefault;

public abstract class OperatorInfo {
    public static final Codec<OperatorInfo> CODEC = Registries.getOperatorInfoRegistry().byNameCodec().dispatch(OperatorInfo::codec, i -> i);

    @NonnullDefault
    public Operator operator;


    public abstract Codec<? extends OperatorInfo> codec();

    /**
     * 标记数据合并 传入数据为不完全数据
     *
     * @param newData 实体保有的数据，用于和此数据合并
     */
    public abstract void merge(final OperatorInfo newData);

    public void init(Operator owner) {
        this.operator = owner;
    }

    public void preLogout() {
    }
}
