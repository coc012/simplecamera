package com.coc.camera;

import android.app.Application;

/**
 * Created by admin on 2017/7/3.
 */

public class MyApplication extends Application {
    private static MyApplication mInstance;

    public static MyApplication getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance =this;
    }
}
