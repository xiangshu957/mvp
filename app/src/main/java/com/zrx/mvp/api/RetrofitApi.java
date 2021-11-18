package com.zrx.mvp.api;

import com.zrx.basemvp.retrofitwithrxjava.RetrofitApiService;
import com.zrx.mvp.common.SysCommon;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public interface RetrofitApi extends RetrofitApiService {

    @GET(SysCommon.YM_RUL)
    Observable<String> getData();

}
