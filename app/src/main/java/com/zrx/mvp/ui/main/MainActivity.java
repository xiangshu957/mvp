package com.zrx.mvp.ui.main;

import com.zrx.basemvp.base.BaseActivity;
import com.zrx.basemvp.utils.LogUtils;
import com.zrx.mvp.R;

public class MainActivity extends BaseActivity<MainPresenter> implements MainContract.View {


    @Override
    public MainPresenter createPresenter() {
        return new MainPresenter();
    }

    @Override
    public int getContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    public void setListener() {
        mPresenter.getData();
    }

    @Override
    public void processLogic() {

    }

    @Override
    public void registerReceiver() {

    }

    @Override
    public void unRegisterReceiver() {

    }

    @Override
    public void showLoading(String msg) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void getDataSuccess(String dataJson) {
        LogUtils.e(dataJson);
    }

    @Override
    public void getDataFail(String message) {
        LogUtils.e(message);
    }
}