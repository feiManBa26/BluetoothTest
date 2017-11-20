package com.bluetoothtest;

import android.app.Application;
import android.content.Context;

/**
 * File: BaseApplication.java
 * Author: ejiang
 * Version: V100R001C01
 * Create: 2017-11-20 10:31
 */

public class BaseApplication extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }
}
