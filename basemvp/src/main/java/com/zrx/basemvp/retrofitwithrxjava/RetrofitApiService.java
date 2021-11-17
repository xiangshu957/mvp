package com.zrx.basemvp.retrofitwithrxjava;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/13
 * DES:
 */
public interface RetrofitApiService {

    //Retrofit上传文件
    @POST
    @Multipart
    Observable<ResponseBody> uploadFile(@Url String url, @Part("sequence") RequestBody sequence, @Part MultipartBody.Part file);

    //Retrofit下载文件
    @GET
    @Streaming
    //10以上@Streaming。不会造成oom
    Observable<ResponseBody> downloadFile(@Url String url);

    @GET
    @Streaming
    Observable<ResponseBody> downloadFile(@Url String url, @Header("RANGE") String range);

}
