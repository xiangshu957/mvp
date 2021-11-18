package com.zrx.basemvp.retrofitwithrxjava;

import android.os.Environment;
import android.text.TextUtils;

import com.zrx.basemvp.retrofitwithrxjava.interceptor.HttpLogInterceptor;
import com.zrx.basemvp.retrofitwithrxjava.interceptor.NetCacheInterceptor;
import com.zrx.basemvp.retrofitwithrxjava.interceptor.OfflineCacheInterceptor;
import com.zrx.basemvp.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:
 */
public class RetrofitManager {

    private static RetrofitManager retrofitManager;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private ArrayList<String> oneNetList;
    private static String baseUrl;
    private static List<String> imageUrlList;
    private static Class<?> retrofitApiServiceClass;
    private static RetrofitApiService retrofitApiService;

    private RetrofitManager() {
        oneNetList = new ArrayList<>();
        initOkHttpClient();
        initRetrofit();
    }

    /**
     * 第一次或者重置baseUrl时使用
     * 建议在application中初始化
     * @param baseUrl 不可空
     * @param list  不可空
     * @return
     */
    public static RetrofitManager getInstance(String baseUrl, List<String> list,Class<?> retrofitApiService) {
        if (retrofitManager == null) {
            synchronized (RetrofitManager.class) {
                if (retrofitManager == null) {
                    RetrofitManager.baseUrl = baseUrl;
                    RetrofitManager.imageUrlList = list;
                    RetrofitManager.retrofitApiServiceClass = retrofitApiService;
                    retrofitManager = new RetrofitManager();
                } else {
                    retrofitManager = null;
                    getInstance(baseUrl, list,retrofitApiService);
                }
            }
        }else {
            retrofitManager = null;
            getInstance(baseUrl, list,retrofitApiService);
        }
        return retrofitManager;
    }

    public static RetrofitManager getInstance(){
        if (TextUtils.isEmpty(baseUrl)){
            LogUtils.e(RetrofitManager.class.getSimpleName(),"baseUrl为空，无法初始化");
            return null;
        }
        if (retrofitManager == null){
            synchronized (RetrofitManager.class){
                if (retrofitManager == null){
                    retrofitManager = new RetrofitManager();
                }
            }
        }
        return retrofitManager;
    }

    private void initRetrofit() {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        retrofitApiService = (RetrofitApiService) retrofit.create(retrofitApiServiceClass);
    }

    private void initOkHttpClient() {
        okHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(new File(Environment.getExternalStorageDirectory() + "/okhttp_cache/"), 50 * 1024 * 1024))
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new HttpLogInterceptor(imageUrlList))
                //设置在线和离线缓存
                .addInterceptor(OfflineCacheInterceptor.getInstance())
                .addNetworkInterceptor(NetCacheInterceptor.getInstance())
                .build();
    }

    public  ArrayList<String> getOneNetList(){
        return oneNetList;
    }

    public RetrofitApiService getRetrofitApiService(){
        return retrofitApiService;
    }

}
