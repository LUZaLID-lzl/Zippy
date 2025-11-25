package com.luza.zippy.ui.sidebarList.minecraft.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager;
import android.util.DisplayMetrics;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.widget.RadioGroup;
import android.widget.RadioButton;

import com.google.android.material.button.MaterialButton;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MinecraftLocationSettingFragment extends BaseFragment {
    private LocationViewModel viewModel;
    private LocationAdapter adapter;
    private EditText etCenterX, etCenterY;
    private RecyclerView rvLocations;
    private MaterialButton btnAddLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minecraft_location_setting, container, false);
        viewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View view) {
        etCenterX = view.findViewById(R.id.etCenterX);
        etCenterY = view.findViewById(R.id.etCenterY);
        rvLocations = view.findViewById(R.id.rvLocations);
        btnAddLocation = view.findViewById(R.id.fabAddLocation);

        // 设置RecyclerView
        adapter = new LocationAdapter(new LocationAdapter.LocationDiff());
        rvLocations.setAdapter(adapter);
        rvLocations.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 观察数据变化
        viewModel.getAllLocations().observe(getViewLifecycleOwner(), locations -> {
            adapter.submitLocationList(locations);
        });

        // 设置点击监听器
        adapter.setOnLocationClickListener(new LocationAdapter.OnLocationClickListener() {
            @Override
            public void onEditClick(LocationModel location) {
                showEditLocationDialog(location);
            }

            @Override
            public void onDeleteClick(LocationModel location) {
                showDeleteConfirmDialog(location);
            }
        });

        // 添加新坐标
        btnAddLocation.setOnClickListener(v -> {
            showAddLocationDialog();
        });

        // 加载保存的中心坐标
        loadCenterLocation();

        // 设置坐标变化监听器
        TextWatcher centerCoordinatesWatcher = new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable saveRunnable = new Runnable() {
                @Override
                public void run() {
                    saveCenterLocation();
                }
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // 取消之前的延迟保存
                handler.removeCallbacks(saveRunnable);
                // 延迟500ms后保存，避免频繁保存
                handler.postDelayed(saveRunnable, 500);
            }
        };

        etCenterX.addTextChangedListener(centerCoordinatesWatcher);
        etCenterY.addTextChangedListener(centerCoordinatesWatcher);
    }

    private void showAddLocationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_edit, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        
        // 初始化控件
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etX = dialogView.findViewById(R.id.etX);
        EditText etY = dialogView.findViewById(R.id.etY);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        RadioGroup rgConnection = dialogView.findViewById(R.id.rgConnection);
        RadioGroup rgDimension = dialogView.findViewById(R.id.rgDimension);
        RadioButton rbNether = dialogView.findViewById(R.id.rbNether);

        // 设置默认选择
        rgConnection.check(R.id.rbConnectionNone); // 默认选择"不连接"
        rbNether.setChecked(true); // 默认选择地狱

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("添加坐标")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        // 设置对话框窗口属性
        dialog.setOnShowListener(dialogInterface -> {
            // 设置按钮颜色
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
            
            // 设置保存按钮点击事件
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                // 验证输入
                if (validateInput(etName, etX, etY)) {
                    String name = etName.getText().toString();
                    int x = Integer.parseInt(etX.getText().toString());
                    int y = Integer.parseInt(etY.getText().toString());
                    
                    // 获取连接方式
                    String connection;
                    int connectionId = rgConnection.getCheckedRadioButtonId();
                    if (connectionId == R.id.rbConnectionNone) {
                        connection = "不连接";
                    } else if (connectionId == R.id.rbConnectionX) {
                        connection = "X轴连接";
                    } else if (connectionId == R.id.rbConnectionY) {
                        connection = "Y轴连接";
                    } else {
                        connection = "XY都连接";
                    }
                    
                    // 获取选中的维度
                    String dimension;
                    int checkedId = rgDimension.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbOverworld) {
                        dimension = "主世界";
                    } else if (checkedId == R.id.rbNether) {
                        dimension = "地狱";
                    } else {
                        dimension = "末地";
                    }
                    
                    String description = etDescription.getText().toString();

                    LocationModel location = new LocationModel(name, x, y, connection, dimension, description);
                    viewModel.insert(location);
                    adapter.notifyDataSetChanged(); // 通知适配器数据已更改
                    
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "坐标已添加", Toast.LENGTH_SHORT).show();
                }
            });

            // 设置对话框高度为屏幕高度的90%
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                
                // 获取屏幕高度
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                
                // 设置最大高度为屏幕高度的90%
                layoutParams.height = (int) (displayMetrics.heightPixels * 0.9);
                
                // 设置软键盘模式
                layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
                
                window.setAttributes(layoutParams);
            }
        });

        dialog.show();
    }

    private boolean validateInput(EditText etName, EditText etX, EditText etY) {
        if (etName.getText().toString().trim().isEmpty()) {
            etName.setError("请输入坐标名称");
            return false;
        }
        
        try {
            String xText = etX.getText().toString();
            if (xText.isEmpty()) {
                etX.setError("请输入X坐标");
                return false;
            }
            int x = Integer.parseInt(xText);
            // 检查是否在合理范围内
            if (x < -30000000 || x > 30000000) {
                etX.setError("X坐标超出范围");
                return false;
            }
        } catch (NumberFormatException e) {
            etX.setError("请输入有效的X坐标");
            return false;
        }
        
        try {
            String yText = etY.getText().toString();
            if (yText.isEmpty()) {
                etY.setError("请输入Y坐标");
                return false;
            }
            int y = Integer.parseInt(yText);
            // 检查是否在合理范围内
            if (y < -30000000 || y > 30000000) {
                etY.setError("Y坐标超出范围");
                return false;
            }
        } catch (NumberFormatException e) {
            etY.setError("请输入有效的Y坐标");
            return false;
        }
        
        return true;
    }

    private void showEditLocationDialog(LocationModel location) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_location_edit, null);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        
        // 初始化控件
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etX = dialogView.findViewById(R.id.etX);
        EditText etY = dialogView.findViewById(R.id.etY);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        RadioGroup rgConnection = dialogView.findViewById(R.id.rgConnection);
        RadioGroup rgDimension = dialogView.findViewById(R.id.rgDimension);

        // 设置现有数据
        etName.setText(location.getName());
        etX.setText(String.valueOf(location.getX()));
        etY.setText(String.valueOf(location.getY()));
        etDescription.setText(location.getDescription());

        // 设置连接方式
        switch (location.getConnectionType()) {
            case "不连接":
                rgConnection.check(R.id.rbConnectionNone);
                break;
            case "X轴连接":
                rgConnection.check(R.id.rbConnectionX);
                break;
            case "Y轴连接":
                rgConnection.check(R.id.rbConnectionY);
                break;
            case "XY都连接":
                rgConnection.check(R.id.rbConnectionXY);
                break;
        }

        // 设置维度选择
        switch (location.getDimension()) {
            case "主世界":
                rgDimension.check(R.id.rbOverworld);
                break;
            case "地狱":
                rgDimension.check(R.id.rbNether);
                break;
            case "末地":
                rgDimension.check(R.id.rbEnd);
                break;
        }

        AlertDialog dialog = builder.setView(dialogView)
                .setTitle("编辑坐标")
                .setPositiveButton("保存", null)
                .setNegativeButton("取消", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (validateInput(etName, etX, etY)) {
                    location.setName(etName.getText().toString());
                    location.setX(Integer.parseInt(etX.getText().toString()));
                    location.setY(Integer.parseInt(etY.getText().toString()));
                    
                    // 获取连接方式
                    int connectionId = rgConnection.getCheckedRadioButtonId();
                    if (connectionId == R.id.rbConnectionNone) {
                        location.setConnectionType("不连接");
                    } else if (connectionId == R.id.rbConnectionX) {
                        location.setConnectionType("X轴连接");
                    } else if (connectionId == R.id.rbConnectionY) {
                        location.setConnectionType("Y轴连接");
                    } else {
                        location.setConnectionType("XY都连接");
                    }
                    
                    // 获取选中的维度
                    int checkedId = rgDimension.getCheckedRadioButtonId();
                    if (checkedId == R.id.rbOverworld) {
                        location.setDimension("主世界");
                    } else if (checkedId == R.id.rbNether) {
                        location.setDimension("地狱");
                    } else {
                        location.setDimension("末地");
                    }
                    
                    location.setDescription(etDescription.getText().toString());
                    
                    // 更新数据并确保UI更新
                    viewModel.update(location);
                    adapter.notifyDataSetChanged(); // 通知适配器数据已更改
                    
                    dialog.dismiss();
                    Toast.makeText(requireContext(), "坐标已更新", Toast.LENGTH_SHORT).show();
                }
            });

            // 设置对话框高度为屏幕高度的90%
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(window.getAttributes());
                
                // 获取屏幕高度
                DisplayMetrics displayMetrics = new DisplayMetrics();
                window.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                
                // 设置最大高度为屏幕高度的90%
                layoutParams.height = (int) (displayMetrics.heightPixels * 0.9);
                
                // 设置软键盘模式
                layoutParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
                
                window.setAttributes(layoutParams);
            }
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(LocationModel location) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("删除坐标")
                .setMessage("确定要删除坐标 \"" + location.getName() + "\" 吗？")
                .setPositiveButton("删除", null)
                .setNegativeButton("取消", null)
                .setIcon(R.drawable.ic_delete);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                viewModel.delete(location);
                Toast.makeText(requireContext(), "已删除坐标", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void saveCenterLocation() {
        String centerX = etCenterX.getText().toString();
        String centerY = etCenterY.getText().toString();
        
        if (!centerX.isEmpty() && !centerY.isEmpty()) {
            try {
                SharedPreferences prefs = requireContext().getSharedPreferences("minecraft_settings", Context.MODE_PRIVATE);
                prefs.edit()
                        .putInt("center_x", Integer.parseInt(centerX))
                        .putInt("center_y", Integer.parseInt(centerY))
                        .apply();
            } catch (NumberFormatException e) {
                // 忽略无效输入
            }
        }
    }

    private void loadCenterLocation() {
        SharedPreferences prefs = requireContext().getSharedPreferences("minecraft_settings", Context.MODE_PRIVATE);
        int centerX = prefs.getInt("center_x", 0);
        int centerY = prefs.getInt("center_y", 0);
        
        etCenterX.setText(String.valueOf(centerX));
        etCenterY.setText(String.valueOf(centerY));
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_minecraft_location_setting);
    }
}