
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

public class ApiClient {

    private static Retrofit sRetrofit;
    private static ApiService sApiService;

//    private static final String API_HOST_DEBUG = "http://172.16.2.12:8848/"; // 测试地址
//    private static final String API_HOST_RELEASE = "http://172.16.2.12:8848/"; // 正式地址

    // 服务器定义的一个TOKEN，服务端那边可以配置，如果改了，需要我更新TOKEN后重新打包
    private static final String SERVER_GIVE_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiZmFjZWFpIl0sInNjb3BlIjpbImFwaS1zZXJ2aWNlIl0sImV4cCI6MTkyMTE1MzI1OCwiYXV0aG9yaXRpZXMiOlsiYWl1bmlvbiJdLCJqdGkiOiI3ODI3YTBkYi0zMGQ3LTRhODItYjQyYy0yMTQ0NTMyZWRlNDEiLCJjbGllbnRfaWQiOiJhcGktY2xpZW50In0.mE8WnaGzVuWhS5LfT0ajQcBr_JP2TUOVfhch-5dJ6mA";

    private static void init(String baseUrl) {
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
                .baseUrl(getBaseUrl(baseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
    }

    private static String getBaseUrl(String baseUrl) {
        return "http://" + baseUrl + "/";
    }

    public static ApiService getApiService(String baseUrl) {
        if (sApiService == null) {
            synchronized (ApiClient.class) {
                if (sApiService == null) {
                    init(baseUrl);
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
