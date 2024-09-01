package com.landis.nonatomic.core;

import com.mojang.serialization.Codec;
import org.lwjgl.system.NonnullDefault;

public abstract class OperatorInfo<I extends OperatorInfo<I>> {
    @NonnullDefault
    public Operator operator;


    public abstract Codec<I> codec();

    public abstract I copy();

    /**干员在部署时复制数据，在撤退时触发本方法
     * @param newData 实体保有的数据，用于和此数据合并
     * @return 实体继续保有的方法——不一定被使用
     * */
    public I modifyData(final I newData) {
        return (I) this;
    }

    public void init(Operator owner){
        this.operator = owner;
    }

    public void preLogout(){}
}
