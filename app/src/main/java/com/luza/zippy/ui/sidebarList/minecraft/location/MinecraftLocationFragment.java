package com.luza.zippy.ui.sidebarList.minecraft.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.CoordinateView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class MinecraftLocationFragment extends BaseFragment {
    private LocationViewModel viewModel;
    private CoordinateView coordinateView;
    private RadioGroup rgDimension;
    private TextView tvDimensionName;
    private TextView tvCenterCoordinates;
    private ImageButton btnSettings;
    private ImageButton btnCenter;
    private String currentDimension = "地狱"; // 默认显示地狱
    private int netherCenterX = 0;
    private int netherCenterY = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_minecraft_location, container, false);
        viewModel = new ViewModelProvider(this).get(LocationViewModel.class);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View view) {
        coordinateView = view.findViewById(R.id.coordinateView);
        rgDimension = view.findViewById(R.id.rgDimension);
        tvDimensionName = view.findViewById(R.id.tvDimensionName);
        tvCenterCoordinates = view.findViewById(R.id.tvCenterCoordinates);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnCenter = view.findViewById(R.id.btnCenter);

        MaterialCardView cardView = view.findViewById(R.id.cardView);
        
        // 默认选择地狱维度
        rgDimension.check(R.id.rbNether);
        
        // 设置初始背景（无动画）
        cardView.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_card_nether));
        tvDimensionName.setTextColor(Color.WHITE);
        tvCenterCoordinates.setTextColor(Color.LTGRAY);
        btnSettings.setColorFilter(Color.WHITE);
        btnCenter.setColorFilter(Color.WHITE);
        
        // 加载地狱中心坐标
        loadNetherCenter();

        // 设置按钮点击事件
        btnSettings.setOnClickListener(v -> {
            navigateToFragment(new MinecraftLocationSettingFragment());
        });

        btnCenter.setOnClickListener(v -> {
            // 重置坐标视图的位置和缩放
            coordinateView.resetPosition();
        });

        // 设置维度切换监听
        rgDimension.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOverworld) {
                currentDimension = "主世界";
                updateCardBackground(cardView, R.drawable.bg_card_overworld, false);
                updateCoordinateView(netherCenterX * 8, netherCenterY * 8);
            } else if (checkedId == R.id.rbNether) {
                currentDimension = "地狱";
                updateCardBackground(cardView, R.drawable.bg_card_nether, true);
                updateCoordinateView(netherCenterX, netherCenterY);
            } else {
                currentDimension = "末地";
                updateCardBackground(cardView, R.drawable.bg_card_end, true);
                updateCoordinateView(0, 0);
            }
        });

        // 观察坐标数据变化
        viewModel.getAllLocations().observe(getViewLifecycleOwner(), locations -> {
            List<LocationModel> filteredLocations = locations.stream()
                    .filter(location -> {
                        switch (currentDimension) {
                            case "主世界":
                                // 在主世界只显示地狱坐标（转换为主世界坐标）
                                return location.getDimension().equals("地狱");
                            case "末地":
                                // 在末地只显示末地坐标
                                return location.getDimension().equals("末地");
                            default:
                                // 在地狱显示地狱坐标
                                return location.getDimension().equals("地狱");
                        }
                    })
                    .map(location -> {
                        if (currentDimension.equals("主世界") && location.getDimension().equals("地狱")) {
                            // 创建新的LocationModel，坐标值*8
                            return new LocationModel(
                                location.getName(),
                                location.getX() * 8,
                                location.getY() * 8,
                                location.getConnectionType(),
                                "主世界",  // 显示为主世界坐标
                                location.getDescription()
                            );
                        }
                        return location;
                    })
                    .collect(Collectors.toList());
            coordinateView.setLocations(filteredLocations);
        });
    }

    private void loadNetherCenter() {
        SharedPreferences prefs = requireContext().getSharedPreferences("minecraft_settings", Context.MODE_PRIVATE);
        netherCenterX = prefs.getInt("center_x", 0);
        netherCenterY = prefs.getInt("center_y", 0);
        updateCoordinateView(netherCenterX, netherCenterY);
    }

    private void updateCoordinateView(int centerX, int centerY) {
        tvDimensionName.setText(currentDimension);
        tvCenterCoordinates.setText(String.format("中心坐标：(%d, %d)", centerX, centerY));
        coordinateView.setCenter(centerX, centerY);
        
        float scale = currentDimension.equals("主世界") ? 0.125f : 1.0f;
        coordinateView.setScale(scale);
        
        // 观察坐标数据变化
        viewModel.getAllLocations().observe(getViewLifecycleOwner(), locations -> {
            List<LocationModel> filteredLocations = locations.stream()
                    .filter(location -> {
                        switch (currentDimension) {
                            case "主世界":
                                // 在主世界只显示地狱坐标（转换为主世界坐标）
                                return location.getDimension().equals("地狱");
                            case "末地":
                                // 在末地只显示末地坐标
                                return location.getDimension().equals("末地");
                            default:
                                // 在地狱显示地狱坐标
                                return location.getDimension().equals("地狱");
                        }
                    })
                    .map(location -> {
                        if (currentDimension.equals("主世界") && location.getDimension().equals("地狱")) {
                            // 创建新的LocationModel，坐标值*8
                            return new LocationModel(
                                location.getName(),
                                location.getX() * 8,
                                location.getY() * 8,
                                location.getConnectionType(),
                                "主世界",  // 显示为主世界坐标
                                location.getDescription()
                            );
                        }
                        return location;
                    })
                    .collect(Collectors.toList());
            coordinateView.setLocations(filteredLocations);
        });
    }

    private void updateCardBackground(MaterialCardView cardView, int backgroundResId, boolean isDarkBackground) {
        // 创建动画
        Animation animation = AnimationUtils.loadAnimation(requireContext(), R.anim.card_fade);
        
        // 设置动画监听器
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // 设置新背景
                cardView.setBackground(ContextCompat.getDrawable(requireContext(), backgroundResId));
                
                // 更新文字和按钮颜色
                int textColor = isDarkBackground ? Color.WHITE : Color.BLACK;
                tvDimensionName.setTextColor(textColor);
                tvCenterCoordinates.setTextColor(isDarkBackground ? Color.LTGRAY : Color.GRAY);
                btnSettings.setColorFilter(textColor);
                btnCenter.setColorFilter(textColor);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        // 启动动画
        cardView.startAnimation(animation);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_minecraft_location);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 重新加载中心坐标
        loadNetherCenter();
        // 重新触发维度切换以刷新视图
        int checkedId = rgDimension.getCheckedRadioButtonId();
        if (checkedId == R.id.rbOverworld) {
            currentDimension = "主世界";
            updateCoordinateView(netherCenterX * 8, netherCenterY * 8);
        } else if (checkedId == R.id.rbNether) {
            currentDimension = "地狱";
            updateCoordinateView(netherCenterX, netherCenterY);
        } else {
            currentDimension = "末地";
            updateCoordinateView(0, 0);
        }
    }
}