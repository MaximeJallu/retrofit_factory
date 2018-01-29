package com.android.jmaxime.factory.network;

public abstract class ConverterStrategy<IN> implements IConverterStrategy<IN> {
    private final IN mData;

    protected ConverterStrategy(IN dataSource) {
        mData = dataSource;
    }

    final NetworkMapValue execute(){
        return convert(mData);
    }
}
