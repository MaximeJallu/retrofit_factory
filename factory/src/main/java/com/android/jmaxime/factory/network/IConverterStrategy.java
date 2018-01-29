package com.android.jmaxime.factory.network;

public interface IConverterStrategy<IN> {
    NetworkMapValue convert(IN in);
}
