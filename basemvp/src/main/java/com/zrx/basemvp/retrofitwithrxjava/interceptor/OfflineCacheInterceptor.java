package com.zrx.basemvp.retrofitwithrxjava.interceptor;

import android.content.Context;

import com.zrx.basemvp.utils.networks.NetWorkUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:这个回避网络拦截器先运行
 * 在没有网络连接的时候，会取缓存
 * 重点：一般okhttp只缓存不太改变的数据适合get
 * 这里和前面的不同，立即设置，立即生效。
 */
public class OfflineCacheInterceptor implements Interceptor {

    private static OfflineCacheInterceptor offlineCacheInterceptor;
    //离线的时候的缓存的过期时间
    private int offlineCacheTime;

    private Context context;

    private OfflineCacheInterceptor() {

    }

    public static OfflineCacheInterceptor getInstance() {
        if (offlineCacheInterceptor == null) {
            synchronized (OfflineCacheInterceptor.class) {
                if (offlineCacheInterceptor == null) {
                    offlineCacheInterceptor = new OfflineCacheInterceptor();
                }
            }
        }
        return offlineCacheInterceptor;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        if (context == null) {
            return chain.proceed(request);
        }
        if (!NetWorkUtils.isNetWorkConnected(context)) {
            if (offlineCacheTime != 0) {
                int temp = offlineCacheTime;
                request = request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale" + temp)
                        .build();
                offlineCacheTime = 0;
            } else {
                request = request.newBuilder()
                        .header("Cached-Control", "no-cache")
                        .build();
            }
        }
        return chain.proceed(request);
    }

    public void setOfflineCacheTime(int offlineCacheTime) {
        this.offlineCacheTime = offlineCacheTime;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
