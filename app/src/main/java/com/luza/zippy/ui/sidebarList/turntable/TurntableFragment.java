package com.luza.zippy.ui.sidebarList.turntable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.TurntableView;

import java.util.ArrayList;
import java.util.List;

import android.animation.ValueAnimator;

public class TurntableFragment extends BaseFragment {
    private TurntableView turntableView;
    private MaterialButton startButton;
    private MaterialButton selectPresupposeButton;
    private MaterialButton editPresupposeButton;
    private MaterialButton sifterButton;
    private MaterialButton saveButton;
    private MaterialButton btnCoin;
    private TextView currentPresupposeText;
    private List<String> options = new ArrayList<>();
    private List<String> originalOptions = new ArrayList<>();
    private List<String> removedOptions = new ArrayList<>();
    private TurntableDbHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "turntable_prefs";
    private static final String PREF_CURRENT_PRESUPPOSE_ID = "current_presuppose_id";
    private Handler handler = new Handler();
    private static final int REMOVE_DELAY_MS = 3000; // 3秒延迟

    @Override
    protected String getTitle() {
        return getString(R.string.menu_turntable);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_turntable, container, false);
    }


    @Override
    protected void initViews(View view) {
        turntableView = view.findViewById(R.id.turntable_view);
        startButton = view.findViewById(R.id.btn_start);
        selectPresupposeButton = view.findViewById(R.id.btn_select_presuppose);
        editPresupposeButton = view.findViewById(R.id.btn_edit_presuppose);
        currentPresupposeText = view.findViewById(R.id.text_current_presuppose);
        com.google.android.material.switchmaterial.SwitchMaterial autoRemoveSwitch = 
            view.findViewById(R.id.switch_auto_remove);
        sifterButton = view.findViewById(R.id.btn_sifter);
        saveButton = view.findViewById(R.id.btn_save);
        btnCoin = view.findViewById(R.id.btn_coin);

        // 初始化数据库和SharedPreferences
        dbHelper = new TurntableDbHelper(requireContext());
        dbHelper.insertDefaultData(); // 插入默认数据
        preferences = requireContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // 加载上次选择的预设
        loadLastPresuppose();

        // 开始按钮点击事件
        startButton.setOnClickListener(v -> {
            if (options.size() < 2) {
                Toast.makeText(requireContext(), R.string.turntable_min_options, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!turntableView.isRotating()) {
                turntableView.startRotation();
                startButton.setEnabled(false);
            }
        });

        // 选择预设按钮点击事件
        selectPresupposeButton.setOnClickListener(v -> showSelectPresupposeDialog());

        // 编辑预设按钮点击事件
        editPresupposeButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.content_frame, new TurntablePresupposeFragment())
                .addToBackStack(null)
                .commit();
        });

        // 筛子按钮点击事件
        sifterButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.content_frame, new TurntableSifterFragment())
                .addToBackStack(null)
                .commit();
        });

        // 保存按钮点击事件
        saveButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )
                .replace(R.id.content_frame, new TurntableTouchFragment())
                .addToBackStack(null)
                .commit();
        });

        // 添加硬币按钮点击事件
        btnCoin.setOnClickListener(v -> {
            // 创建硬币Fragment
            TurntableCoinFragment coinFragment = new TurntableCoinFragment();
            
            // 使用FragmentManager进行Fragment切换
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_right,  // 进入动画
                    R.anim.slide_out_left,   // 退出动画
                    R.anim.slide_in_left,    // 返回时进入动画
                    R.anim.slide_out_right   // 返回时退出动画
                )
                .replace(R.id.content_frame, coinFragment)  // 使用 content_frame 作为容器
                .addToBackStack(null)  // 添加到返回栈，这样按返回键可以回到上一个Fragment
                .commit();
        });

        // 设置转盘结果监听器
        turntableView.setOnRotationEndListener(selectedOption -> {
            if (isAdded() && !isDetached() && getContext() != null) {
                Toast.makeText(getContext(), 
                    String.format(getString(R.string.turntable_result), selectedOption), 
                    Toast.LENGTH_SHORT).show();
                
                // 如果开关打开，延迟移除选中的选项
                if (autoRemoveSwitch.isChecked() && options.size() > 1) {
                    // 显示倒计时提示
                    Toast.makeText(getContext(), "3秒后移除选中项...", Toast.LENGTH_SHORT).show();
                    
                    handler.postDelayed(() -> {
                        if (isAdded() && !isDetached() && getContext() != null) {
                            // 创建动画
                            android.view.animation.Animation fadeOut = new android.view.animation.AlphaAnimation(1, 0);
                            fadeOut.setDuration(500);
                            fadeOut.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(android.view.animation.Animation animation) {
                                    // 禁用开始按钮，防止动画过程中重复点击
                                    if (startButton != null) {
                                        startButton.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onAnimationEnd(android.view.animation.Animation animation) {
                                    if (isAdded() && !isDetached() && getContext() != null) {
                                        // 动画结束后移除选项
                                        options.remove(selectedOption);
                                        removedOptions.add(selectedOption);
                                        updateTurntableOptions();
                                        
                                        // 添加淡入动画
                                        android.view.animation.Animation fadeIn = new android.view.animation.AlphaAnimation(0, 1);
                                        fadeIn.setDuration(500);
                                        fadeIn.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(android.view.animation.Animation animation) {}

                                            @Override
                                            public void onAnimationEnd(android.view.animation.Animation animation) {
                                                // 动画结束后重新启用开始按钮
                                                if (startButton != null) {
                                                    startButton.setEnabled(true);
                                                }
                                            }

                                            @Override
                                            public void onAnimationRepeat(android.view.animation.Animation animation) {}
                                        });
                                        turntableView.startAnimation(fadeIn);
                                    }
                                }

                                @Override
                                public void onAnimationRepeat(android.view.animation.Animation animation) {}
                            });
                            
                            // 开始淡出动画
                            turntableView.startAnimation(fadeOut);
                        }
                    }, REMOVE_DELAY_MS);
                } else {
                    // 如果不需要移除选项，直接启用开始按钮
                    if (startButton != null) {
                        startButton.setEnabled(true);
                    }
                }
            }
        });

        // 更新转盘选项
        updateTurntableOptions();

        // 添加开关状态改变监听器
        autoRemoveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 更新开关的视觉状态
            updateSwitchAppearance(autoRemoveSwitch, isChecked);
            
            if (!isChecked) {
                // 当开关关闭时，恢复原始选项
                options = new ArrayList<>(originalOptions);
                removedOptions.clear();
                updateTurntableOptions();
            }
        });
        
        // 初始化开关的视觉状态
        updateSwitchAppearance(autoRemoveSwitch, autoRemoveSwitch.isChecked());
    }

    // 更新开关的视觉状态
    private void updateSwitchAppearance(SwitchMaterial switchMaterial, boolean isChecked) {
        if (isChecked) {
            switchMaterial.setAlpha(1.0f);
            switchMaterial.setTextColor(getResources().getColor(R.color.blue_500));
            switchMaterial.setBackgroundResource(R.drawable.bg_rounded_card);
        } else {
            switchMaterial.setAlpha(0.6f);
            switchMaterial.setTextColor(getResources().getColor(R.color.gray_500));
            switchMaterial.setBackgroundResource(R.drawable.bg_rounded_card);
        }
    }

    // 显示选择预设对话框
    private void showSelectPresupposeDialog() {
        List<TurntablePresuppose> presupposeList = dbHelper.getAllPresuppose();
        if (presupposeList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.turntable_presuppose_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_select_presuppose, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_presuppose);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(R.string.turntable_select_title)
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // 创建适配器
        SelectPresupposeAdapter adapter = new SelectPresupposeAdapter(presupposeList, presuppose -> {
            options = new ArrayList<>(presuppose.getOptions());
            originalOptions = new ArrayList<>(presuppose.getOptions());
            removedOptions.clear();
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                presuppose.getName()));
            updateTurntableOptions();
            preferences.edit().putLong(PREF_CURRENT_PRESUPPOSE_ID, presuppose.getId()).apply();
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    // 加载上次选择的预设
    private void loadLastPresuppose() {
        long presupposeId = preferences.getLong(PREF_CURRENT_PRESUPPOSE_ID, -1);
        TurntablePresuppose presuppose = null;
        
        if (presupposeId != -1) {
            presuppose = dbHelper.getPresuppose(presupposeId);
        }
        
        if (presuppose == null) {
            List<TurntablePresuppose> presupposeList = dbHelper.getAllPresuppose();
            if (!presupposeList.isEmpty()) {
                presuppose = presupposeList.get(0);
                preferences.edit().putLong(PREF_CURRENT_PRESUPPOSE_ID, presuppose.getId()).apply();
            }
        }

        if (presuppose != null) {
            options = new ArrayList<>(presuppose.getOptions());
            originalOptions = new ArrayList<>(presuppose.getOptions());
            removedOptions.clear();
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                presuppose.getName()));
            updateTurntableOptions();
        } else {
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                getString(R.string.turntable_no_presuppose)));
            updateTitleWithOptionsCount();
        }
    }

    // 更新转盘选项
    private void updateTurntableOptions() {
        if (turntableView != null) {
            turntableView.setOptions(options);
            updateTitleWithOptionsCount();
        }
    }

    // 更新标题显示选项数量
    private void updateTitleWithOptionsCount() {
        if (currentPresupposeText != null) {
            String currentText = currentPresupposeText.getText().toString();
            // 移除可能存在的旧的数量显示
            int bracketIndex = currentText.lastIndexOf("(");
            if (bracketIndex != -1) {
                currentText = currentText.substring(0, bracketIndex).trim();
            }
            // 添加新的数量显示
            currentPresupposeText.setText(String.format("%s (%d)", currentText, options.size()));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        // 移除所有待执行的延迟任务
        handler.removeCallbacksAndMessages(null);
    }

    // 选择预设适配器
    private static class SelectPresupposeAdapter extends RecyclerView.Adapter<SelectPresupposeAdapter.ViewHolder> {
        private final List<TurntablePresuppose> presupposeList;
        private final OnPresupposeSelectedListener listener;

        public SelectPresupposeAdapter(List<TurntablePresuppose> presupposeList, OnPresupposeSelectedListener listener) {
            this.presupposeList = presupposeList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TurntablePresuppose presuppose = presupposeList.get(position);
            holder.textView.setText(presuppose.getName());
            holder.itemView.setOnClickListener(v -> listener.onPresupposeSelected(presuppose));
        }

        @Override
        public int getItemCount() {
            return presupposeList.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            ViewHolder(View view) {
                super(view);
                textView = (TextView) view;
                textView.setPadding(32, 24, 32, 24);
            }
        }
    }

    // 预设选择监听器接口
    interface OnPresupposeSelectedListener {
        void onPresupposeSelected(TurntablePresuppose presuppose);
    }
}