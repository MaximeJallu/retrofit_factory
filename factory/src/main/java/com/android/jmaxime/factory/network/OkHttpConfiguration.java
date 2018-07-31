package com.android.jmaxime.factory.network;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @see OkHttpConfiguration.Builder
 */
public final class OkHttpConfiguration {
    private final Map<String, String> mBaseUrls;
    private final Map<String, String> mMapApiKey;
    private final HttpClientFlyweight mFactory;

    private OkHttpConfiguration(Map<String, String> baseUrls, Map<String, String> mapApiKey, HttpClientFlyweight factory) {
        mBaseUrls = baseUrls;
        mMapApiKey = mapApiKey;
        mFactory = factory;
    }

    String getBaseUrl(String targetUrl) {
        return mBaseUrls.get(targetUrl);
    }

    String getApiKey(String apiName) {
        return mMapApiKey.get(apiName);
    }

    OkHttpClient create(final String apiName, final String apiKey, final int timeOutValue, final boolean addCache, final boolean allowCookies) {
        return mFactory.getOkHttpClient(apiName).build(null, apiKey, timeOutValue, addCache, allowCookies);
    }

    Converter.Factory getConverterFactory(String tagApiName) {
        return mFactory.getConverterFactory(tagApiName);
    }

    CallAdapter.Factory getCallAdapter(String tagApiName) {
        return mFactory.getCallAdapter(tagApiName);
    }

    /**
     * Configure the HttpClientFlyweight (for create to retrofit client)
     * By default OkHttpClient will be confirurer with these values if are not override:
     * <p>
     * Converter: GsonConverterFactory.create ()<br/>
     * CallAdapter: RxJavaCallAdapterFactory.create ()) <br/>
     * </p>
     */
    public static final class Builder {
        private final HttpClientFlyweight mFactory = new HttpClientFlyweight();
        private final Map<String, String> mBaseUrls = new HashMap<>();
        private final Map<String, String> mMapApiKey = new HashMap<>();

        public Builder(OkHttpClient client, Converter.Factory defaultConverter) {
            this(new OkHttpBuilder(client), defaultConverter);
        }

        public Builder(OkHttpBuilder client, Converter.Factory defaultConverter) {
            mFactory.registerClient(client, HttpClientFlyweight.DEFAULT_KEY);
            mFactory.registerDefaultFactories(defaultConverter, RxJavaCallAdapterFactory.create());
        }

