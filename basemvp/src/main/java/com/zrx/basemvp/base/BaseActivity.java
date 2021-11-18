package com.zrx.basemvp.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxFragmentActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public abstract class BaseActivity<T extends BasePresenter> extends RxFragmentActivity implements BaseView {

    public T mPresenter;

    public abstract T createPresenter();

    //获取当前activity的布局文件
    public abstract int getContentViewId();

    //设置监听事件
    public abstract void setListener();

    //处理业务逻辑
    public abstract void processLogic();

    //注册广播
    public abstract void registerReceiver();

    //注销广播
    public abstract void unRegisterReceiver();

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());
        mUnbinder = ButterKnife.bind(this);
        mPresenter = createPresenter();
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        setListener();
        processLogic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        mUnbinder.unbind();
    }

    public void transfer(Class<?> clazz) {
        Intent intent = new Intent(this, clazz);
        startActivity(intent);
    }

    public void transfer(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        intent.putExtra("bundle", bundle);
        startActivity(intent);
    }

    public String getStringByUi(View view) {
        if (view instanceof EditText) {
            return ((EditText) view).getText().toString().trim();
        } else if (view instanceof TextView) {
            return ((TextView) view).getText().toString().trim();
        }
        return "";
    }

    @Override
    public LifecycleTransformer bindLifecycle() {
        LifecycleTransformer<Object> objectLifecycleTransformer = bindToLifecycle();
        return objectLifecycleTransformer;
    }
}
