package com.zrx.basemvp.retrofitwithrxjava.uploadutils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/14
 * DES:
 */
public class UploadFileRequestBody extends RequestBody {

    private RequestBody requestBody;
    private FileUploadObserver<RequestBody> fileUploadObserver;

    public UploadFileRequestBody(File file, FileUploadObserver<RequestBody> fileUploadObserver) {
        this.requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        this.fileUploadObserver = fileUploadObserver;
    }

    public UploadFileRequestBody(String key, ArrayList<File> files, FileUploadObserver<RequestBody> fileUploadObserver) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (files != null) {
            RequestBody fileBody = null;
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                String fileKeyName = key;
                String fileName = file.getName();
                fileBody = RequestBody.create(MediaType.parse(guessMineType(fileName)), file);
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + fileKeyName + "\"; filename=\""), fileBody);
            }
        }
        this.requestBody = builder.build();
        this.fileUploadObserver = fileUploadObserver;
    }

    public UploadFileRequestBody(Map<String, File> fileMap, FileUploadObserver<RequestBody> fileUploadObserver) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if (fileMap != null) {
            RequestBody finalBody = null;
            for (String key : fileMap.keySet()) {
                File file = fileMap.get(key);
                String fileKeyName = key;
                String fileName = file.getName();
                finalBody = RequestBody.create(MediaType.parse(guessMineType(fileName)), file);
                builder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + fileKeyName + "\"; filename=\"" + fileName + "\""), finalBody);
            }
        }
        this.requestBody = builder.build();
        this.fileUploadObserver = fileUploadObserver;
    }

    private String guessMineType(String fileName) {

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(fileName);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }

        return contentTypeFor;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {

        CountingSnk countingSnk = new CountingSnk(bufferedSink);
        BufferedSink buffer = Okio.buffer(countingSnk);
        requestBody.writeTo(buffer);
        buffer.flush();

    }

    protected final class CountingSnk extends ForwardingSink {

        private long byteWritten = 0;

        public CountingSnk(@NotNull Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NotNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            byteWritten += byteCount;
            if (fileUploadObserver != null) {
                fileUploadObserver.onProgressChange(byteWritten, contentLength());
            }
        }
    }
}
