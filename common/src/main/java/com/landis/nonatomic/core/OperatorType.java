package com.landis.nonatomic.core;

//WARN: 不要在此处写任何变量形式的内容 就像block item那样
public abstract class OperatorType {
    public abstract Operator createDefaultInstance();

    public static class Placeholder extends OperatorType{

        @Override
        public Operator createDefaultInstance() {
            throw new UnsupportedOperationException();
        }
    }
}
