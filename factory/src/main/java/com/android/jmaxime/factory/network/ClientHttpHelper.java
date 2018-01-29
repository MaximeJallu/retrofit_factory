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
 * @see ClientHttpHelper.Builder
 */
public final class ClientHttpHelper {

    private final Map<String, String> mBaseUrls;
    private final Map<String, String> mMapApiKey;
    private final FlyweightHttpClientFactory mFactory;

    private ClientHttpHelper(Map<String, String> baseUrls, Map<String, String> mapApiKey, FlyweightHttpClientFactory factory) {
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
        return mFactory.create(apiName, apiKey, timeOutValue, addCache, allowCookies);
    }

    Converter.Factory getConverterFactory(String tagApiName) {
        return mFactory.getConverterFactory(tagApiName);
    }

    CallAdapter.Factory getCallAdapter(String tagApiName) {
        return mFactory.getCallAdapter(tagApiName);
    }

    /**
     * Configure the FlyweightHttpClientFactory (for create to retrofit client)
     * By default OkHttpClient will be confirurer with these values if are not override:
     * <p>
     * Converter: GsonConverterFactory.create ()<br/>
     * CallAdapter: RxJavaCallAdapterFactory.create ()) <br/>
     * </p>
     */
    public static final class Builder {

        private final Map<String, String> mBaseUrls;
        private final Map<String, String> mMapApiKey;
        private ConverterStrategy mApiStrategy;
        private ConverterStrategy mBaseUrlStrategy;
        private final FlyweightHttpClientFactory mFactory;

        public Builder(IOkHttpClient client, Converter.Factory defaultConverter) {
            mFactory = new FlyweightHttpClientFactory();
            mFactory.registerClient(client, FlyweightHttpClientFactory.DEFAULT_KEY);
            mBaseUrls = new HashMap<>();
            mMapApiKey = new HashMap<>();
            mFactory.registerDefaultFactory(defaultConverter, RxJavaCallAdapterFactory.create());
        }

        public Builder(OkHttpClient client, Converter.Factory defaultConverter) {
            mFactory = new FlyweightHttpClientFactory();
            mFactory.registerClient(client, FlyweightHttpClientFactory.DEFAULT_KEY);
            mBaseUrls = new HashMap<>();
            mMapApiKey = new HashMap<>();
            mFactory.registerDefaultFactory(defaultConverter, RxJavaCallAdapterFactory.create());
        }

        public Builder registerApiKeyConverter(ConverterStrategy strategy) {
            mApiStrategy = strategy;
            return this;
        }

