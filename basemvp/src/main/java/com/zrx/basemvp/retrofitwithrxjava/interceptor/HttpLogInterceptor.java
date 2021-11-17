package com.zrx.basemvp.retrofitwithrxjava.interceptor;

import android.text.TextUtils;

import com.zrx.basemvp.utils.LogUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:
 */
public class HttpLogInterceptor implements Interceptor {

    private Charset UTF8 = Charset.forName("UTF-8");

    private List<String> imageUrlList;

    public HttpLogInterceptor(List<String> imageUrlList) {
        super();
        if (imageUrlList == null) {
            imageUrlList = new ArrayList<>();
        }
        this.imageUrlList = imageUrlList;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {

        StringBuffer sbf = new StringBuffer();
        Request request = chain.request();

        RequestBody requestBody = request.body();
        String body = null;
        if (requestBody != null) {
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            Charset charset = UTF8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }
            body = buffer.clone().readString(charset);
            if (!TextUtils.isEmpty(body)) {
                String netUrl = request.url().toString();
                if (imageUrlList.contains(netUrl)) {
                    body = "本次请求为图片上传或下载，无法打印参数！";
                } else {
                    body = URLDecoder.decode(body, "utf-8");
                }
            }
        }
        sbf.append("\n请求方式：==>").append(request.method())
                .append("\nurl:==>").append(request.url().toString())
                .append("\n请求头：").append(request.headers())
                .append("\n请求参数：").append(body);
        Response response = chain.proceed(request);
        String rBody = "";
        String netUrl = request.url().toString();
        if (!netUrl.contains("")) {
            ResponseBody responseBody = response.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    e.printStackTrace();
                }
            }
            rBody = buffer.clone().readString(charset);
            if (!TextUtils.isEmpty(rBody)) {
                rBody = decodeUnicode(rBody);
            }

            sbf.append("\n收到响应：code ==>").append(response.code())
                    .append("\nresponse：").append(rBody);
            LogUtils.i("网络请求", sbf.toString());
        }

        return response;
    }

    private String decodeUnicode(String rBody) {
        char aChar;
        int len = rBody.length();
        StringBuffer outBuffer = new StringBuffer(len);
        for (int i = 0; i < len; ) {
            aChar = rBody.charAt(i++);
            if (aChar == '\\') {
                aChar = rBody.charAt(i++);
                if (aChar == 'u') {
                    int value = 0;
                    for (int j = 0; j < 4; j++) {
                        aChar = rBody.charAt(i++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed  \\uxxxx  encoding");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't') {
                        aChar = '\t';
                    } else if (aChar == 'r') {
                        aChar = '\r';
                    } else if (aChar == 'n') {
                        aChar = '\n';
                    } else if (aChar == 'f') {
                        aChar = '\f';
                    }
                    outBuffer.append(aChar);
                }
            } else {
                outBuffer.append(aChar);
            }
        }
        return outBuffer.toString();
    }
}
