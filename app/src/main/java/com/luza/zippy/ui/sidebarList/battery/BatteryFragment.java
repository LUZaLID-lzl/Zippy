package com.luza.zippy.ui.sidebarList.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.sidebarList.battery.BatteryInfoAdapter;
import java.util.ArrayList;
import java.util.List;

public class BatteryFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private BatteryInfoAdapter infoAdapter;
    private BroadcastReceiver batteryReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_battery, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_battery);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 初始化适配器
        infoAdapter = new BatteryInfoAdapter();
        recyclerView.setAdapter(infoAdapter);
        
        // 注册电池状态变化广播接收器
        registerBatteryReceiver();
        
        // 加载初始电池信息
        loadBatteryInfo();
    }

    private void registerBatteryReceiver() {
        batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                    loadBatteryInfo();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        requireContext().registerReceiver(batteryReceiver, filter);
    }

    private void loadBatteryInfo() {
        List<String> info = new ArrayList<>();
        
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = requireContext().registerReceiver(null, ifilter);
        
        if (batteryStatus != null) {
            // 获取电池电量
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            float batteryPct = level * 100 / (float)scale;
            info.add(String.format(getString(R.string.battery_current_level), batteryPct));
            
            // 获取充电状态
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            String statusText = getString(R.string.unknow);
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusText = getString(R.string.battery_status_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusText = getString(R.string.battery_status_discharging);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusText = getString(R.string.battery_status_full);
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusText = getString(R.string.battery_status_not_charging);
                    break;
            }
            info.add(String.format(getString(R.string.battery_charging_status), statusText));
            android.util.Log.d("liziluo","UpdateLight red steady: " + statusText);
            
            // 获取充电类型
            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            String chargeType = getString(R.string.unknow);
            switch (chargePlug) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    chargeType = getString(R.string.battery_type_ac);
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    chargeType = getString(R.string.battery_type_usb);
                    break;
                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    chargeType = getString(R.string.battery_type_wireless);
                    break;
            }
            info.add(String.format(getString(R.string.battery_charging_type), chargeType));
            
            // 获取电池温度
            float temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) / 10.0f;
            info.add(String.format(getString(R.string.battery_temp_value), temp));
            
            // 获取电池电压
            int voltage = batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
            info.add(String.format(getString(R.string.battery_voltage), voltage / 1000.0f));
            
            // 获取电池技术
            String technology = batteryStatus.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            info.add(String.format(getString(R.string.battery_tech), 
                technology != null ? technology : getString(R.string.unknow)));
        }
        
        // 更新适配器数据
        infoAdapter.setData(info);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 注销广播接收器
        if (batteryReceiver != null) {
            try {
                requireContext().unregisterReceiver(batteryReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            batteryReceiver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时刷新电池信息
        loadBatteryInfo();
    }
} 