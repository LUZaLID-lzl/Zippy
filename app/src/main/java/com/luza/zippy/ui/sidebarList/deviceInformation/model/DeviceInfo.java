package com.luza.zippy.ui.sidebarList.deviceInformation.model;

public class DeviceInfo {
    private String deviceName;
    private String deviceModel;
    private String systemVersion;
    // 添加其他需要的设备信息字段

    // Getters and Setters
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }
} 