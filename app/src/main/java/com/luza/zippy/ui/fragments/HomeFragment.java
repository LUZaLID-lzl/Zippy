package com.luza.zippy.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.sidebarList.timer.TimerFragment;
import com.luza.zippy.ui.views.SparkView;
import com.luza.zippy.ui.sidebarList.settings.SettingsFragment;
import android.os.Handler;
import android.os.Looper;
import java.util.Random;

import android.content.Intent;

public class HomeFragment extends Fragment {
    private FrameLayout sparkContainer;
    private Handler handler;
    private Random random = new Random();
    private boolean isGeneratingSparks = true;
    private ImageView lightningImage;
    private ShardPerfenceSetting shardPerfenceSetting;
    private int animationNum;
    private final int[] pikachuImages = {
        R.drawable.pikaqiu_2,
        R.drawable.pikaqiu_3,
        R.drawable.pikaqiu_4,
        R.drawable.pikaqiu_6,
        R.drawable.pikaqiu_7
    };

    private final int[] bulbasaurImages = {
        R.drawable.bulbasaur_1,
        R.drawable.bulbasaur_2,
        R.drawable.bulbasaur_3,
        R.drawable.bulbasaur_4,
        R.drawable.bulbasaur_5,
    };

    private final int[] squirtleImages = {
        R.drawable.squirtle_1,
        R.drawable.squirtle_2,
        R.drawable.squirtle_3,
        R.drawable.squirtle_4,
        R.drawable.squirtle_5,
        R.drawable.squirtle_6,
        R.drawable.squirtle_7,
    };

    private final int[] mewImages = {
        R.drawable.mew_1,
        R.drawable.mew_2,
        R.drawable.mew_3,
        R.drawable.mew_4,
        R.drawable.mew_5,
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());
        animationNum = shardPerfenceSetting.getHomeAnimationNum();
        // 设置闪电动画
        lightningImage = view.findViewById(R.id.lightning_image);
        
        // 设置电流效果
        sparkContainer = view.findViewById(R.id.spark_container);
        handler = new Handler(Looper.getMainLooper());

        // 设置菜单按钮点击事件
        ImageButton menuButton = view.findViewById(R.id.btn_menu);
        menuButton.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // 设置按钮点击事件
        view.findViewById(R.id.btn_right).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.content_frame, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });

        // 开始按钮点击事件
        view.findViewById(R.id.btn_left).setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(
                                    R.anim.slide_in_right,
                                    R.anim.slide_out_left
                            )
                            .replace(R.id.content_frame, new TimerFragment() )
                            .addToBackStack(null)
                            .commit();
                }
            }
        });


        // 等待视图布局完成后再开始生成粒子
        sparkContainer.post(() -> startGeneratingSparks());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 随机显示皮卡丘图片

        int[] currentImages;
        animationNum = shardPerfenceSetting.getHomeAnimationNum();

        switch (shardPerfenceSetting.getHomeTheme()){
            case "pikachu":
                currentImages = pikachuImages;
                break;
            case "bulbasaur":
                currentImages = bulbasaurImages;
                break;
            case "squirtle":
                currentImages = squirtleImages;
                break;
            case "mew":
                currentImages = mewImages;
                break;
            default:
                currentImages = pikachuImages;
                break;
        }


        if (lightningImage != null) {
            int randomIndex = new Random().nextInt(currentImages.length);
            lightningImage.setImageResource(currentImages[randomIndex]);
        }
        
        // 如果已经有动画在运行，就不需要重新启动
        restartAnimations();
    }

    private void restartAnimations() {
        // 重新启动中心闪电动画
        Animation chargingAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.charging_animation);
        lightningImage.startAnimation(chargingAnimation);
        
        // 重新生成小闪电
        isGeneratingSparks = true;
        startGeneratingSparks();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止生成小闪电
        isGeneratingSparks = false;
        handler.removeCallbacksAndMessages(null);
        // 停止中心闪电动画
        lightningImage.clearAnimation();
    }

    private void startGeneratingSparks() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGeneratingSparks && isAdded() && sparkContainer != null 
                    && sparkContainer.getWidth() > 0 && sparkContainer.getHeight() > 0) {
                    // 创建新的闪电
                    SparkView sparkView = new SparkView(requireContext(), 
                        sparkContainer.getWidth(), 
                        sparkContainer.getHeight());
                    sparkContainer.addView(sparkView, 
                        new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    
                    // 随机间隔后生成下一个闪电

                    int delayMills = (int) (100 + (100 - 300) * ((animationNum - 10) / (float) (100 - 10))) * 10;
                    handler.postDelayed(this, delayMills);
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置闪电动画
        lightningImage = view.findViewById(R.id.lightning_image);
        
        // 设置电流效果
        sparkContainer = view.findViewById(R.id.spark_container);
        handler = new Handler(Looper.getMainLooper());

        // 等待视图布局完成后再开始生成粒子
        sparkContainer.post(() -> startGeneratingSparks());
    }
} 