package com.android.jmaxime.factory.network;

import android.content.Context;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public final class OkHttpBuilder {
    //region Constants *****************************************************************************

    public static final int DEFAULT_TIMEOUT = 30;
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

    //endregion


    //region Fields ********************************************************************************

    private int mTimeOut = DEFAULT_TIMEOUT;
    private int mCacheSize;
    private boolean mHasHeader;
    private boolean mUseCache;
    private String mToken;
    private String mHeaderKey;
    private String mApiKey;
    private File mCacheDirectory;
    private Interceptor mCacheInterceptor;
    private ClearableCookieJar mCookieJar;
    private HttpLoggingInterceptor.Level mLevel;

    //endregion



    //region Public Methods ************************************************************************

    public OkHttpBuilder() {
    }

    public OkHttpBuilder authenticated(String token) {
        mToken = token;
        mHasHeader = true;
        return this;
    }

    public OkHttpBuilder apiKey(String apiKey) {
        return apiKey("X-Api-Key", apiKey);
    }

    public OkHttpBuilder apiKey(String headerKey, String apiKey) {
        mHeaderKey = headerKey;
        mApiKey = apiKey;
        mHasHeader = true;
        return this;
    }

    public OkHttpBuilder useCache(Context context) {
        return useCache(new File(context.getCacheDir().getAbsolutePath(), "HttpCache"), CACHE_SIZE * CACHE_SIZE, REWRITE_CACHE_CONTROL_INTERCEPTOR);
    }

    public OkHttpBuilder useCache(File cacheDirectory, int cacheSize, Interceptor cacheInterceptor) {
        mCacheDirectory = cacheDirectory;
        mCacheSize = cacheSize;
        mCacheInterceptor = cacheInterceptor;
        mUseCache = true;
        return this;
    }

    public OkHttpBuilder useCookie(Context context) {
        mCookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
        return this;
    }

    public OkHttpBuilder loggingLevel() {
        return loggingLevel(HttpLoggingInterceptor.Level.NONE);
    }

    public OkHttpBuilder loggingLevel(HttpLoggingInterceptor.Level level) {
        mLevel = level;
        return this;
    }

    public OkHttpBuilder timeOut(int timeOut) {
        mTimeOut = timeOut > 0 ? timeOut : DEFAULT_TIMEOUT;
        return this;
    }

    public OkHttpClient.Builder buildBuilder() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(mTimeOut, TimeUnit.SECONDS);
        httpClient.connectTimeout(mTimeOut, TimeUnit.SECONDS);
        if (mUseCache) {
            httpClient.addNetworkInterceptor(mCacheInterceptor);
            httpClient.cache(new Cache(mCacheDirectory, mCacheSize));
        }
        if (mCookieJar != null) {
            httpClient.cookieJar(mCookieJar);
        }
        if (mLevel != null) {
            httpClient.addInterceptor(new HttpLoggingInterceptor().setLevel(mLevel));
        }
        if (mHasHeader) {
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder request = original.newBuilder();
                    if (mToken != null) {
                        request.header("Authorization", mToken);
                    }
                    if (mApiKey != null) {
                        request.header(mHeaderKey, mApiKey);
                    }
                    return chain.proceed(request.build());
                }
            });
        }
        return httpClient;
    }

    public OkHttpClient build() {
        return buildBuilder().build();
    }

    //endregion
}
