package com.android.jmaxime.factory.network;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @see ApiKeyService
 * @see ClientHttpHelper
 * @see Helper for easily create your Retrofit Service
 */
public class ApiFactory {

    private static ApiFactory sFactory;

    private ClientHttpHelper mConfigurations;

    public static ApiFactory getInstance(ClientHttpHelper configurations) {
        if (sFactory == null) {
            sFactory = new ApiFactory(configurations);
        } else {
            sFactory.mConfigurations = configurations;
        }
        return sFactory;
    }

    private ApiFactory(ClientHttpHelper configurations) {
        mConfigurations = configurations;
    }

    public <T> T create(final Class<T> service) {
        checkViewType(service);
        ApiKeyService apiKeyService = service.getAnnotation(ApiKeyService.class);
        return new Helper(this, apiKeyService.baseUrlName())
                .setApiKeyService(apiKeyService)
                .create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName) {
        return new Helper(this, baseUrlName).create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName, String apiKeyName) {
        return new Helper(this, baseUrlName).setApiKeyName(apiKeyName).create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName, String apiKeyName, String tagApiName, int timeOutValue, boolean addCache, boolean allowCookies) {
        String baseUrl = baseUrlName.startsWith("http") ? baseUrlName : mConfigurations.getBaseUrl(baseUrlName);
        String apiKey = mConfigurations.getApiKey(apiKeyName);

        OkHttpClient client = mConfigurations.create(tagApiName, apiKey, timeOutValue, addCache, allowCookies);

        Converter.Factory converterFactory = mConfigurations.getConverterFactory(tagApiName);
        CallAdapter.Factory callAdapter = mConfigurations.getCallAdapter(tagApiName);
        return build(baseUrl, converterFactory, callAdapter, client).create(service);
    }

    private void checkViewType(Class<?> service) {
        if (!service.isAnnotationPresent(ApiKeyService.class)) {
            throw new IllegalArgumentException(service.getSimpleName()
                    + "is not annoted by " + ApiKeyService.class.getSimpleName());
        }
    }

    private Retrofit build(String baseUrl, Converter.Factory converterFactory, CallAdapter.Factory callAdapter, OkHttpClient client) {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(converterFactory)
                .client(client);
        if (callAdapter != null) {
            builder.addCallAdapterFactory(callAdapter);
        }
        return builder.build();
    }

    public final static class Helper {

        private final ApiFactory mFactory;
        private final String mBaseUrl;
        private String mApiKeyName;
        private String mTagClientHttp;
        private int mTimeOutValue = -1;
        private boolean mAddCache = false;
        private boolean mAllowCookies = false;


        /**
         * @param factory     factory
         * @param baseUrlName key of url or url valid started http...
         */
        public Helper(ApiFactory factory, String baseUrlName) {
            mFactory = factory;
            mBaseUrl = baseUrlName;
        }

        public Helper setApiKeyService(ApiKeyService service) {
            setTagClientHttp(service.tagApiName());
            setApiKeyName(service.apiKeyName());
            setTimeOut(service.timeOut());
            setEnableCache(service.addCache());
            setEnableCookies(service.allowCookies());
            return this;
        }

        /**
         * @param apiKeyName ex "customer" - "stores" - "one_id"
         * @return helper
         */
        public Helper setApiKeyName(String apiKeyName) {
            mApiKeyName = apiKeyName == null ? "" : apiKeyName;
            return this;
        }

        /**
         * @param clientHttpName if api key name is empty, this tag serves as a reference to know that it http client refers to
         * @return helper
         */
        public Helper setTagClientHttp(String clientHttpName) {
            mTagClientHttp = clientHttpName == null ? "" : clientHttpName;
            return this;
        }

        public Helper setTimeOut(int timeOut) {
            mTimeOutValue = timeOut < -1 ? -1 : timeOut;
            return this;
        }

        public Helper setEnableCache(boolean addCache) {
            mAddCache = addCache;
            return this;
        }

        public Helper setEnableCookies(boolean allowCookies) {
            mAllowCookies = allowCookies;
            return this;
        }

        public <T> T create(final Class<T> service) {
            if (!isValue(mTagClientHttp)) {
                mTagClientHttp = isValue(mApiKeyName) ? mApiKeyName : (mBaseUrl.contains("http")) ? service.getSimpleName() : mBaseUrl;
            }
            return mFactory.create(service, mBaseUrl, mApiKeyName, mTagClientHttp, mTimeOutValue, mAddCache, mAllowCookies);
        }

        private boolean isValue(String value) {
            return value != null && !value.isEmpty();
        }
    }
}
