package com.zrx.basemvp.base;

import com.zrx.basemvp.retrofitwithrxjava.RetrofitApiService;
import com.zrx.basemvp.retrofitwithrxjava.RetrofitManager;
import com.zrx.basemvp.utils.LogUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/14
 * DES:
 */
public class BasePresenter<V extends BaseView> {

    /**
     * 为了退出页面，取消请求
     */
    private CompositeDisposable compositeDisposable;

    /**
     * 绑定的view
     */
    private V mvpView;

    /**
     * 绑定view，一般在初始化中调用该方法
     *
     * @param mvpView
     */
    public void attachView(V mvpView) {
        this.mvpView = mvpView;
        compositeDisposable = new CompositeDisposable();
    }

    /**
     * 解绑view，一般在onDestroy中调用
     */
    public void detachView() {
        this.mvpView = null;
        //退出页面的时候移除网络请求
        removeDisposable();
    }

    private void removeDisposable() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    /**
     * 是否绑定view
     *
     * @return
     */
    public boolean isViewAttached() {
        return mvpView != null;
    }

    /**
     * 获取绑定的view
     *
     * @return
     */
    public V getView() {
        return mvpView;
    }

    public RetrofitApiService apiService() {
        return RetrofitManager.getInstance().getRetrofitApiService();
    }

    public <T> Observable<T> observe(Observable<T> observable) {
        return observe(observable, true, "");
    }

    public <T> Observable<T> observe(Observable<T> observable, boolean showDialog) {
        return observe(observable, showDialog, "");
    }

    public <T> Observable<T> observe(Observable<T> observable, final boolean showDialog, final String messafe) {
        return observable.subscribeOn(Schedulers.io())
                .doOnSubscribe(disposable -> {
                    if (showDialog) {
                        mvpView.showLoading(messafe);
                    }
                }).doFinally(() -> {
                    if (showDialog) {
                        mvpView.hideLoading();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .compose(mvpView.bindLifecycle());

    }

    public <T> Observable<T> observeWithRetry(Observable<T> observable,boolean showDialog,String message, int retryMaxCount){
        final int maxCount = retryMaxCount;
        final int[] currentCount = {0};
        return observable.subscribeOn(Schedulers.io())
                .retryWhen(throwableObservable -> {
                    //如果还没到次数，就延迟5秒发起重连
                    LogUtils.i("重连","当前重连的次数 == "+ currentCount[0]);
                    if (currentCount[0] <= maxCount){
                        currentCount[0]++;
                        return Observable.just(1).delay(5000, TimeUnit.MILLISECONDS);
                    }else {
                        return Observable.error(new Throwable("重连次数已达最高，请求超时"));
                    }
                });
    }

}
