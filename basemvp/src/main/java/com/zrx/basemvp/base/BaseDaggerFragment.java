package com.zrx.basemvp.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.RxFragment;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public abstract class BaseDaggerFragment<T extends BasePresenter> extends RxFragment implements BaseView, HasAndroidInjector {

    @Inject
    public T mPresenter;

    //获取当前fragment布局文件
    public abstract int getContentViewId();

    //设置监听事件
    protected abstract void setListener();

    //处理业务逻辑
    protected abstract void processLogic(Bundle saveInstanceState);

    //注册广播
    public abstract void registerReceiver();

    //注销广播
    public abstract void unRegisterReceiver();

    protected View mContentView;
    private Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (mContentView == null) {
            setContentView(getContentViewId());
            setListener();
            processLogic(savedInstanceState);
        } else {
            ViewGroup parent = (ViewGroup) mContentView.getParent();
            if (parent != null) {
                parent.removeView(mContentView);
            }
        }
        return mContentView;
    }

    private void setContentView(int contentViewID) {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
        mContentView = LayoutInflater.from(getActivity()).inflate(contentViewID, null);
        mUnbinder = ButterKnife.bind(this, mContentView);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detachView();
            mPresenter = null;
        }
        mUnbinder.unbind();
    }

    public void transfer(Class<?> clazz) {
        Intent intent = new Intent(getActivity(), clazz);
        startActivity(intent);
    }

    public void transfer(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(getActivity(), clazz);
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

    @Override
    public void onAttach(Activity activity) {
        AndroidInjection.inject(this);
        super.onAttach(activity);
    }

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
}