        public Builder registerBaseUrlConverter(ConverterStrategy strategy) {
            mBaseUrlStrategy = strategy;
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
            mFactory.registerDefaultFactory(mFactory.getConverterFactory(FlyweightHttpClientFactory.DEFAULT_KEY), callAdapter);
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
            mFactory.registerFactory(gsonConverter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Gson gsonConverter, CallAdapter.Factory callAdapter, IOkHttpClient client, String tagApiName) {
            mFactory.registerFactory(gsonConverter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Converter.Factory converter, CallAdapter.Factory callAdapter, OkHttpClient client, String tagApiName) {
            mFactory.registerFactory(converter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder register(Converter.Factory converter, CallAdapter.Factory callAdapter, IOkHttpClient client, String tagApiName) {
            mFactory.registerFactory(converter, callAdapter, tagApiName);
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder registerClient(OkHttpClient client, String tagApiName) {
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public Builder registerClient(IOkHttpClient client, String tagApiName) {
            mFactory.registerClient(client, tagApiName);
            return this;
        }

        public ClientHttpHelper build() {
            if (mFactory.mClients.containsKey(FlyweightHttpClientFactory.DEFAULT_KEY) ||
                    mFactory.mClientsFactory.containsKey(FlyweightHttpClientFactory.DEFAULT_KEY)) {
                if (mApiStrategy != null) {
                    for (Map.Entry<String, String> entry : mApiStrategy.execute().getMap().entrySet()) {
                        mMapApiKey.put(entry.getKey(), entry.getValue());
                    }
                }
                if (mBaseUrlStrategy != null) {
                    for (Map.Entry<String, String> entry : mBaseUrlStrategy.execute().getMap().entrySet()) {
                        mBaseUrls.put(entry.getKey(), entry.getValue());
                    }
                }

                return new ClientHttpHelper(mBaseUrls, mMapApiKey, mFactory);
            } else {
                throw new IllegalArgumentException(String.format("default client factory must not be null, please use %s#registerDefault(IOkHttpClient client)",
                        this.getClass().getCanonicalName()));
            }
        }

    }

    public static class FlyweightHttpClientFactory {

        private static final String DEFAULT_KEY = "DEFAULT_KEY";

        Map<String, Converter.Factory> mConverters = new HashMap<>();
        Map<String, CallAdapter.Factory> mCallAdapters = new HashMap<>();
        Map<String, IOkHttpClient> mClientsFactory = new HashMap<>();
        Map<String, OkHttpClient> mClients = new HashMap<>();

        /**
         * if you prefer to register a custom client. It will be priority on a factory with the same #tagApiName
         *
         * @param client     OkHttpClient
         * @param tagApiName name register
         */
        public void registerClient(OkHttpClient client, String tagApiName) {
            mClients.put(tagApiName, client);
        }

        /**
         * Preferably use OkHttpFactory.Builder to create your http client to register
         *
         * @param client     OkHttpClient Factory
         * @param tagApiName name register
         */
        public void registerClient(IOkHttpClient client, String tagApiName) {
            mClientsFactory.put(tagApiName, client);
        }

        public void registerDefaultFactory(Gson gsonConverter, CallAdapter.Factory callAdapter) {
            registerDefaultFactory(GsonConverterFactory.create(gsonConverter), callAdapter);
        }

        public void registerDefaultFactory(Converter.Factory converter, CallAdapter.Factory callAdapter) {
            registerFactory(converter, callAdapter, DEFAULT_KEY);
        }

        public void registerFactory(Gson gsonConverter, CallAdapter.Factory callAdapter, String tagApiName) {
            registerFactory(GsonConverterFactory.create(gsonConverter), callAdapter, tagApiName);
        }

        public void registerFactory(Converter.Factory converter, CallAdapter.Factory callAdapter, String tagApiName) {
            mConverters.put(tagApiName, converter);
            mCallAdapters.put(tagApiName, callAdapter);
        }

        public void registerCallAdapter(CallAdapter.Factory callAdapter, String tagApiName) {
            mCallAdapters.put(tagApiName, callAdapter);
        }

        public void registerConverter(Gson gsonConverter, String tagApiName) {
            mConverters.put(tagApiName, GsonConverterFactory.create(gsonConverter));
        }

        public void registerConverter(Converter.Factory converter, String tagApiName) {
            mConverters.put(tagApiName, converter);
        }

        /**
         * @param apiName      name of api, is a Tag Service or ApiKeyName
         * @param apiKey       apiKeyName
         * @param addCache     boolean
         * @param allowCookies boolean
         * @return HttpClient
         * @see ApiKeyService#tagApiName()
         * @see ApiKeyService#apiKeyName()
         */
        public final OkHttpClient create(final String apiName, final String apiKey, final int timeOutValue, final boolean addCache, final boolean allowCookies) {
            OkHttpClient client = mClients.get(apiName);
            if (client == null) {
                if (mClientsFactory.containsKey(apiName)) {
                    client = mClientsFactory.get(apiName).create(apiKey, timeOutValue, addCache, allowCookies);
                } else if (mClients.containsKey(DEFAULT_KEY)) {
                    client = mClients.get(DEFAULT_KEY);
                } else {
                    client = mClientsFactory.get(DEFAULT_KEY).create(apiKey, timeOutValue, addCache, allowCookies);
                }
                mClients.put(apiName, client);
            }
            return client;
        }

        /**
         * Ex : RxJavaCallAdapterFactory.create()
         *
         * @return Factory
         */
        public CallAdapter.Factory getCallAdapter(String apiName) {
            if (mCallAdapters.containsKey(apiName)) {
                return mCallAdapters.get(apiName);
            } else {
                return mCallAdapters.get(DEFAULT_KEY);
            }
        }

        /**
         * Converter factory for serialization and deserialization of objects.
         *
         * @return
         * @See Retrofit
         */
        public Converter.Factory getConverterFactory(String apiName) {
            if (mConverters.containsKey(apiName)) {
                return mConverters.get(apiName);
            } else {
                return mConverters.get(DEFAULT_KEY);
            }
        }
    }
}
