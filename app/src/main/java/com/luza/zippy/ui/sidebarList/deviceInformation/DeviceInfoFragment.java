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
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
    private View layoutApps, layoutSensors, layoutDeviceInfo;
    private ImageView imageApps, imageSensors, imageDeviceInfo;
    private TextView textApps, textSensors, textDeviceInfo;

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
        
        // 初始化视图引用
        layoutApps = view.findViewById(R.id.layout_apps);
        layoutSensors = view.findViewById(R.id.layout_sensors);
        layoutDeviceInfo = view.findViewById(R.id.layout_device_info);
        
        imageApps = view.findViewById(R.id.image_apps);
        imageSensors = view.findViewById(R.id.image_sensors);
        imageDeviceInfo = view.findViewById(R.id.image_device_info);
        
        textApps = view.findViewById(R.id.text_apps);
        textSensors = view.findViewById(R.id.text_sensors);
        textDeviceInfo = view.findViewById(R.id.text_device_info);

        // 设置点击事件
        layoutDeviceInfo.setOnClickListener(v -> {
            updateSelection(0);
            showDeviceInfo();
        });

        layoutApps.setOnClickListener(v -> {
            updateSelection(1);
            showAppList();
        });

        layoutSensors.setOnClickListener(v -> {
            updateSelection(2);
            showSensorList();
        });

        // 默认选中其他信息
        updateSelection(0);
        showDeviceInfo();
    }

    private void updateSelection(int selectedIndex) {
        // 重置所有选项的状态并播放动画
        resetItemWithAnimation(layoutApps, imageApps, textApps);
        resetItemWithAnimation(layoutSensors, imageSensors, textSensors);
        resetItemWithAnimation(layoutDeviceInfo, imageDeviceInfo, textDeviceInfo);

        // 设置选中项的状态并播放动画
        switch (selectedIndex) {
            case 0:
                selectItemWithAnimation(layoutDeviceInfo, imageDeviceInfo, textDeviceInfo);
                break;
            case 1:
                selectItemWithAnimation(layoutApps, imageApps, textApps);
                break;
            case 2:
                selectItemWithAnimation(layoutSensors, imageSensors, textSensors);
                break;
        }
    }

    private void selectItemWithAnimation(View layout, ImageView icon, TextView text) {
        layout.setSelected(true);
        icon.setSelected(true);
        text.setSelected(true);

        Animation selectedAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.device_info_item_selected);
        icon.startAnimation(selectedAnim);
    }

    private void resetItemWithAnimation(View layout, ImageView icon, TextView text) {
        if (layout.isSelected()) {
            Animation unselectedAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.device_info_item_unselected);
            icon.startAnimation(unselectedAnim);
        }
        
        layout.setSelected(false);
        icon.setSelected(false);
        text.setSelected(false);
    }

    private void switchContent(Runnable loadAction) {
        recyclerView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    loadAction.run();
                    recyclerView.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void showAppList() {
        switchContent(() -> {
            // 创建一个新的Handler用于主线程更新UI
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            // 在子线程中加载应用列表
            new Thread(() -> {
                try {
                    PackageManager pm = requireContext().getPackageManager();
                    List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                    
                    // 按照应用大小从大到小排序
                    Collections.sort(apps, (a, b) -> {
                        long sizeA = new File(a.sourceDir).length();
                        long sizeB = new File(b.sourceDir).length();
                        // 降序排列
                        return Long.compare(sizeB, sizeA);
                    });
                    
                    // 在主线程中更新UI
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {  // 确保Fragment仍然有效
                            AppListAdapter appListAdapter = new AppListAdapter(pm);
                            recyclerView.setAdapter(appListAdapter);
                            appListAdapter.setData(apps);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DeviceInfoFragment", "Error loading app list", e);
                    // 在主线程中显示错误信息
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {
                            Toast.makeText(requireContext(), 
                                "加载应用列表失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });
    }

    private void showSensorList() {
        switchContent(() -> {
            // 创建一个新的Handler用于主线程更新UI
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            // 在子线程中加载传感器列表
            new Thread(() -> {
                try {
                    SensorManager sensorManager = (SensorManager) requireContext()
                        .getSystemService(Context.SENSOR_SERVICE);
                    List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
                    List<String> sensorNames = new ArrayList<>();
                    
                    for (Sensor sensor : sensors) {
                        sensorNames.add(sensor.getName());
                    }
                    
                    // 在主线程中更新UI
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {  // 确保Fragment仍然有效
                            InfoAdapter infoAdapter = new InfoAdapter();
                            recyclerView.setAdapter(infoAdapter);
                            infoAdapter.setHeaderText(String.format(
                                MainActivity.mContext.getString(R.string.device_do_sensor_num) + "：%d", 
                                sensors.size()));
                            infoAdapter.setData(sensorNames);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DeviceInfoFragment", "Error loading sensor list", e);
                    // 在主线程中显示错误信息
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {
                            Toast.makeText(requireContext(), 
                                "加载传感器列表失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });
    }

    private void showDeviceInfo() {
        switchContent(() -> {
            // 创建一个新的Handler用于主线程更新UI
            Handler mainHandler = new Handler(Looper.getMainLooper());
            
            // 在子线程中收集设备信息
            new Thread(() -> {
                try {
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
                    
                    // 在主线程中更新UI
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {  // 确保Fragment仍然有效
                            InfoAdapter infoAdapter = new InfoAdapter();
                            recyclerView.setAdapter(infoAdapter);
                            infoAdapter.setData(info);
                        }
                    });
                } catch (Exception e) {
                    Log.e("DeviceInfoFragment", "Error loading device info", e);
                    // 在主线程中显示错误信息
                    mainHandler.post(() -> {
                        if (isAdded() && !isDetached()) {
                            Toast.makeText(requireContext(), 
                                "加载设备信息失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).start();
        });
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
} 