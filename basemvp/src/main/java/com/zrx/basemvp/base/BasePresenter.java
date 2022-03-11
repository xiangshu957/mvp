package com.zrx.basemvp.base;

import android.content.Context;

import com.zrx.basemvp.retrofitwithrxjava.RetrofitApiService;
import com.zrx.basemvp.retrofitwithrxjava.RetrofitManager;
import com.zrx.basemvp.retrofitwithrxjava.downloadutils.FileDownloadObserver;
import com.zrx.basemvp.retrofitwithrxjava.interceptor.NetCacheInterceptor;
import com.zrx.basemvp.retrofitwithrxjava.interceptor.OfflineCacheInterceptor;
import com.zrx.basemvp.retrofitwithrxjava.uploadutils.FileUploadObserver;
import com.zrx.basemvp.retrofitwithrxjava.uploadutils.UploadFileRequestBody;
import com.zrx.basemvp.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

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

    public <T> Observable<T> observeWithRetry(Observable<T> observable, boolean showDialog, String message, int retryMaxCount) {
        final int maxCount = retryMaxCount;
        final int[] currentCount = {0};
        return observable.subscribeOn(Schedulers.io())
                .retryWhen(throwableObservable -> {
                    //如果还没到次数，就延迟5秒发起重连
                    LogUtils.i("重连", "当前重连的次数 == " + currentCount[0]);
                    if (currentCount[0] <= maxCount) {
                        currentCount[0]++;
                        return Observable.just(1).delay(5000, TimeUnit.MILLISECONDS);
                    } else {
                        return Observable.error(new Throwable("重连次数已达最高，请求超时"));
                    }
                })
                .doOnSubscribe(disposable -> {
                    if (showDialog) {
                        mvpView.showLoading(message);
                    }
                }).doFinally(() -> {
                    if (showDialog) {
                        mvpView.hideLoading();
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .compose(mvpView.bindLifecycle());
    }

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    //设置在线网络缓存有效时间
    public void setOnlineCacheTime(int time) {
        NetCacheInterceptor.getInstance().setOnlineCacheTime(time);
    }

    //设置离线网络缓存有效时间
    public void setOfflineCacheTime(int time) {
        OfflineCacheInterceptor.getInstance().setOfflineCacheTime(time);
    }

    public void setContext(Context context){
        OfflineCacheInterceptor.getInstance().setContext(context);
    }

    public void addOnNet(String tag) {
        if (RetrofitManager.getInstance().getOneNetList().contains(tag)) {
            return;
        } else {
            RetrofitManager.getInstance().getOneNetList().remove(tag);
        }
    }

    public void removeTag(String tag) {
        RetrofitManager.getInstance().getOneNetList().remove(tag);
    }

    //这里是监听图片下载进度的
    //单张图片
    public void uploadWithListener(String url, RequestBody requestBody, File file, FileUploadObserver fileUploadObserver) {
        UploadFileRequestBody uploadFileRequestBody = new UploadFileRequestBody(file, fileUploadObserver);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), uploadFileRequestBody);
        observe(apiService().uploadFile(url, requestBody, body)).subscribe(fileUploadObserver);
    }

    //这里是多张图片，不同key，不同图片
    public void uploadWithListener(String url, RequestBody requestBody, Map<String, File> fileMap, FileUploadObserver fileUploadObserver) {
        UploadFileRequestBody uploadFileRequestBody = new UploadFileRequestBody(fileMap, fileUploadObserver);
        MultipartBody.Part body = MultipartBody.Part.create(uploadFileRequestBody);
        observe(apiService().uploadFile(url, requestBody, body)).subscribe(fileUploadObserver);
    }

    //这里是多张图片，同一个key，不同图片
    public void uploadWithListener(String url, RequestBody requestBody, ArrayList<File> files, String key, FileUploadObserver fileUploadObserver) {
        UploadFileRequestBody uploadFileRequestBody = new UploadFileRequestBody(key, files, fileUploadObserver);
        MultipartBody.Part body = MultipartBody.Part.create(uploadFileRequestBody);
        observe(apiService().uploadFile(url, requestBody, body)).subscribe(fileUploadObserver);
    }

    /**
     * 下载文件
     *
     * @param url
     * @param desDir
     * @param fileName
     * @param fIleFileDownloadObserver
     */
    public void downLoadFile(String url, final String desDir, final String fileName, final FileDownloadObserver<File> fIleFileDownloadObserver) {
        apiService().downloadFile(url)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(responseBody -> fIleFileDownloadObserver.saveFile(responseBody, desDir, fileName)).compose(mvpView.bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fIleFileDownloadObserver);
    }

    /**
     * 断点下载
     *
     * @param url
     * @param desDir
     * @param fileName
     * @param currentLength
     * @param fileFileDownloadObserver
     */
    public void downloadFile(String url, final String desDir, final String fileName, final long currentLength, final FileDownloadObserver<File> fileFileDownloadObserver) {
        String range = "bytes=" + currentLength + "-";
        apiService().downloadFile(url, range)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(responseBody -> fileFileDownloadObserver.saveFile(responseBody, desDir, fileName, currentLength))
                .compose(mvpView.bindLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileFileDownloadObserver);
    }

}
