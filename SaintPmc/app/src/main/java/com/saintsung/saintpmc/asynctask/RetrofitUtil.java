package com.saintsung.saintpmc.asynctask;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by EvanShu on 2018/1/30.
 */

public class RetrofitUtil {
    /**
     * 服务器地址
     */
    private static final String API_HOST ="你的BaseUrl";
    private RetrofitUtil() {

    }
    public static Retrofit getRetrofit() {
        return Instanace.retrofit;
    }
    //静态内部类,保证单例并在调用getRetrofit方法的时候才去创建.
    private static class Instanace {
        private static final Retrofit retrofit = getInstanace();
        private static Retrofit getInstanace() {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger(){
                @Override
                public void log(String message) {
                    Log.i("RxJava", message);
                }
            });
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .build();
            Retrofit retrofit= new Retrofit.Builder()
                    .client(client)
                    .baseUrl(API_HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit;
        }
    }
}
