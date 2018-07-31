package com.android.jmaxime.factory.network;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @see ApiKeyService
 * @see OkHttpConfiguration
 * @see Helper for easily create your Retrofit Service
 */
public class ApiFactory {
    private final OkHttpConfiguration mConfigurations;

    public ApiFactory(OkHttpConfiguration configurations) {
        mConfigurations = configurations;
    }

    public <T> T create(final Class<T> service) {
        hasApiKeyServiceAnnotation(service);
        ApiKeyService apiKeyService = service.getAnnotation(ApiKeyService.class);
        return new Helper(mConfigurations, apiKeyService.baseUrlName())
                .setApiKeyService(apiKeyService)
                .create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName) {
        return new Helper(mConfigurations, baseUrlName).create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName, String apiKeyName) {
        return new Helper(mConfigurations, baseUrlName).setApiKeyName(apiKeyName).create(service);
    }

    public <T> T create(final Class<T> service, String baseUrlName, String apiKeyName, String tagApiName, int timeOutValue, boolean addCache, boolean allowCookies) {
        return new Helper(mConfigurations, baseUrlName)
                .setApiKeyName(apiKeyName)
                .setTagClientHttp(tagApiName)
                .setTimeOut(timeOutValue)
                .setEnableCache(addCache)
                .setEnableCookies(allowCookies)
                .create(service);
    }

    private void hasApiKeyServiceAnnotation(Class<?> service) {
        if (!service.isAnnotationPresent(ApiKeyService.class)) {
            throw new IllegalArgumentException(service.getSimpleName()
                    + "is not annoted by " + ApiKeyService.class.getSimpleName());
        }
    }

    private final static class Helper {
        private final OkHttpConfiguration mConfigurations;
        private final String mBaseUrl;
        private String mApiKeyName;
        private String mTagClientHttp;
        private int mTimeOutValue = -1;
        private boolean mAddCache = false;
        private boolean mAllowCookies = false;

        /**
         * @param baseUrlName key of url or url valid started http...
         */
        Helper(OkHttpConfiguration configurations, String baseUrlName) {
            mConfigurations = configurations;
            mBaseUrl = baseUrlName;
        }

        Helper setApiKeyService(ApiKeyService service) {
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
        Helper setApiKeyName(String apiKeyName) {
            mApiKeyName = apiKeyName == null ? "" : apiKeyName;
            return this;
        }

        /**
         * @param clientHttpName if api key name is empty, this tag serves as a reference to know that it http client refers to
         * @return helper
         */
        Helper setTagClientHttp(String clientHttpName) {
            mTagClientHttp = clientHttpName == null ? "" : clientHttpName;
            return this;
        }

        Helper setTimeOut(int timeOut) {
            mTimeOutValue = timeOut < -1 ? -1 : timeOut;
            return this;
        }

        Helper setEnableCache(boolean addCache) {
            mAddCache = addCache;
            return this;
        }

        Helper setEnableCookies(boolean allowCookies) {
            mAllowCookies = allowCookies;
            return this;
        }

        private <T> T create(final Class<T> service) {
            if (isNullOrEmptyValue(mTagClientHttp)) {
                mTagClientHttp = isValueNotEmpty(mApiKeyName) ? mApiKeyName : (mBaseUrl.contains("http")) ? service.getSimpleName() : mBaseUrl;
            }
            String baseUrl = mBaseUrl.startsWith("http") ? mBaseUrl : mConfigurations.getBaseUrl(mBaseUrl);
            String apiKey = mConfigurations.getApiKey(mApiKeyName);
            OkHttpClient client = mConfigurations.create(mTagClientHttp, apiKey, mTimeOutValue, mAddCache, mAllowCookies);
            Converter.Factory converterFactory = mConfigurations.getConverterFactory(mTagClientHttp);
            CallAdapter.Factory callAdapter = mConfigurations.getCallAdapter(mTagClientHttp);
            return build(baseUrl, converterFactory, callAdapter, client).create(service);
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

        private boolean isValueNotEmpty(String value) {
            return value != null && !value.isEmpty();
        }

        private boolean isNullOrEmptyValue(String value){
            return !isValueNotEmpty(value);
        }

    }
}
