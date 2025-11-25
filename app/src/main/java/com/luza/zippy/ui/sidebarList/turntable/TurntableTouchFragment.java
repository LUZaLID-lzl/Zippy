package com.luza.zippy.ui.sidebarList.turntable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.TouchSelectView;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TurntableTouchFragment extends BaseFragment implements TouchSelectView.TouchSelectListener {
    private TouchSelectView touchSelectView;
    private TextView hintText;
    private TextView subHintText;
    private TextView countdownText;
    private Handler countdownHandler;
    private static final long COUNTDOWN_INTERVAL = 1000; // 1秒
    private static final long PREPARATION_TIME = 5000; // 5秒

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turntable_touch, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_turntable_touch);
    }

    @Override
    protected void initViews(View view) {
        touchSelectView = view.findViewById(R.id.touch_select_view);
        hintText = view.findViewById(R.id.text_hint);
        subHintText = view.findViewById(R.id.text_sub_hint);
        countdownText = view.findViewById(R.id.text_countdown);
        countdownHandler = new Handler(Looper.getMainLooper());

        touchSelectView.setTouchSelectListener(this);
        touchSelectView.setOnMaxTouchReachedListener(() -> {
            if (isAdded()) {
                subHintText.setText("最多只能同时按压 5 个位置");
                // 2秒后恢复原来的提示
                subHintText.postDelayed(() -> {
                    if (isAdded()) {
                        subHintText.setText("可以同时按压多个位置");
                    }
                }, 2000);
            }
        });
    }

    @Override
    public void onPreparationStart() {
        if (isAdded()) {
            hintText.setText("准备中，请保持手指按压");
            subHintText.setText("请保持手指位置不变");
            startCountdown();
        }
    }

    private void startCountdown() {
        countdownText.setVisibility(View.VISIBLE);
        final long startTime = System.currentTimeMillis();
        
        Runnable updateCountdown = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = PREPARATION_TIME - elapsedTime;
                
                if (remainingTime > 0) {
                    int secondsRemaining = (int) (remainingTime / 1000) + 1;
                    countdownText.setText(secondsRemaining + "s");
                    countdownHandler.postDelayed(this, COUNTDOWN_INTERVAL);
                } else {
                    countdownText.setVisibility(View.GONE);
                }
            }
        };
        
        countdownHandler.post(updateCountdown);
    }

    @Override
    public void onSelectionStart() {
        if (isAdded()) {
            hintText.setText("正在随机选择");
            subHintText.setText("请保持手指按压");
            countdownText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSelectionComplete(int selectedIndex) {
        if (isAdded()) {
            hintText.setText("已选择第 " + (selectedIndex + 1) + " 个位置");
            subHintText.setText("5 秒后自动重置");
            countdownText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onReset() {
        if (isAdded()) {
            hintText.setText("请将手指按在屏幕上");
            subHintText.setText("可以同时按压多个位置（最多5个）");
            countdownText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        countdownHandler.removeCallbacksAndMessages(null);
    }
}