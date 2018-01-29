package com.android.jmaxime.factory.network;

import okhttp3.OkHttpClient;

public interface IOkHttpClient {
    OkHttpClient create(final String apiKey, final int timeOutValue, final boolean addCache, final boolean allowCookies);
}
