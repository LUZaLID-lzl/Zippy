package com.luza.zippy.ui.sidebarList.compass;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.CompassScaleView;

/**
 * 指南针Fragment
 * 实现指南针功能，包括方向检测、海拔显示等
 */
public class CompassFragment extends BaseFragment implements SensorEventListener, LocationListener {

    private CompassScaleView compassRose;
    private ImageView directionIndicator;
    private TextView tvDirection;
    private TextView tvDegrees;
    private TextView tvAccuracy;
    private TextView tvAltitude;
    private TextView tvLatitude;
    private TextView tvLongitude;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private LocationManager locationManager;

    private float[] accelerometerValues = new float[3];
    private float[] magnetometerValues = new float[3];
    private float currentDegree = 0f;
    private float targetDegree = 0f;

    private boolean hasAccelerometer = false;
    private boolean hasMagnetometer = false;
    private boolean hasLocationPermission = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compass, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_compass);
    }

    @Override
    protected void initViews(View view) {
        // 初始化视图
        compassRose = view.findViewById(R.id.compass_rose);
        directionIndicator = view.findViewById(R.id.direction_indicator);
        tvDirection = view.findViewById(R.id.tv_direction);
        tvDegrees = view.findViewById(R.id.tv_degrees);
        tvAccuracy = view.findViewById(R.id.tv_accuracy);
        tvAltitude = view.findViewById(R.id.tv_altitude);
        tvLatitude = view.findViewById(R.id.tv_latitude);
        tvLongitude = view.findViewById(R.id.tv_longitude);

        // 初始化传感器管理器
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            
            hasAccelerometer = (accelerometer != null);
            hasMagnetometer = (magnetometer != null);
            
            // 调试信息
            System.out.println("Accelerometer available: " + hasAccelerometer);
            System.out.println("Magnetometer available: " + hasMagnetometer);
        }

        // 初始化位置管理器
        locationManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);

        // 检查权限
        checkPermissions();

        // 设置返回按钮
        setupBackButton(view);
        
        // 初始化UI
        initializeUI();
    }

    private void initializeUI() {
        // 设置初始值
        tvDirection.setText(getString(R.string.compass_north));
        tvDegrees.setText(getString(R.string.compass_degrees, 0));
        tvAccuracy.setText(getString(R.string.compass_calibrating));
        tvAltitude.setText(getString(R.string.compass_altitude, 0));
        tvLatitude.setText(getString(R.string.compass_latitude, 0.0));
        tvLongitude.setText(getString(R.string.compass_longitude, 0.0));
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasLocationPermission = true;
            startLocationUpdates();
        } else {
            hasLocationPermission = false;
            tvAccuracy.setText(getString(R.string.compass_permission_denied));
            // 请求权限
            ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasLocationPermission = true;
                startLocationUpdates();
            } else {
                hasLocationPermission = false;
                tvAccuracy.setText(getString(R.string.compass_permission_denied));
            }
        }
    }

    private void startLocationUpdates() {
        if (locationManager != null && hasLocationPermission) {
            try {
                // 检查GPS是否可用
                boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                
                System.out.println("GPS enabled: " + isGPSEnabled + ", Network enabled: " + isNetworkEnabled);
                
                // 同时使用GPS和网络提供者
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 
                        1000, // 1秒更新一次
                        1, // 1米精度
                        this
                    );
                }
                
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        1000,
                        1,
                        this
                    );
                }
                
                // 如果没有提供者可用，显示提示
                if (!isGPSEnabled && !isNetworkEnabled) {
                    tvAccuracy.setText("位置服务未开启");
                    tvLatitude.setText(getString(R.string.compass_latitude, 0.0));
                    tvLongitude.setText(getString(R.string.compass_longitude, 0.0));
                    tvAltitude.setText(getString(R.string.compass_altitude, 0));
                } else {
                    tvAccuracy.setText("正在获取位置...");
                }
                
            } catch (SecurityException e) {
                e.printStackTrace();
                tvAccuracy.setText("位置权限错误");
            }
        } else {
            tvAccuracy.setText(getString(R.string.compass_permission_denied));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (!hasAccelerometer || !hasMagnetometer) {
            Toast.makeText(requireContext(), getString(R.string.compass_no_sensor), 
                Toast.LENGTH_LONG).show();
            return;
        }

        // 注册传感器监听器
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        
        // 重新检查位置权限和更新
        if (hasLocationPermission) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // 取消传感器监听器
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        
        // 停止位置更新
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // 清理资源
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerValues, 0, 3);
        }

        // 只有当两个传感器都有数据时才计算方向
        if (accelerometerValues[0] != 0 || accelerometerValues[1] != 0 || accelerometerValues[2] != 0) {
            if (magnetometerValues[0] != 0 || magnetometerValues[1] != 0 || magnetometerValues[2] != 0) {
                calculateDirection();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 更新精度显示
        String accuracyText;
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                accuracyText = getString(R.string.compass_accuracy_high);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                accuracyText = getString(R.string.compass_accuracy_medium);
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                accuracyText = getString(R.string.compass_accuracy_low);
                break;
            default:
                accuracyText = getString(R.string.compass_calibrating);
                break;
        }
        
        // 只有在没有位置精度信息时才显示传感器精度
        if (tvAccuracy.getText().toString().contains("正在获取位置") || 
            tvAccuracy.getText().toString().contains("校准")) {
            tvAccuracy.setText(accuracyText);
        }
    }

    private void calculateDirection() {
        float[] rotationMatrix = new float[9];
        float[] orientationAngles = new float[3];

        // 计算旋转矩阵
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, 
            accelerometerValues, magnetometerValues);

        if (success) {
            // 获取方向角度
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            
            // 转换为度数
            float azimuthInRadians = orientationAngles[0];
            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);
            
            // 确保角度为正数
            if (azimuthInDegrees < 0) {
                azimuthInDegrees += 360;
            }

            // 更新UI
            updateCompassUI(azimuthInDegrees);
        } else {
            // 传感器数据无效，显示校准信息
            tvAccuracy.setText(getString(R.string.compass_calibrating));
        }
    }

    private void updateCompassUI(float degrees) {
        // 更新角度显示
        tvDegrees.setText(getString(R.string.compass_degrees, (int) degrees));
        
        // 更新方向文字
        String direction = getDirectionText(degrees);
        tvDirection.setText(direction);
        
        // 更新指南针旋转
        updateCompassRotation(degrees);
    }

    private String getDirectionText(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return getString(R.string.compass_north);
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return getString(R.string.compass_northeast);
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return getString(R.string.compass_east);
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return getString(R.string.compass_southeast);
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return getString(R.string.compass_south);
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return getString(R.string.compass_southwest);
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return getString(R.string.compass_west);
        } else if (degrees >= 292.5 && degrees < 337.5) {
            return getString(R.string.compass_northwest);
        }
        return getString(R.string.compass_north);
    }

    private void updateCompassRotation(float degrees) {
        // 方向指示器应该旋转，指南针盘保持不动
        float rotation = degrees;
        
        // 创建旋转动画
        RotateAnimation rotateAnimation = new RotateAnimation(
            currentDegree, rotation,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        
        rotateAnimation.setDuration(100); // 减少动画时间，更流畅
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setInterpolator(new android.view.animation.LinearInterpolator()); // 线性插值，更平滑
        
        // 只旋转方向指示器，指南针盘保持不动
        directionIndicator.startAnimation(rotateAnimation);
        
        currentDegree = rotation;
    }

    // LocationListener 实现
    @Override
    public void onLocationChanged(@NonNull Location location) {
        // 更新海拔信息
        double altitude = location.getAltitude();
        tvAltitude.setText(getString(R.string.compass_altitude, (int) altitude));
        
        // 更新经纬度信息
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        
        tvLatitude.setText(getString(R.string.compass_latitude, latitude));
        tvLongitude.setText(getString(R.string.compass_longitude, longitude));
        
        // 更新精度信息
        float accuracy = location.getAccuracy();
        if (accuracy <= 5) {
            tvAccuracy.setText(getString(R.string.compass_accuracy_high));
        } else if (accuracy <= 20) {
            tvAccuracy.setText(getString(R.string.compass_accuracy_medium));
        } else {
            tvAccuracy.setText(getString(R.string.compass_accuracy_low));
        }
        
        // 调试信息
        System.out.println("Location updated - Lat: " + latitude + ", Lng: " + longitude + ", Alt: " + altitude + ", Acc: " + accuracy);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // 位置提供者状态变化
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // 位置提供者启用
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // 位置提供者禁用
    }
}