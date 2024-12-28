package com.luza.zippy.ui.sidebarList.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.sidebarList.test.TestFragment;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends BaseFragment {
    private static final String PREF_NAME = "zippy_settings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HOMEANIMALTIONNUM = "homeAnimationNum";
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_ZH = "zh";

    private static final String KEY_HOMETHEME = "homeTheme";

    private ShardPerfenceSetting shardPerfenceSetting;
    private Util util;
    private SwitchCompat languageSwitch;
    private RecyclerView themeRecyclerView;
    private ThemeAdapter themeAdapter;
    private SeekBar seekBar;
    private TextView sliderValueText;
    private static final String KEY_SLIDER_VALUE = "slider_value";

    @Override
    protected String getTitle() {
        return getString(R.string.settings);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void initViews(View view) {
        util = new Util();
        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());

        loadLanguage(view);
        loadTheme(view);
        setupSlider(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void loadLanguage(View view){
        languageSwitch = view.findViewById(R.id.switch_language);

        // 设置当前语言状态
        String currentLang = shardPerfenceSetting.getLanguage();
        languageSwitch.setChecked(LANGUAGE_EN.equals(currentLang));

        // 监听切换事件
        languageSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String newLang = isChecked ? LANGUAGE_EN : LANGUAGE_ZH;
            if (!newLang.equals(currentLang)) {
                android.util.Log.d("ShardPerfenceSetting","newLang : " +newLang);
                shardPerfenceSetting.setLanguage(newLang);
                showLoading();
                util.updateLocale(requireContext());
            }
        });
    }

    public void loadTheme(View view){
        themeRecyclerView = view.findViewById(R.id.theme_recycler_view);
        themeRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        themeAdapter = new ThemeAdapter(getThemeList(),getActivity());
        themeRecyclerView.setAdapter(themeAdapter);
    }

    private List<ThemeItem> getThemeList() {
        return Arrays.asList(
                new ThemeItem("Pokemon", "squirtle", R.drawable.squirtle_5, R.drawable.bg_setting_card_background_squirtle),
                new ThemeItem("Pokemon", "pikachu",  R.drawable.pikaqiu_2, R.drawable.bg_setting_card_background_pikaqiu),
                new ThemeItem("Pokemon", "bulbasaur", R.drawable.bulbasaur_4, R.drawable.bg_setting_card_background_bulbasaur),
                new ThemeItem("Pokemon", "mew", R.drawable.mew_1, R.drawable.bg_setting_card_background_mew)
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getSystemLanguage() {
        return Resources.getSystem().getConfiguration().getLocales().get(0).getLanguage();
    }

    private void showLoading() {
        Dialog loadingDialog = new Dialog(requireContext(), R.style.LoadingDialog);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        loadingDialog.show();

        // 1秒后关闭加载对话框并切换语言
        new Handler().postDelayed(() -> {
            loadingDialog.dismiss();
            requireActivity().recreate();
        }, 1000);
    }

    private void setupSlider(View view) {
        seekBar = view.findViewById(R.id.seekbar);
        sliderValueText = view.findViewById(R.id.slider_value);
        
        // 获取保存的值
        int savedValue = shardPerfenceSetting.getHomeAnimationNum();
        android.util.Log.d("liziluo","savedValue: " + savedValue);
        seekBar.setProgress(savedValue);
        updateSliderText(savedValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateSliderText(progress);
                // 保存当前值
                android.util.Log.d("liziluo","ShardPerfenceSetting: " + progress);
                shardPerfenceSetting.setHomeAnimationNum(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 开始拖动时的操作（如果需要）
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 停止拖动时的操作（如果需要）
            }
        });
    }

    private void updateSliderText(int value) {
        sliderValueText.setText(String.format(getString(R.string.homeAnimationNum_current_num) + "%d", value));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}