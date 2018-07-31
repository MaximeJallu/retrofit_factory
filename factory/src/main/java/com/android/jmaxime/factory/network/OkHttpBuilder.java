package com.android.jmaxime.factory.network;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public final class OkHttpBuilder {
    private static final int DEFAULT_TIMEOUT = 30;
    private static final int CACHE_SIZE = 2048;
    private static final int CACHE_DURATION = 30;
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Response originalResponse = chain.proceed(chain.request());
            String cacheControl = String.format(Locale.getDefault(), "max-age=%d, only-if-cached, max-stale=%d", CACHE_DURATION, 0);
            return originalResponse.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .build();
        }
    };

    private int mTimeOut = DEFAULT_TIMEOUT;
    private Cache mCache;
    private CookieJar mCookieJar;
    private Authenticator mAuthenticator;
    private List<Interceptor> mInterceptors = new ArrayList<>();
    private List<Interceptor> mNetworkInterceptors = new ArrayList<>();
    private OnClientCreatedListener mListener;

    public OkHttpBuilder() {
    }

    public OkHttpBuilder(OkHttpClient client) {
        this(client, null);
    }

    public OkHttpBuilder(OkHttpClient client, OnClientCreatedListener listener) {
        mTimeOut = client.connectTimeoutMillis();
        mCache = client.cache();
        mCookieJar = client.cookieJar();
        mAuthenticator = client.authenticator();
        mInterceptors.addAll(client.interceptors());
        mNetworkInterceptors.addAll(client.networkInterceptors());
        mListener = listener;
    }

    public OkHttpBuilder apiKey(String apiKey) {
        return apiKey("X-Api-Key", apiKey);
    }

    public OkHttpBuilder apiKey(final String headerKey, final String apiKey) {
        mInterceptors.add(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder request = chain.request().newBuilder();
                if (apiKey != null) {
                    request.header(headerKey, apiKey);
                }
                return chain.proceed(request.build());
            }
        });
        return this;
    }

    public OkHttpBuilder useCache(Context context) {
        return useCache(new File(context.getCacheDir().getAbsolutePath(), "HttpCache"), CACHE_SIZE * CACHE_SIZE, REWRITE_CACHE_CONTROL_INTERCEPTOR);
    }

    public OkHttpBuilder useCache(File cacheDirectory, int cacheSize, Interceptor cacheInterceptor) {
        mCache = new Cache(cacheDirectory, cacheSize);
        mNetworkInterceptors.add(cacheInterceptor);
        return this;
    }

    public OkHttpBuilder useCookie(CookieJar cookieJar) {
        mCookieJar = cookieJar;
        return this;
    }

    public OkHttpBuilder loggingLevel(boolean isDebug) {
        return loggingLevel(isDebug ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
    }

    public OkHttpBuilder loggingLevel(HttpLoggingInterceptor.Level level) {
        mInterceptors.add(new HttpLoggingInterceptor().setLevel(level));
        return this;
    }

    public void authenticator(Authenticator authenticator) {
        mAuthenticator = authenticator;
    }

    public void addInterceptor(Interceptor interceptor) {
        mInterceptors.add(interceptor);
    }

    public void addNetworkInterceptor(Interceptor interceptor) {
        mNetworkInterceptors.add(interceptor);
    }

    public void setOnClientCreatedListener(OnClientCreatedListener listener) {
        mListener = listener;
    }

    public OkHttpBuilder timeOut(int timeOut) {
        mTimeOut = timeOut > 0 ? timeOut : DEFAULT_TIMEOUT;
        return this;
    }

    public OkHttpClient.Builder originalBuilder() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(mTimeOut, TimeUnit.SECONDS);
        httpClient.connectTimeout(mTimeOut, TimeUnit.SECONDS);
        if (mCache != null) {
            httpClient.cache(mCache);
        }
        if (mCookieJar != null) {
            httpClient.cookieJar(mCookieJar);
        }
        if (mAuthenticator != null) {
            httpClient.authenticator(mAuthenticator);
        }
        for (Interceptor interceptor : mInterceptors) {
            httpClient.addInterceptor(interceptor);
        }
        for (Interceptor networkInterceptor : mNetworkInterceptors) {
            httpClient.addNetworkInterceptor(networkInterceptor);
        }
        return httpClient;
    }

    public OkHttpClient build() {
        final OkHttpClient client = originalBuilder().build();
        if (mListener != null) {
            mListener.onClientCreated(client);
        }
        return client;
    }

    public OkHttpClient build(Context context, String apiKey, int timeOutValue, boolean addCache, boolean allowCookies) {
        apiKey(apiKey);
        timeOut(timeOutValue);
        if (addCache) {
            useCache(context);
        }
        if (allowCookies) {
            useCookie(mCookieJar);
        }
        return originalBuilder().build();
    }

    public interface OnClientCreatedListener {
        void onClientCreated(OkHttpClient client);
    }
}
