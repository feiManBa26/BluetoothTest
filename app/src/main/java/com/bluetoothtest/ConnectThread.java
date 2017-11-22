package com.bluetoothtest;

import android.os.AsyncTask;

/**
 * File: ConnectThread.java
 * Author: Mr.Zang
 * Version: V100R001C01
 * Create: 2017-11-21 10:21
 * 蓝牙配对线程方法
 */

public class ConnectThread extends AsyncTask<String, String, String> {

    @Override
    protected String doInBackground(String... strings) {
        //获取连接参数列表
        if (strings != null && strings.length > 0) {
            for (int i = 0, size = strings.length; i < size; i++) {
                String addressStr = strings[i];
            }
        }
        return null;
    }
}
