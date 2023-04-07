
package com.aiunion.aidesk.model;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by gupengcheng on 17/3/29.
 */

public class CharGPTApiClient {

    private static Retrofit sRetrofit;
    private static ApiService sApiService;

//    private static final String API_HOST_DEBUG = "http://172.16.2.12:8848/"; // 测试地址
//    private static final String API_HOST_RELEASE = "http://172.16.2.12:8848/"; // 正式地址

    // 服务器定义的一个TOKEN，服务端那边可以配置，如果改了，需要我更新TOKEN后重新打包
    //private static final String SERVER_GIVE_TOKEN = "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"; //old
    private static final String SERVER_GIVE_TOKEN = "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";

    private static void init() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // 添加统一的头信息
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .header("cache-control", "no-cache")
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Authorization", "Bearer " + SERVER_GIVE_TOKEN)
                        .build();
                return chain.proceed(request);
            }
        });

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(httpLoggingInterceptor);

        OkHttpClient okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        sRetrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private static String getBaseUrl() {
        return "https://api.openai.com/";
    }

    public static ApiService getApiService() {
        if (sApiService == null) {
            synchronized (CharGPTApiClient.class) {
                if (sApiService == null) {
                    init();
                    sApiService = sRetrofit.create(ApiService.class);
                }
            }
        }
        return sApiService;
    }

    public static void resetApiService() {
        sApiService = null;
        sRetrofit = null;
    }

}
