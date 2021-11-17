package com.zrx.basemvp.retrofitwithrxjava.downloadutils;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:
 */
public abstract class FileDownloadObserver<T> implements Observer<T> {

    Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onNext(@NonNull T t) {
        handler.post(() -> {
            onDownloadSuccess(t);
        });
    }

    @Override
    public void onError(@NonNull Throwable e) {
        handler.post(() -> {
            onDownloadFail(e);
        });
    }

    @Override
    public void onComplete() {

    }

    //下载成功的回调
    public abstract void onDownloadSuccess(T t);

    //下载失败的回调
    public abstract void onDownloadFail(Throwable throwable);

    //下载进度监听
    public abstract void onProgress(int precent, long total);

    /**
     * 下载文件
     * @param responseBody 响应体
     * @param desFileDir  文件夹
     * @param desFileName 文件名
     * @return
     * @throws IOException
     */
    public File saveFile(ResponseBody responseBody, String desFileDir, String desFileName) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = responseBody.byteStream();
            final long total = responseBody.contentLength();
            long sum = 0;
            File dir = new File(desFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, desFileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                handler.post(() -> {
                    onProgress((int) (finalSum * 100 / total), total);
                });
            }
            fos.flush();
            return file;
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * 断点下载
     * @param responseBody 响应体
     * @param desFileDir  文件夹
     * @param desFileName  文件名
     * @param currentLength 当前已下载的文件大小
     * @return
     * @throws IOException
     */
    public File saveFile(ResponseBody responseBody, String desFileDir, String desFileName, long currentLength) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = responseBody.byteStream();
            final long total = responseBody.contentLength() + currentLength;
            long sum = currentLength;
            File dir = new File(desFileDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, desFileName);
            fos = new FileOutputStream(file, true);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                handler.post(() -> {
                    onProgress((int) (finalSum * 100 / total), total);
                });
            }
            fos.flush();
            return file;
        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

}
