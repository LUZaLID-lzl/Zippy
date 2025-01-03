package com.luza.zippy.ui.sidebarList.turntable;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.ui.views.TurntableView;

import java.util.ArrayList;
import java.util.List;

public class TurntableFragment extends BaseFragment {
    private TurntableView turntableView;
    private MaterialButton startButton;
    private MaterialButton selectPresupposeButton;
    private MaterialButton editPresupposeButton;
    private TextView currentPresupposeText;
    private List<String> options = new ArrayList<>();
    private TurntableDbHelper dbHelper;
    private SharedPreferences preferences;
    private static final String PREF_NAME = "turntable_prefs";
    private static final String PREF_CURRENT_PRESUPPOSE_ID = "current_presuppose_id";

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

        // 设置转盘结果监听器
        turntableView.setOnRotationEndListener(selectedOption -> {
            if (isAdded() && !isDetached() && getContext() != null) {
                Toast.makeText(getContext(), 
                    String.format(getString(R.string.turntable_result), selectedOption), 
                    Toast.LENGTH_SHORT).show();
                if (startButton != null) {
                    startButton.setEnabled(true);
                }
            }
        });

        // 更新转盘选项
        updateTurntableOptions();
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
            // 选择预设后的处理
            options = new ArrayList<>(presuppose.getOptions());
            updateTurntableOptions();
            // 保存当前选择的预设ID
            preferences.edit().putLong(PREF_CURRENT_PRESUPPOSE_ID, presuppose.getId()).apply();
            // 更新显示的预设名称
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                presuppose.getName()));
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
            // 如果上次选择的预设不存在，获取第一个预设
            List<TurntablePresuppose> presupposeList = dbHelper.getAllPresuppose();
            if (!presupposeList.isEmpty()) {
                presuppose = presupposeList.get(0);
                // 保存新选择的预设ID
                preferences.edit().putLong(PREF_CURRENT_PRESUPPOSE_ID, presuppose.getId()).apply();
            }
        }

        if (presuppose != null) {
            options = new ArrayList<>(presuppose.getOptions());
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                presuppose.getName()));
            updateTurntableOptions(); // 更新转盘选项
        } else {
            currentPresupposeText.setText(String.format(getString(R.string.turntable_current_presuppose), 
                getString(R.string.turntable_no_presuppose)));
        }
    }

    // 更新转盘选项
    private void updateTurntableOptions() {
        if (turntableView != null) {
            turntableView.setOptions(options);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
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