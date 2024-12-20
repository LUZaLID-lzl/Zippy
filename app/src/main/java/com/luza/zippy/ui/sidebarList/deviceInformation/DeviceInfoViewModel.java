package com.luza.zippy.ui.sidebarList.deviceInformation;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DeviceInfoViewModel extends ViewModel {
    private MutableLiveData<String> deviceInfo = new MutableLiveData<>();

    public LiveData<String> getDeviceInfo() {
        return deviceInfo;
    }

    public void loadDeviceInfo() {
        // TODO: 加载设备信息
    }
} 