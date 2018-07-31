# Status
![alt text](https://travis-ci.org/MaximeJallu/retrofit_factory.svg?branch=develop) [![API](https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=16)


# Generic-Factory for Retrofit:

This tool allows you to no longer worry about adapters. Now you will only create your ViewHolder.
Communication management between your Views and your ViewHolders is possible.
Creating sections is now very easily.
Enjoy.

# Download [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.maximejallu/factory/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.maximejallu/factory)
buildtool used is 27
use {exclude group: 'com.android.support'} only if you have problems
```groovy
dependencies {
    ...
    implementation ('com.github.maximejallu:factory:{version}')
    ...
}
```
# Init Factory
```java
class ProviderModule {

    @Provides
    Gson provideTestGson() {
        return new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Provides
    OkHttpClient providesClientDefault(Gson gson){
        return new OkHttpBuilder().loggingLevel(BuildConfig.DEBUG).build();
    }

    @Provides
    OkHttpConfiguration providesClientHttpHelper(OkHttpClient client, Gson gson) {
        return new OkHttpConfiguration.Builder(client, GsonConverterFactory.create(gson))
                .registerCallAdapter(RxAdapterFactory.create(), "proxy_v3")
                .addApiKey("apiKey1", "fail111c-dc6d-4f4c-8adc-2fg9a90z249z")
                .addUrlConfiguration("proxy_v3", "https://apiproxy-v3.preprod.marque.net/")
                .build();
    }

    @Provides
    ApiFactory providesApiFactory(OkHttpConfiguration configuration) {
        return new ApiFactory(configuration);
    }
}
```

# 1- Create yours Services Interface with @nnotation
```java
@ApiKeyService("apiKey1", "proxy_v3")
interface GeoDataService{
    @POST("...")
    Observable<Response> postObject(...);
}

@ApiKeyService("apiKey1", "http://base-url-service.net/api/")
interface GeoDataService2{
    @POST("...")
    Observable<Response> postObject(...);
}
```
# Or Create yours Services Interface without annotation
```java
interface GeoDataService3{
    @GET("...")
    Observable<Result> getObject(...);
}
```

# 2 - Provides Services
```java
    @Provides
    @Singleton
    GeoDataService provideGeoDataService(ApiFactory factory) {
    /*if GeoDataService with annotation*/
    return factory.create(GeoDataService.class);    
    /*else create manually*/
    return factory.create(GeoDataService.class, /*tag: http client*/ "proxy_v3", "apiKey1");
    }
```

[1]: https://github.com/JakeWharton/butterknife
[2]: https://github.com/square/picasso
[3]: https://github.com/bumptech/glide