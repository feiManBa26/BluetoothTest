package com.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_ENABLE_BT = 100;
    private static String TAG = MainActivity.class.getName();
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 10;
    private ListView mListItem;
    private BlueToothBroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        // 设置广播信息过滤
        mReceiver = new BlueToothBroadcastReceiver(mListItem);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);//每搜索到一个设备就会发送一个该广播
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//当全部搜索完后发送该广播
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.setPriority(Integer.MAX_VALUE);//设置优先级
        registerReceiver(mReceiver, filter);// 注册蓝牙搜索广播接收者，接收并处理搜索结果

        if (getLocalBluetooth()) { //当前设备是否支持蓝牙
            if (!mBluetoothAdapter.isEnabled()) {
                //动态开启蓝牙功能
//                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                //静态开启蓝牙功能
                requestBluetoothPermission();
            } else {
                //搜索蓝牙设备列表
                inquireBlueEquipment();
                //打印与当前设备配对的蓝牙设备列表
                pairedDevicesPrintln();
            }
        }

        initEvent();
    }

    private BluetoothDevice mDevice;
    static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

    private void initEvent() {
        if (mListItem != null) {
            mListItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Map<String, String> s = (Map<String, String>) parent.getItemAtPosition(position);
                    String address = s.get("address");//把地址解析出来
                    Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
                    try {
                        //主动连接蓝牙服务端
                        if (mBluetoothAdapter.isDiscovering()) {
                            mBluetoothAdapter.cancelDiscovery();
                        }

                        if (mDevice == null) {
                            mDevice = mBluetoothAdapter.getRemoteDevice(address);
                        }

                        boolean returnValue = false;
                        if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
                            Method createBondMethod = BluetoothDevice.class
                                    .getMethod("createBond");
                            createBondMethod.invoke(mDevice);
                        } else if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {

                            if (mBluetoothSocket == null) {
                                //创建客户端蓝牙Socket
                                try {
                                    mBluetoothSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
                                    if (mBluetoothSocket != null) {
                                        //开始连接蓝牙，如果没有配对则弹出对话框提示我们进行配对
                                        mBluetoothSocket.connect();
                                        if (mBluetoothSocket.isConnected()) {
                                            //获得输出流（客户端指向服务端输出文本）
                                            mOutputStream = mBluetoothSocket.getOutputStream();
                                        }
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        mOutputStream.write("信息来啦".getBytes("UTF-8"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

    public void onShow(View view) {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
                while (!mBluetoothAdapter.startDiscovery()) {
                    Log.e(TAG, "尝试失败");
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {

                mBluetoothAdapter.enable(); //开启
                inquireBlueEquipment();
            }
        } else {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestBluetoothPermission() {
        //判断系统版本
        if (Build.VERSION.SDK_INT >= 23) {
            //检测当前app是否拥有某个权限
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            //判断这个权限是否已经授权过
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                //判断是否需要 向用户解释，为什么要申请该权限
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
                    Toast.makeText(this, "Need bluetooth permission.",
                            Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]
                        {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_BLUETOOTH_PERMISSION);
                return;
            } else {
                //当前程序获取到开启蓝牙权限
                if (getLocalBluetooth()) {
                    //判断蓝牙是否开启
                    if (mBluetoothAdapter.isEnabled()) {
                        pairedDevicesPrintln();
                        inquireBlueEquipment();
                    } else {
                        mBluetoothAdapter.enable(); //开启
                        pairedDevicesPrintln();
                        inquireBlueEquipment();
                    }
                }
            }
        } else {
            if (getLocalBluetooth()) {
                //判断蓝牙是否开启
                if (mBluetoothAdapter.isEnabled()) {
                    pairedDevicesPrintln();
                    inquireBlueEquipment();
                } else {
                    mBluetoothAdapter.enable(); //开启
                    pairedDevicesPrintln();
                    inquireBlueEquipment();
                }
            }
        }
    }

    /**
     * 寻找蓝牙设备
     */
    private void inquireBlueEquipment() {
        mBluetoothAdapter.cancelDiscovery();
        // 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
        while (!mBluetoothAdapter.startDiscovery()) {
            Log.e(TAG, "尝试失败");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 输入当前蓝牙已经配对的对象
     */
    private void pairedDevicesPrintln() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.i(TAG, "onCreate: " + device.getName() + "  " + device.getAddress());
            }
        }
    }

    /**
     * 获取蓝牙对象
     *
     * @return
     */
    private boolean getLocalBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_BLUETOOTH_PERMISSION:
                if (getLocalBluetooth()) {
                    //判断蓝牙是否开启
                    if (mBluetoothAdapter.isEnabled()) {
                        pairedDevicesPrintln();
                        inquireBlueEquipment();
                    } else {
                        mBluetoothAdapter.enable(); //开启
                        pairedDevicesPrintln();
                        inquireBlueEquipment();
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            pairedDevicesPrintln();
        }
    }

    private void initView() {
        mListItem = (ListView) findViewById(R.id.list_item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }
}
