package com.zrx.basemvp.retrofitwithrxjava.interceptor;

import android.text.TextUtils;

import com.zrx.basemvp.utils.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:在有网络的情况下
 * 如果还在网络有效期取缓存，否则进行网络请求
 * 重点：一般okhttp只缓存不太改变的数据适合get
 * 如果缓存时间设置了30秒，下次请求这个接口的30秒这内都会去取缓存，即使你设置0也不起效。因为缓存文件里的标识里已经有30秒的有效期
 */
public class NetCacheInterceptor implements Interceptor {

    private static NetCacheInterceptor cacheInterceptor;
    //在线时候的缓存过期时间，不想缓存时间可以设置为0
    private int onlineCacheTime;

    private final String USER_TOKEN = "USER_TOKEN";

    public static NetCacheInterceptor getInstance(){
        if (cacheInterceptor == null){
            synchronized (NetCacheInterceptor.class){
                if (cacheInterceptor == null){
                    cacheInterceptor = new NetCacheInterceptor();
                }
            }
        }
        return cacheInterceptor;
    }

    private NetCacheInterceptor(){

    }

    public void setOnlineCacheTime(int onlineCacheTime) {
        this.onlineCacheTime = onlineCacheTime;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        //这里做了自动解析头部和取值，之前一个项目要用头部的Token字段
        String token = (String) PreferenceUtil.getInstance().get(USER_TOKEN,"");
        if (!TextUtils.isEmpty(token)){
            builder.addHeader("Token",token).build();
        }
        request = builder.build();
        Response response = chain.proceed(request);
        List<String> list = response.headers().values("Token");
        if (list.size()>0){
            PreferenceUtil.getInstance().put(USER_TOKEN,list.get(0));
        }
        if (onlineCacheTime!=0){
            int temp = onlineCacheTime;
            Response response1 = response.newBuilder()
                    .header("Cache-Control", "public, max-age=" + temp)
                    .removeHeader("Pragma")
                    .build();
            onlineCacheTime = 0;
            return response1;
        }else {
            Response response1 = response.newBuilder()
                    .header("Cache-Control", "no-cache")
                    .removeHeader("Pragma")
                    .build();
            return response1;
        }
    }
}
