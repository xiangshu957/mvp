package com.zrx.basemvp.retrofitwithrxjava.uploadutils;

import android.os.Handler;
import android.os.Looper;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DefaultObserver;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:
 */
public abstract class FileUploadObserver<T> extends DefaultObserver<T> {

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onNext(@NonNull T t) {
        handler.post(() -> {
            onUploadSuccess(t);
        });
    }

    @Override
    public void onError(@NonNull Throwable e) {
        handler.post(() -> {
            onUploadFail(e);
        });
    }

    @Override
    public void onComplete() {

    }

    public void onProgressChange(final long byteWritten, final long contentLength) {
        handler.post(() -> {
            onProgress((int) (byteWritten * 100 / contentLength));
        });
    }

    //上传成功的回调
    public abstract void onUploadSuccess(T t);

    //上传事变的回调
    public abstract void onUploadFail(Throwable e);

    //上传进度回调
    public abstract void onProgress(int progress);
}
