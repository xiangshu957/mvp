package com.zrx.mvp.application;

import android.content.Context;

import com.zrx.basemvp.retrofitwithrxjava.RetrofitManager;
import com.zrx.mvp.api.RetrofitApi;
import com.zrx.mvp.common.SysCommon;
import com.zrx.mvp.daggerforandroid.DaggerAppComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public class MyApplication extends DaggerApplication {

    private static MyApplication context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

        RetrofitManager.getInstance(SysCommon.BASE_URL,null, RetrofitApi.class);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().application(this).build();
    }

    public static Context getContext(){
        return context;
    }
}
