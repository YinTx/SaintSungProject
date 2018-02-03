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

public class RetrofitConfig {
    //这个是处理网络请求的log信息的,可以实现Interceptor接口来自定义.
    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
        @Override
        public void log(String message) {
            Log.i("RxJava", message);
        }
    });

    OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();
    Retrofit retrofit = new Retrofit.Builder()
            .client(client)//Retrofit需要配置一个OkHttpClient实例.
            .baseUrl("")//需要指定一个baseUrl,一般就是服务器的域名
            .addConverterFactory(GsonConverterFactory.create())//这个是数据解析工厂,我用的是fastjson
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())//支持rxJava,在第二个jar包里面
            .build();
}
