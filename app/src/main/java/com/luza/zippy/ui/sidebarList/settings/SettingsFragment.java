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
import android.widget.RadioGroup;
import android.widget.RadioButton;

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
    private SeekBar bottomMenuSizeSeekBar;
    private TextView bottomMenuSizeValueText;
    private TextInputEditText activationInput;
    private MaterialButton activationButton;
    private TextInputLayout activationInputLayout;
    
    // 工作模式相关
    private RadioGroup workModeRadioGroup;
    private static final String WORK_MODE_WORK = "work";
    private static final String WORK_MODE_LIFE = "life";
    private static final String WORK_MODE_GAME = "game";
    private static final String WORK_MODE_ALL = "all";

    private List<String> activeList = Arrays.asList(
            "LUZaLID"
    );

    private SwitchCompat titleLayoutSwitch;
    private SwitchCompat bottomCardSwitch;
    private SwitchCompat randomThemeSwitch;

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
        loadBottomCard(view);
        loadLanguage(view);
        loadWorkMode(view);
        loadTheme(view);
        loadRandomTheme(view);
        setupSlider(view);
        setupBottomMenuSizeSlider(view);
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

    public void loadBottomCard(View view) {
        bottomCardSwitch = view.findViewById(R.id.switch_bottom_menu);

        // 设置当前布局状态
        Boolean isBottomMenu = shardPerfenceSetting.isBottom_menu_bkg();
        bottomCardSwitch.setChecked(isBottomMenu);

        // 监听切换事件
        bottomCardSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shardPerfenceSetting.setBottom_menu_bkg(isChecked);
            showLoading();
            
            // 如果当前是首页，刷新首页以更新底部菜单
            if (getActivity() != null) {
                Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if (currentFragment instanceof HomeFragment) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.detach(currentFragment);
                    transaction.attach(currentFragment);
                    transaction.commit();
                }
            }
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

    public void loadWorkMode(View view) {
        workModeRadioGroup = view.findViewById(R.id.work_mode_radio_group);
        
        // 获取所有RadioButton
        RadioButton radioWork = view.findViewById(R.id.radio_work_mode);
        RadioButton radioLife = view.findViewById(R.id.radio_life_mode);
        RadioButton radioGame = view.findViewById(R.id.radio_game_mode);
        RadioButton radioAll = view.findViewById(R.id.radio_all_mode);
        
        // 根据保存的设置选择对应的单选按钮
        String currentMode = shardPerfenceSetting.getWorkMode();
        clearAllRadioButtons(radioWork, radioLife, radioGame, radioAll);
        
        if (WORK_MODE_WORK.equals(currentMode)) {
            radioWork.setChecked(true);
        } else if (WORK_MODE_LIFE.equals(currentMode)) {
            radioLife.setChecked(true);
        } else if (WORK_MODE_GAME.equals(currentMode)) {
            radioGame.setChecked(true);
        } else {
            radioAll.setChecked(true);
        }
        
        // 为每个RadioButton设置点击监听器
        radioWork.setOnClickListener(v -> handleWorkModeChange(WORK_MODE_WORK, radioWork, radioLife, radioGame, radioAll));
        radioLife.setOnClickListener(v -> handleWorkModeChange(WORK_MODE_LIFE, radioWork, radioLife, radioGame, radioAll));
        radioGame.setOnClickListener(v -> handleWorkModeChange(WORK_MODE_GAME, radioWork, radioLife, radioGame, radioAll));
        radioAll.setOnClickListener(v -> handleWorkModeChange(WORK_MODE_ALL, radioWork, radioLife, radioGame, radioAll));
    }
    
    private void clearAllRadioButtons(RadioButton... buttons) {
        for (RadioButton button : buttons) {
            button.setChecked(false);
        }
    }
    
    private void handleWorkModeChange(String newMode, RadioButton... buttons) {
        // 清除所有选择
        clearAllRadioButtons(buttons);
        
        // 设置当前选择
        if (WORK_MODE_WORK.equals(newMode)) {
            buttons[0].setChecked(true);
        } else if (WORK_MODE_LIFE.equals(newMode)) {
            buttons[1].setChecked(true);
        } else if (WORK_MODE_GAME.equals(newMode)) {
            buttons[2].setChecked(true);
        } else {
            buttons[3].setChecked(true);
        }
        
        // 保存设置
        String currentMode = shardPerfenceSetting.getWorkMode();
        if (!newMode.equals(currentMode)) {
            shardPerfenceSetting.setWorkMode(newMode);
            Toast.makeText(requireContext(), getString(R.string.work_mode_changed), Toast.LENGTH_SHORT).show();
            
            // 更新MainActivity中的菜单显示
            MainActivity activity = (MainActivity) requireActivity();
            activity.filterMenuItemsByWorkMode();
        }
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
                new ThemeItem("Pokemon", "pikachu", SplashActivity.pikachuImages[1], R.drawable.bg_setting_card_background_pikaqiu),
                new ThemeItem("Pokemon", "squirtle", SplashActivity.squirtleImages[3], R.drawable.bg_setting_card_background_squirtle),
                new ThemeItem("Character", "winter", SplashActivity.winterImages[0], R.drawable.bg_setting_card_background_winter),
                new ThemeItem("Character", "maple", SplashActivity.mapleImages[2], R.drawable.bg_setting_card_background_maple),
                new ThemeItem("Pokemon", "gengar", SplashActivity.gengarImages[4], R.drawable.bg_setting_card_background_gengar),
                new ThemeItem("Cutey", "capoo", SplashActivity.capooImages[2], R.drawable.bg_setting_card_background_capoo),
                new ThemeItem("Pokemon", "bulbasaur", SplashActivity.bulbasaurImages[2], R.drawable.bg_setting_card_background_bulbasaur),
                new ThemeItem("Pokemon", "mew", SplashActivity.mewImages[0], R.drawable.bg_setting_card_background_mew),
                new ThemeItem("Legends", "karsa", SplashActivity.karsaImages[5], R.drawable.bg_setting_card_background_karsa)
        );
    }

    public void loadRandomTheme(View view) {
        randomThemeSwitch = view.findViewById(R.id.switch_random_theme);

        // 设置当前随机主题状态
        boolean isRandomTheme = shardPerfenceSetting.getRandomTheme();
        randomThemeSwitch.setChecked(isRandomTheme);

        // 监听切换事件
        randomThemeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            shardPerfenceSetting.setRandomTheme(isChecked);
            
            // 如果开启随机主题，则禁用主题选择器；如果关闭随机主题，则启用主题选择器
            if (themeRecyclerView != null) {
                themeRecyclerView.setEnabled(!isChecked);
                themeRecyclerView.setAlpha(isChecked ? 0.5f : 1.0f);
                
                // 遍历RecyclerView的子项，设置点击事件的启用状态
                for (int i = 0; i < themeRecyclerView.getChildCount(); i++) {
                    View child = themeRecyclerView.getChildAt(i);
                    child.setEnabled(!isChecked);
                }
            }
            
            // 更新ThemeAdapter的状态
            if (themeAdapter != null) {
                themeAdapter.updateRandomThemeState();
            }
            
            if (isChecked) {
                Toast.makeText(requireContext(), "已开启随机主题，下次启动应用时将随机选择主题", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "已关闭随机主题", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化时设置主题选择器的状态
        if (themeRecyclerView != null) {
            themeRecyclerView.setEnabled(!isRandomTheme);
            themeRecyclerView.setAlpha(isRandomTheme ? 0.5f : 1.0f);
        }

        // 使用post方法确保RecyclerView完全初始化后再设置状态
        randomThemeSwitch.post(() -> {
            if (themeRecyclerView != null) {
                themeRecyclerView.setEnabled(!isRandomTheme);
                themeRecyclerView.setAlpha(isRandomTheme ? 0.5f : 1.0f);
            }
        });
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

    private void setupBottomMenuSizeSlider(View view) {
        bottomMenuSizeSeekBar = view.findViewById(R.id.bottom_menu_size_seekbar);
        bottomMenuSizeValueText = view.findViewById(R.id.bottom_menu_size_value);

        // 获取保存的值
        int savedValue = shardPerfenceSetting.getBottomMenuItemSize();
        android.util.Log.d("liziluo","bottomMenuItemSize savedValue: " + savedValue);
        // SeekBar的范围是0-60，对应实际大小60-120dp
        bottomMenuSizeSeekBar.setProgress(savedValue - 60);
        updateBottomMenuSizeText(savedValue);

        bottomMenuSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将SeekBar进度(0-60)转换为实际大小(60-120dp)
                int actualSize = progress + 60;
                updateBottomMenuSizeText(actualSize);
                // 保存当前值
                android.util.Log.d("liziluo","bottomMenuItemSize: " + actualSize);
                shardPerfenceSetting.setBottomMenuItemSize(actualSize);
                
                // 如果当前是首页，刷新底部菜单大小
                if (getActivity() != null) {
                    Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.content_frame);
                    if (currentFragment instanceof HomeFragment) {
                        ((HomeFragment) currentFragment).refreshBottomMenuSize();
                    }
                }
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

    private void updateBottomMenuSizeText(int value) {
        bottomMenuSizeValueText.setText(String.format(getString(R.string.bottom_menu_item_size_current), value));
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