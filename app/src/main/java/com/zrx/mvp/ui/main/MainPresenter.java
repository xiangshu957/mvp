package com.zrx.mvp.ui.main;

import android.annotation.SuppressLint;

import com.zrx.basemvp.base.BasePresenter;
import com.zrx.mvp.api.RetrofitApi;

import javax.inject.Inject;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public class MainPresenter extends BasePresenter<MainContract.View> implements MainContract.Presenter {

    @Inject
    public MainPresenter() {

    }


    @SuppressLint("CheckResult")
    @Override
    public void getData() {
        setOfflineCacheTime(0);
        setOnlineCacheTime(0);
        observe(((RetrofitApi) apiService()).getData(), false)
                .subscribe(s -> {
                    getView().getDataSuccess(s);
                }, throwable -> {
                    getView().getDataFail(throwable.getMessage());
                });


    }
}
