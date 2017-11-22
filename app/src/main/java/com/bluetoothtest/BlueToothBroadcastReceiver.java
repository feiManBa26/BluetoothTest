package com.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * File: BlueToothBroadcastReceiver.java
 * Author: ejiang
 * 蓝牙搜索广播接收器
 * Create: 2017-11-20 10:28
 */

public final class BlueToothBroadcastReceiver extends BroadcastReceiver {

    /**
     * 搜索出的设备集合
     */
    private List<Map<String, String>> devices = new ArrayList<>();
    /**
     * 发现的设备列表
     */
    private ListView mDevicesLv;
    private SimpleAdapter mSimpleAdapter;

    public SimpleAdapter getSimpleAdapter() {
        return mSimpleAdapter;
    }

    public BlueToothBroadcastReceiver(ListView devicesLv) {
        mDevicesLv = devicesLv;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            Toast.makeText(BaseApplication.getContext(), "Showing Devices", Toast.LENGTH_SHORT).show();
            // 从Intent中获取设备对象
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // 定义一个装载蓝牙设备名字和地址的Map
            Map<String, String> deviceMap = new HashMap<>();
            // 过滤已配对的和重复的蓝牙设备
            if ((device.getBondState() != BluetoothDevice.BOND_BONDED) && isSingleDevice(device)) {
                deviceMap.put("name", device.getName() == null ? "null" : device.getName());
                deviceMap.put("address", device.getAddress());
                devices.add(deviceMap);
            }
            // 显示发现的蓝牙设备列表
            mDevicesLv.setVisibility(View.VISIBLE);
            // 加载设备
            showDevices();
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //已搜素完成
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_NONE:
                    Log.e(getPackageName(), "取消配对");
                    break;
                case BluetoothDevice.BOND_BONDING:
                    Log.e(getPackageName(), "配对中");
                    break;
                case BluetoothDevice.BOND_BONDED:
                    Log.e(getPackageName(), "配对成功");
                    break;
            }
        }
    }


    /**
     * 判断此设备是否存在
     */
    private boolean isSingleDevice(BluetoothDevice device) {
        if (devices == null) {
            return true;
        }
        for (Map<String, String> mDeviceMap : devices) {
            if ((device.getAddress()).equals(mDeviceMap.get("address"))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示搜索到的设备列表
     */
    private void showDevices() {
        mSimpleAdapter = new SimpleAdapter(BaseApplication.getContext(), devices,
                android.R.layout.simple_list_item_2,
                new String[]{"name", "address"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mDevicesLv.setAdapter(mSimpleAdapter);
    }

    public String getPackageName() {
        return BlueToothBroadcastReceiver.class.getName();
    }
}
