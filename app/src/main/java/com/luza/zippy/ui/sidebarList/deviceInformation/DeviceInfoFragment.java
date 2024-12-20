package com.luza.zippy.ui.sidebarList.deviceInformation;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.sidebarList.deviceInformation.InfoAdapter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceInfoFragment extends BaseFragment {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private View currentSelectedTab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_info, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_device_info);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        // 设置点击事件
        View appsTab = view.findViewById(R.id.layout_apps);
        View sensorsTab = view.findViewById(R.id.layout_sensors);
        View deviceInfoTab = view.findViewById(R.id.layout_device_info);

        appsTab.setOnClickListener(v -> {
            updateTabSelection(v);
            switchContent(this::loadApps);
        });
        sensorsTab.setOnClickListener(v -> {
            updateTabSelection(v);
            switchContent(this::loadSensors);
        });
        deviceInfoTab.setOnClickListener(v -> {
            updateTabSelection(v);
            switchContent(this::loadDeviceInfo);
        });

        // 默认选中应用列表（无动画）
        updateTabSelection(appsTab);
        loadApps();
    }

    private void updateTabSelection(View selectedTab) {
        // 清除之前选中的状态
        if (currentSelectedTab != null) {
            currentSelectedTab.setBackgroundResource(android.R.color.transparent);
        }
        // 设置新的选中状态
        selectedTab.setBackgroundResource(R.drawable.bg_tab_selected);
        currentSelectedTab = selectedTab;
    }

    private void loadApps() {
        PackageManager pm = requireContext().getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        
        AppListAdapter appListAdapter = new AppListAdapter(pm);
        recyclerView.setAdapter(appListAdapter);
        appListAdapter.setData(apps);
    }

    private void loadSensors() {
        SensorManager sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String> sensorNames = new ArrayList<>();
        
        for (Sensor sensor : sensors) {
            sensorNames.add(sensor.getName());
        }
        
        InfoAdapter infoAdapter = new InfoAdapter();
        recyclerView.setAdapter(infoAdapter);
        infoAdapter.setHeaderText(String.format(MainActivity.mContext.getString(R.string.device_do_sensor_num) + "：%d", sensors.size()));
        infoAdapter.setData(sensorNames);
    }

    private void loadDeviceInfo() {
        List<String> info = new ArrayList<>();
        
        // 基本设备信息
        info.add(getString(R.string.device_info_basic));
        info.add(getString(R.string.device_brand) + ": " + Build.BRAND);
        info.add(getString(R.string.device_model) + ": " + Build.MODEL);
        info.add(getString(R.string.device_manufacturer) + ": " + Build.MANUFACTURER);
        info.add(getString(R.string.device_product) + ": " + Build.PRODUCT);
        info.add(getString(R.string.device_code) + ": " + Build.DEVICE);
        
        // 系统信息
        info.add(getString(R.string.device_info_system));
        info.add(getString(R.string.device_android_version) + ": " + Build.VERSION.RELEASE);
        info.add(getString(R.string.device_sdk_version) + ": " + Build.VERSION.SDK_INT);
        info.add(getString(R.string.device_build_version) + ": " + Build.DISPLAY);
        info.add(getString(R.string.device_radio_version) + ": " + Build.getRadioVersion());
        info.add(getString(R.string.device_kernel) + ": " + getKernelVersion());
        info.add(getString(R.string.device_build_time) + ": " + getBuildTime());
        
        // 硬件信息
        info.add(getString(R.string.device_info_hardware));
        info.add(getString(R.string.device_cpu) + ": " + Build.SUPPORTED_ABIS[0]);
        info.add(getString(R.string.device_cpu_cores) + ": " + Runtime.getRuntime().availableProcessors());
        info.add(getString(R.string.device_screen) + ": " + getScreenResolution());
        info.add(getString(R.string.device_density) + ": " + getScreenDensity());
        info.add(getString(R.string.device_memory_total) + ": " + getTotalMemory());
        info.add(getString(R.string.device_memory_available) + ": " + getAvailableMemory());
        info.add(getString(R.string.device_storage_total) + ": " + getTotalInternalStorage());
        info.add(getString(R.string.device_storage_available) + ": " + getAvailableInternalStorage());
        
        // 网络信息
        info.add(getString(R.string.device_info_network));
        info.add(getString(R.string.device_mac) + ": " + getMacAddress());
        info.add(getString(R.string.device_ip) + ": " + getIPAddress());
        info.add(getString(R.string.device_network_type) + ": " + getNetworkType());
        
        InfoAdapter infoAdapter = new InfoAdapter();
        recyclerView.setAdapter(infoAdapter);
        infoAdapter.setData(info);
    }

    // 获取内核版本
    private String getKernelVersion() {
        try {
            return System.getProperty("os.version");
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取构建时间
    private String getBuildTime() {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date(Build.TIME));
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取屏幕密度
    private String getScreenDensity() {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return String.format("%.1f dpi", metrics.density * 160);
    }

    // 获取总内存
    private String getTotalMemory() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) requireContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(memoryInfo);
            return formatSize(memoryInfo.totalMem);
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取可用内存
    private String getAvailableMemory() {
        try {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) requireContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(memoryInfo);
            return formatSize(memoryInfo.availMem);
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取内部存储总空间
    private String getTotalInternalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            return formatSize(totalBlocks * blockSize);
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取内部存储可用空间
    private String getAvailableInternalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            return formatSize(availableBlocks * blockSize);
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取MAC地址
    private String getMacAddress() {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");
            byte[] macBytes = networkInterface.getHardwareAddress();
            if (macBytes == null) return getString(R.string.unknow);
            
            StringBuilder mac = new StringBuilder();
            for (byte b : macBytes) {
                mac.append(String.format("%02X:", b));
            }
            if (mac.length() > 0) {
                mac.deleteCharAt(mac.length() - 1);
            }
            return mac.toString();
        } catch (Exception e) {
            return getString(R.string.unknow);
        }
    }

    // 获取IP地址
    private String getIPAddress() {
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return getString(R.string.unknow);
    }

    // 获取网络类型
    private String getNetworkType() {
        try {
            ConnectivityManager cm = (ConnectivityManager) requireContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    return "WiFi";
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    return getString(R.string.mobile_data);
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return getString(R.string.unknow);
    }

    // 格式化文件大小
    private String formatSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) 
            + " " + units[digitGroups];
    }

    private String getScreenResolution() {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels + " x " + metrics.heightPixels;
    }

    private void switchContent(Runnable loadAction) {
        // 创建淡出动画
        Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // 淡出动画结束后，加载新内容并开始淡入动画
                loadAction.run();
                Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in);
                recyclerView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // 开始淡出动画
        recyclerView.startAnimation(fadeOut);
    }
} 