package com.android.jmaxime.retrofit_factory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.android.jmaxime.factory.network.ApiFactory;
import com.android.jmaxime.factory.network.OkHttpBuilder;
import com.android.jmaxime.factory.network.OkHttpConfiguration;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
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
                .registerCallAdapter(RxAdapterFactory.create(), "customer")
                .addApiKey("customer", "fail111c-dc6d-4f4c-8adc-2fg9a90z249z")
                .addUrlConfiguration("proxy_v3", "https://apiproxy-v3.preprod.marque.net/")
                .build();
    }

    @Provides
    ApiFactory providesApiFactory(OkHttpConfiguration configuration) {
        return new ApiFactory(configuration);
    }
    */
}
