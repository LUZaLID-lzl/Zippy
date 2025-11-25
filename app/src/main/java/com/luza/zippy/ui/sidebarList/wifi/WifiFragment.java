package com.luza.zippy.ui.sidebarList.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class WifiFragment extends BaseFragment {

    private WifiManager wifiManager;
    private TextView ssidText, bssidText, signalText, ipText, macText;
    private TextView frequencyText, speedText, securityText, statusText;
    private TextView speedResultText;
    private MaterialButton speedTestBtn;
    private boolean isSpeedTesting = false;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_wifi, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_wifi);
    }

    @Override
    protected void initViews(View view) {
        // 初始化WiFi管理器
        wifiManager = (WifiManager) requireContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // 获取控件
        ssidText = view.findViewById(R.id.text_wifi_ssid);
        bssidText = view.findViewById(R.id.text_wifi_bssid);
        signalText = view.findViewById(R.id.text_wifi_signal);
        ipText = view.findViewById(R.id.text_wifi_ip);
        macText = view.findViewById(R.id.text_wifi_mac);
        frequencyText = view.findViewById(R.id.text_wifi_frequency);
        speedText = view.findViewById(R.id.text_wifi_speed);
        securityText = view.findViewById(R.id.text_wifi_security);
        statusText = view.findViewById(R.id.text_wifi_status);
        MaterialButton pingBtn = view.findViewById(R.id.btn_wifi_ping);
        TextView pingResult = view.findViewById(R.id.text_wifi_ping_result);
        speedTestBtn = view.findViewById(R.id.btn_wifi_speed);
        speedResultText = view.findViewById(R.id.text_wifi_speed_result);

        // 更新WiFi信息
        updateWifiInfo();

        // Ping测试
        pingBtn.setOnClickListener(v -> {
            ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            if (!isConnected) {
                pingResult.setText("请先连接网络");
                return;
            }
            pingResult.setText(getString(R.string.wifi_test_running));
            new PingTask(pingResult, requireContext()).execute("8.8.8.8");
        });

        // 速率测试
        speedTestBtn.setOnClickListener(v -> {
            if (isSpeedTesting) {
                return;
            }
            ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            
            if (!isConnected) {
                speedResultText.setText("请先连接网络");
                return;
            }
            startSpeedTest();
        });
    }

    private void updateWifiInfo() {
        ConnectivityManager cm = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        
        if (!isConnected) {
            // 未连接网络
            setDisconnectedState();
            return;
        }

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            // WiFi已连接
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            ssidText.setText("网络名称: " + ssid);
            bssidText.setText("BSSID: " + wifiInfo.getBSSID());
            
            // 信号强度
            int rssi = wifiInfo.getRssi();
            int signalLevel = WifiManager.calculateSignalLevel(rssi, 100);
            signalText.setText("信号强度: " + signalLevel + "% (" + rssi + " dBm)");
            
            // IP地址
            int ip = wifiInfo.getIpAddress();
            ipText.setText("IP地址: " + Formatter.formatIpAddress(ip));
            
            // MAC地址
            String macAddress = wifiInfo.getMacAddress();
            if (macAddress != null && !macAddress.equals("02:00:00:00:00:00")) {
                macText.setText("MAC地址: " + macAddress);
            } else {
                macText.setText("MAC地址: 无法获取");
            }

            // 频率
            int frequency = wifiInfo.getFrequency();
            String band = frequency > 5000 ? "5GHz" : "2.4GHz";
            frequencyText.setText("频率: " + frequency + " MHz (" + band + ")");

            // 连接速度
            int linkSpeed = wifiInfo.getLinkSpeed();
            speedText.setText("连接速度: " + linkSpeed + " Mbps");

            // 安全性
            securityText.setText("安全性: WPA/WPA2");

            // 连接状态
            statusText.setText("连接状态: 已连接");
        } else {
            setDisconnectedState();
        }
    }

    private void setDisconnectedState() {
        ssidText.setText("网络名称: 未连接");
        bssidText.setText("BSSID: 未连接");
        signalText.setText("信号强度: 未连接");
        ipText.setText("IP地址: 未连接");
        macText.setText("MAC地址: 未连接");
        frequencyText.setText("频率: 未连接");
        speedText.setText("连接速度: 未连接");
        securityText.setText("安全性: 未连接");
        statusText.setText("连接状态: 未连接");
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWifiInfo();
    }

    // 异步Ping任务
    private static class PingTask extends AsyncTask<String, Void, Boolean> {
        private final TextView resultView;
        private final Context context;
        public PingTask(TextView resultView, Context context) {
            this.resultView = resultView;
            this.context = context;
        }
        @Override
        protected Boolean doInBackground(String... params) {
            String ip = params[0];
            try {
                Process process = Runtime.getRuntime().exec("ping -c 1 -w 2 " + ip);
                int status = process.waitFor();
                return status == 0;
            } catch (Exception e) {
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                resultView.setText(context.getString(R.string.wifi_test_success));
            } else {
                resultView.setText(context.getString(R.string.wifi_test_fail));
            }
        }
    }

    private void startSpeedTest() {
        isSpeedTesting = true;
        speedTestBtn.setEnabled(false);
        speedResultText.setText("测试中...");
        
        new Thread(() -> {
            try {
                // 测试下载速度
                String testUrl = "https://speed.cloudflare.com/__down?bytes=25000000"; // 25MB文件
                URL url = new URL(testUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(10000);
                connection.connect();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream input = connection.getInputStream();
                    byte[] buffer = new byte[8192];
                    long startTime = new Date().getTime();
                    long totalBytesRead = 0;
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                        long currentTime = new Date().getTime();
                        long timeElapsed = currentTime - startTime;
                        
                        if (timeElapsed > 0) {
                            // 计算速度（Mbps）
                            final double speedMbps = (totalBytesRead * 8.0 / 1000000.0) / (timeElapsed / 1000.0);
                            
                            // 更新UI
                            handler.post(() -> {
                                speedResultText.setText(String.format("当前速度: %.1f Mbps", speedMbps));
                            });
                        }
                    }
                    
                    input.close();
                    long endTime = new Date().getTime();
                    long timeElapsed = endTime - startTime;
                    
                    if (timeElapsed > 0) {
                        // 计算平均速度
                        final double averageSpeedMbps = (totalBytesRead * 8.0 / 1000000.0) / (timeElapsed / 1000.0);
                        
                        handler.post(() -> {
                            speedResultText.setText(String.format("平均下载速度: %.1f Mbps", averageSpeedMbps));
                            speedTestBtn.setEnabled(true);
                            isSpeedTesting = false;
                        });
                    }
                } else {
                    handler.post(() -> {
                        speedResultText.setText("测试失败，请稍后重试");
                        speedTestBtn.setEnabled(true);
                        isSpeedTesting = false;
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    speedResultText.setText("测试失败: " + e.getMessage());
                    speedTestBtn.setEnabled(true);
                    isSpeedTesting = false;
                });
            }
        }).start();
    }
}