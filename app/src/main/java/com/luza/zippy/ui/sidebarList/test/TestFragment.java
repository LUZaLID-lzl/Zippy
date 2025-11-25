package com.luza.zippy.ui.sidebarList.test;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import android.content.Intent;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Objects;

public class TestFragment extends BaseFragment {

    private static final String TAG = "liziluo_TestFragment";
    private Button button;

    private TextView textView;

    private TextView textViewBluetooth;
    private TextView textViewWifi;
    private BroadcastReceiver receiver;

    private static final int REQUEST_CODE_SCAN = 1001;
    private ActivityResultLauncher<Intent> scanLauncher;

    private static final String BLUETOOTH_MAC_ADDRESS = "/sys/devices/platform/ecn_info/bluetooth_addr";
    private static final String WIFI_MAC_ADDRESS = "/sys/devices/platform/ecn_info/wifi_addr";
    private static final String SN_INFO = "/sys/devices/platform/ecn_info/sn_info";

    private static final String ECN_INFO_JSON = "/sys/devices/platform/ecn_info/json_info";

    @Override
    protected String getTitle() {
        return getString(R.string.menu_test);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    protected void initViews(View view) {
        textView = view.findViewById(R.id.textview1);

        // 初始化新增的TextView
        textViewBluetooth = view.findViewById(R.id.textview_bluetooth);
        textViewWifi = view.findViewById(R.id.textview_wifi);

        // 更新SN信息
        textView.setText("ECN_INFO: " + ReadFile(ECN_INFO_JSON));

        // 更新蓝牙MAC地址
        textViewBluetooth.setText("Bluetooth MAC: " + ReadFile(BLUETOOTH_MAC_ADDRESS));
//
//        // 更新Wi-Fi MAC地址
//        textViewWifi.setText("Wi-Fi MAC: " + ReadFile(WIFI_MAC_ADDRESS));


        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(getContext());
        if (nfcAdapter == null) {
            // 设备不支持 NFC
            Log.d("NFC1", "该设备不支持NFC");
        } else if (!nfcAdapter.isEnabled()) {
            // NFC 已支持但未启用
            Log.d("NFC1", "NFC支持但未开启");
        } else {
            // NFC 已启用
            Log.d("NFC1", "NFC已启用");
        }
        try {
            // 反射调用兼容不同版本
            Method method = nfcAdapter.getClass().getMethod("isNdefPushEnabled", Context.class);
            boolean isP2pSupported = (boolean) method.invoke(nfcAdapter, requireContext());

            Log.d("NFC1", "isP2pSupported： " + isP2pSupported);
        } catch (Exception e) {
            // 回退处理
        }

        registerBroadcastReceiver();

        // 初始化（通常在 onCreate 或 onViewCreated 中）
//        scanLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == Activity.RESULT_OK) {
//                        Intent data = result.getData();
//                        if (data != null) {
//                            // 处理返回的数据
//                            String resultText = data.getStringExtra("com.cipherlab.image2textlauncher.notify.text");
//                            Log.d("Result", "返回结果: " + resultText);
//                        }
//                    } else {
//                        Log.d("Result", "用户取消或失败");
//                    }
//                }
//        );

        // 启动目标 Activity
//        Intent intent = new Intent();
//        intent.setAction("com.cipherlab.intent.action.ImageToText");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        // 可选：设置包名确保定向调用（避免选择器弹窗）
//        intent.setPackage("com.cipherlab.imagetotext");
//        startActivity(intent);
//        //scanLauncher.launch(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume： ");
    }

    private void registerBroadcastReceiver() {
        Log.d(TAG, "registerBroadcastReceiver： ");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.cipherlab.image2textlauncher.notify");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive： ");
                String textValue = intent.getStringExtra("com.cipherlab.image2textlauncher.notify.text");
                if (textValue != null) {
                    Log.d(TAG, "广播接收到参数: " + textValue);
                }
            }
        };

    }

    public static String ReadFile(String sys_path) {
        String prop = "waiting";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(sys_path));
            prop = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

}