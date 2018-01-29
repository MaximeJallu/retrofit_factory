package com.android.jmaxime.factory.network;

import android.content.Context;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * use OkHttpFactory.Builder to set up your client
 *
 * @see OkHttpFactory.Builder
 */
public class OkHttpFactory implements IOkHttpClient {

    protected final Context mContext;
    protected final HttpLoggingInterceptor.Level mLevel;
    protected String mBearer;

    private Authenticator mAuthenticator = null;
    private Interceptor mNetworkInterceptor = null;
    private Interceptor mInterceptor = null;
    private Cache mCache = null;
    private IClientCreateCallback mCallback;

    private OkHttpFactory(Context context, HttpLoggingInterceptor.Level level) {
        mContext = context;
        mLevel = level;
    }

    public OkHttpClient create(OkHttpClient.Builder httpBuilder) {
        return httpBuilder.build();
    }

    @Override
    public final OkHttpClient create(String apiKey, int timeOutValue, boolean addCache, boolean allowCookies) {
        OkHttpBuilder builder = new OkHttpBuilder()
                .apiKey(apiKey)
                .authenticated(mBearer)
                .timeOut(timeOutValue)
                .loggingLevel(mLevel);

        if (addCache) {
            builder.useCache(mContext);
        }

        if (allowCookies) {
            builder.useCookie(mContext);
        }

        OkHttpClient.Builder buildBuilder = builder.buildBuilder();

        if (mInterceptor != null) {
            buildBuilder.addInterceptor(mInterceptor);
        }
        if (mAuthenticator != null) {
            buildBuilder.authenticator(mAuthenticator);
        }
        if (mNetworkInterceptor != null) {
            buildBuilder.addNetworkInterceptor(mNetworkInterceptor);
        }
        if (mCache != null) {
            buildBuilder.cache(mCache);
        }

        OkHttpClient client = create(buildBuilder);

        if (mCallback != null) {
            mCallback.onClientCreated(client);
        }
        return client;
    }

    public interface IClientCreateCallback {
        void onClientCreated(OkHttpClient client);
    }

    public static final class Builder {
        private OkHttpFactory mHttpFactory;

        public Builder(Context context, HttpLoggingInterceptor.Level level) {
            mHttpFactory = new OkHttpFactory(context, level);
        }

        public Builder addInterceptor(Interceptor interceptor) {
            mHttpFactory.mInterceptor = interceptor;
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            mHttpFactory.mNetworkInterceptor = interceptor;
            return this;
        }

        public Builder addAuthenticator(Authenticator authenticator) {
            mHttpFactory.mAuthenticator = authenticator;
            return this;
        }

        public Builder addCreatedCallback(IClientCreateCallback callback) {
            mHttpFactory.mCallback = callback;
            return this;
        }

        public Builder addCache(Cache cache) {
            mHttpFactory.mCache = cache;
            return this;
        }

        public OkHttpFactory build() {
            return mHttpFactory;
        }
    }
}
