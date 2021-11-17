package com.zrx.basemvp.base;

import com.trello.rxlifecycle2.LifecycleTransformer;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/14
 * DES:
 */
public interface BaseView {

    //展示加载框
    void showLoading(String msg);

    //隐藏加载框
    void hideLoading();

    LifecycleTransformer bindLifecycle();

}