        public Builder registerApiKey(ConfigurationMapper mapper) {
            for (Map.Entry<String, String> entry : mapper.map().entrySet()) {
                mMapApiKey.put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder registerBaseUrl(ConfigurationMapper mapper) {
            for (Map.Entry<String, String> entry : mapper.map().entrySet()) {
                mBaseUrls.put(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public Builder addApiKey(String name, String value) {
            mMapApiKey.put(name, value);
            return this;
        }

        public Builder addUrlConfiguration(String name, String value) {
            mBaseUrls.put(name, value);
            return this;
        }

        public Builder registerDefaultCallAdapter(CallAdapter.Factory callAdapter) {
            mFactory.registerDefaultFactories(mFactory.getConverterFactory(HttpClientFlyweight.DEFAULT_KEY), callAdapter);
            return this;
        }

        public Builder registerConverter(Gson gsonConverter, String tagApiName) {
            mFactory.registerConverter(gsonConverter, tagApiName);
            return this;
        }

        public Builder registerConverter(Converter.Factory converter, String tagApiName) {
            mFactory.registerConverter(converter, tagApiName);
            return this;
        }

        public Builder registerCallAdapter(CallAdapter.Factory callAdapter, String tagApiName) {
            mFactory.registerCallAdapter(callAdapter, tagApiName);
            return this;
        }

        public Builder register(Gson gsonConverter, CallAdapter.Factory callAdapter, OkHttpClient client, String tagApiName) {
            mFactory.registerFactories(gsonConverter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Gson gsonConverter, CallAdapter.Factory callAdapter, OkHttpBuilder client, String tagApiName) {
            mFactory.registerFactories(gsonConverter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Converter.Factory converter, CallAdapter.Factory callAdapter, OkHttpClient client, String tagApiName) {
            mFactory.registerFactories(converter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Converter.Factory converter, CallAdapter.Factory callAdapter, OkHttpBuilder client, String tagApiName) {
            mFactory.registerFactories(converter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder registerClient(OkHttpClient client, String tagApiName) {
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder registerClient(OkHttpBuilder client, String tagApiName) {
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public OkHttpConfiguration build() {
            if (!mFactory.mClients.containsKey(HttpClientFlyweight.DEFAULT_KEY)) {
                throw new IllegalArgumentException(String.format("default client factory must not be null, please use %s#registerDefault(IOkHttpClient client)", this.getClass().getCanonicalName()));
            }
            return new OkHttpConfiguration(mBaseUrls, mMapApiKey, mFactory);
        }

    }

    private static class HttpClientFlyweight {
        private static final String DEFAULT_KEY = "DEFAULT_KEY";
        private Map<String, Converter.Factory> mConverters = new HashMap<>();
        private Map<String, CallAdapter.Factory> mCallAdapters = new HashMap<>();
        private Map<String, OkHttpBuilder> mClients = new HashMap<>();

        /**
         * if you prefer to register a custom client. It will be priority on a factory with the same #tagApiName
         *
         * @param client     OkHttpClient
         * @param tagApiName name register
         */
        void registerClient(OkHttpClient client, String tagApiName) {
            registerClient(new OkHttpBuilder(client), tagApiName);
        }

        /**
         * Preferably use OkHttpFactory.Builder to create your http client to register
         *
         * @param client     OkHttpClient Factory
         * @param tagApiName name register
         */
        void registerClient(OkHttpBuilder client, String tagApiName) {
            mClients.put(tagApiName, client);
        }

        void registerDefaultFactories(Gson gsonConverter, CallAdapter.Factory callAdapter) {
            registerDefaultFactories(GsonConverterFactory.create(gsonConverter), callAdapter);
        }

        void registerDefaultFactories(Converter.Factory converter, CallAdapter.Factory callAdapter) {
            registerFactories(converter, callAdapter, DEFAULT_KEY);
        }

        void registerFactories(Gson gsonConverter, CallAdapter.Factory callAdapter, String tagApiName) {
            registerFactories(GsonConverterFactory.create(gsonConverter), callAdapter, tagApiName);
        }

        void registerFactories(Converter.Factory converter, CallAdapter.Factory callAdapter, String tagApiName) {
            mConverters.put(tagApiName, converter);
            mCallAdapters.put(tagApiName, callAdapter);
        }

        void registerCallAdapter(CallAdapter.Factory callAdapter, String tagApiName) {
            mCallAdapters.put(tagApiName, callAdapter);
        }

        void registerConverter(Gson gsonConverter, String tagApiName) {
            mConverters.put(tagApiName, GsonConverterFactory.create(gsonConverter));
        }

        void registerConverter(Converter.Factory converter, String tagApiName) {
            mConverters.put(tagApiName, converter);
        }

        /**
         * @param apiName name of api, is a Tag Service or ApiKeyName
         * @return HttpClient
         * @see ApiKeyService#tagApiName()
         * @see ApiKeyService#apiKeyName()
         */
        final OkHttpBuilder getOkHttpClient(final String apiName) {
            if (mClients.containsKey(apiName)) {
                return mClients.get(apiName);
            }
            return mClients.get(DEFAULT_KEY);
        }

        /**
         * Ex : RxJavaCallAdapterFactory.create()
         *
         * @return Factory
         */
        CallAdapter.Factory getCallAdapter(String apiName) {
            if (mCallAdapters.containsKey(apiName)) {
                return mCallAdapters.get(apiName);
            }
            return mCallAdapters.get(DEFAULT_KEY);
        }

        /**
         * Converter factory for serialization and deserialization of objects.
         */
        Converter.Factory getConverterFactory(String apiName) {
            if (mConverters.containsKey(apiName)) {
                return mConverters.get(apiName);
            }
            return mConverters.get(DEFAULT_KEY);
        }
    }
}
