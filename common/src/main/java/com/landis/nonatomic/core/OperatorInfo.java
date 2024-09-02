package com.landis.nonatomic.core;

import com.landis.nonatomic.Registries;
import com.mojang.serialization.Codec;
import org.lwjgl.system.NonnullDefault;

public abstract class OperatorInfo {
    public static final Codec<OperatorInfo> CODEC = Registries.getOperatorInfoRegistry().byNameCodec().dispatch(OperatorInfo::codec, i -> i);

    @NonnullDefault
    public Operator operator;


    public abstract Codec<? extends OperatorInfo> codec();

    public abstract <T extends OperatorInfo> T copy();

    /**
     * 干员在部署时复制数据，在撤退时触发本方法
     *
     * @param newData 实体保有的数据，用于和此数据合并
     * @return 实体继续保有的方法——不一定被使用
     */
    public <T extends OperatorInfo> T modifyData(final T newData) {
        return (T) this;
    }

    public void init(Operator owner) {
        this.operator = owner;
    }

    public void preLogout() {
    }
}
