package com.zrx.mvp.daggerforandroid;

import android.app.Application;

import com.zrx.mvp.application.MyApplication;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
@Component(modules = {AndroidSupportInjectionModule.class})
public interface AppComponent extends AndroidInjector<MyApplication> {

    @Component.Builder
    interface Builder{
        @BindsInstance
        Builder application(Application application);
        AppComponent build();
    }

}
