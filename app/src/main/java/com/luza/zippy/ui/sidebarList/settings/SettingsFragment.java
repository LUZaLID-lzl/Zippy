package com.luza.zippy.ui.sidebarList.settings;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import com.luza.zippy.SplashActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.fragments.HomeFragment;
import com.luza.zippy.ui.sidebarList.test.TestFragment;
import com.luza.zippy.ui.utils.ImageProcess;

import java.util.Arrays;
import java.util.List;

public class SettingsFragment extends BaseFragment {
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_ZH = "zh";
    private ShardPerfenceSetting shardPerfenceSetting;
    private Util util;
    private SwitchCompat languageSwitch;
    private RecyclerView themeRecyclerView;
    private ThemeAdapter themeAdapter;
    private SeekBar seekBar;
    private TextView sliderValueText;
    private TextInputEditText activationInput;
    private MaterialButton activationButton;
    private TextInputLayout activationInputLayout;

    private List<String> activeList = Arrays.asList(
            "LUZaLID"
    );

    private SwitchCompat titleLayoutSwitch;

    @Override
    protected String getTitle() {
        return getString(R.string.settings);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void initViews(View view) {
        util = new Util();
        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());

        loadTitleLayout(view);
        loadLanguage(view);
        loadTheme(view);
        setupSlider(view);
        setupActivation(view);
        setupVersionInfo(view);
    }

    public void loadTitleLayout(View view) {
        titleLayoutSwitch = view.findViewById(R.id.switch_title_layout);

        // 设置当前布局状态
        Boolean isHorizontal = shardPerfenceSetting.getArrange();
        titleLayoutSwitch.setChecked(isHorizontal);

        // 监听切换事件
        titleLayoutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shardPerfenceSetting.setArrange(isChecked);
            showLoading();
        });
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
                new ThemeItem("Pokemon", "squirtle", SplashActivity.squirtleImages[3], R.drawable.bg_setting_card_background_squirtle),
                new ThemeItem("Character", "maple", SplashActivity.mapleImages[2], R.drawable.bg_setting_card_background_maple),
                new ThemeItem("Cutey", "capoo", SplashActivity.capooImages[2], R.drawable.bg_setting_card_background_capoo),
                new ThemeItem("Pokemon", "pikachu", SplashActivity.pikachuImages[1], R.drawable.bg_setting_card_background_pikaqiu),
                new ThemeItem("Pokemon", "bulbasaur", SplashActivity.bulbasaurImages[2], R.drawable.bg_setting_card_background_bulbasaur),
                new ThemeItem("Pokemon", "mew", SplashActivity.mewImages[0], R.drawable.bg_setting_card_background_mew),
                new ThemeItem("Legends", "karsa", SplashActivity.karsaImages[5], R.drawable.bg_setting_card_background_karsa)
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

        // 1秒后关闭加载对话框并刷新页面
        new Handler().postDelayed(() -> {
            loadingDialog.dismiss();
            // 创建新的Fragment实例
            Fragment newFragment = new SettingsFragment();
            // 替换当前Fragment
            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.replace(((View) getView().getParent()).getId(), newFragment);
            ft.commit();
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

    private void setupActivation(View view) {
        activationInputLayout = view.findViewById(R.id.activation_input_layout);
        activationInput = view.findViewById(R.id.activation_input);
        activationButton = view.findViewById(R.id.activation_button);
        
        // 检查是否已经激活
        Boolean savedActivationCode = shardPerfenceSetting.getActivate();
        if (savedActivationCode) {
            // 已激活状态
            updateActivationUI(true);
            activationInput.setText("XXXXX");  // 使用掩码显示
            //Toast.makeText(requireContext(), getString(R.string.activation_status_activated), Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 设置输入框的焦点变化监听
        activationInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                // 当失去焦点时，如果输入为空则显示错误提示
                String input = activationInput.getText().toString().trim();
                if (input.isEmpty()) {
                    activationInputLayout.setError(getString(R.string.activation_hint));
                } else {
                    activationInputLayout.setError(null);
                }
            }
        });
        
        activationButton.setOnClickListener(v -> {
            String activationCode = activationInput.getText().toString().trim();
            if (activationCode.isEmpty()) {
                activationInputLayout.setError(getString(R.string.activation_hint));
                return;
            }
            activationInputLayout.setError(null);

            if (activeList.contains(activationCode)){
                Toast.makeText(requireContext(), getString(R.string.activation_success), Toast.LENGTH_SHORT).show();
                updateActivationUI(true);
                activationInput.setText("XXXXX");  // 激活成功后使用掩码显示

                shardPerfenceSetting.setActivate(true);
                shardPerfenceSetting.setActiveName(activationCode);
                // 更新导航菜单中计时器的显示状态
                MainActivity activity = (MainActivity) requireActivity();
                Menu navMenu = activity.navigationView.getMenu();
                MenuItem timerItem = navMenu.findItem(R.id.nav_timer);
                timerItem.setVisible(true);
            } else {
                Toast.makeText(requireContext(), getString(R.string.activation_failed), Toast.LENGTH_SHORT).show();
                updateActivationUI(false);
                
                // 隐藏计时器菜单项
                MainActivity activity = (MainActivity) requireActivity();
                Menu navMenu = activity.navigationView.getMenu();
                MenuItem timerItem = navMenu.findItem(R.id.nav_timer);
                timerItem.setVisible(false);
            }
        });
    }

    private void updateActivationUI(boolean isActivated) {
        // 更新输入框和按钮的状态
        activationInput.setEnabled(!isActivated);
        activationButton.setEnabled(!isActivated);
        
        // 更新按钮的视觉状态
        if (isActivated) {
            activationButton.setAlpha(0.5f);  // 设置半透明表示禁用状态
            activationButton.setText(getString(R.string.activation_status_activated));
        } else {
            activationButton.setAlpha(1.0f);
            activationButton.setText(getString(R.string.activation_button));
        }
    }

    private void setupVersionInfo(View view) {
        TextView versionInfoText = view.findViewById(R.id.text_version_info);
        TextView versionCodeText = view.findViewById(R.id.text_version_code);

        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            String versionName = packageInfo.versionName;  // 版本名称
            String versionCode = ""; // 版本号
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = String.valueOf(packageInfo.getLongVersionCode());
            }

            // 直接使用AndroidManifest.xml中定义的版本号
            versionInfoText.setText(String.format(getString(R.string.version_description), versionName));
            versionCodeText.setText(String.format(getString(R.string.version_code), versionCode));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}