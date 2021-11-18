package com.zrx.mvp.ui.main;

import com.zrx.basemvp.base.BaseView;
import com.zrx.mvp.model.TestBean;

/**
 * @Author: ZhangRuixiang
 * Date: 2021/11/18
 * DES:
 */
public interface MainContract {

    interface View extends BaseView{

        void getDataSuccess(TestBean dataJson);
        void getDataFail(String message);

    }

    interface Presenter {

        void getData();

    }

}
