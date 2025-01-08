package com.luza.zippy.ui.sidebarList.foodRecord;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.luza.zippy.R;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;
import com.luza.zippy.ui.base.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class FoodRecordFragment extends BaseFragment {
    private FoodRecordViewModel viewModel;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private FoodRecordAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_record, container, false);
        viewModel = new ViewModelProvider(this).get(FoodRecordViewModel.class);
        initViews(view);
        return view;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_food_record);
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_food);
        emptyText = view.findViewById(R.id.text_empty);
        FloatingActionButton fabAddFood = view.findViewById(R.id.fab_add_food);
        FloatingActionButton fabManagePreset = view.findViewById(R.id.fab_manage_preset);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FoodRecordAdapter(new FoodRecordAdapter.FoodRecordDiff());
        recyclerView.setAdapter(adapter);

        // 观察数据变化
        viewModel.getAllFoodRecords().observe(getViewLifecycleOwner(), foodRecords -> {
            adapter.submitList(foodRecords);
            emptyText.setVisibility(foodRecords.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 添加按钮点击事件
        fabAddFood.setOnClickListener(v -> showAddFoodDialog());
        fabManagePreset.setOnClickListener(v -> showPresetManageDialog());

        // 设置点击事件
        adapter.setOnItemClickListener(new FoodRecordAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(FoodRecord foodRecord) {
                showEditDialog(foodRecord);
            }

            @Override
            public void onServingsChanged(FoodRecord foodRecord) {
                viewModel.update(foodRecord);
            }
        });

        // 初始化预设数据
        viewModel.initializeIfNeeded();

        // 设置保存记录按钮点击事件
        view.findViewById(R.id.btn_save_record).setOnClickListener(v -> showSaveRecordDialog());

        // 设置查看历史按钮点击事件
        view.findViewById(R.id.btn_view_history).setOnClickListener(v -> showHistoryDialog());
    }

    private void showAddFoodDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_food_record, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        // 设置标题
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(R.string.food_add);

        TextInputEditText nameEdit = dialogView.findViewById(R.id.edit_food_name);
        TextInputEditText servingsEdit = dialogView.findViewById(R.id.edit_servings);
        MaterialAutoCompleteTextView presetSpinner = dialogView.findViewById(R.id.spinner_food_preset);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        // 设置按钮文本
        positiveButton.setText(R.string.food_save);
        negativeButton.setText(R.string.cancel);

        // 设置预设选择器
        viewModel.getAllFoodPresets().observe(getViewLifecycleOwner(), presets -> {
            List<String> presetNames = new ArrayList<>();
            presetNames.add(getString(R.string.food_preset_select));
            for (FoodPreset preset : presets) {
                presetNames.add(preset.getFoodName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.item_dropdown_menu,
                presetNames
            );
            presetSpinner.setAdapter(adapter);
        });

        // 预设选择监听
        presetSpinner.setOnItemClickListener((parent, view, position, id) -> {
            if (position > 0) {
                String selectedPreset = parent.getItemAtPosition(position).toString();
                nameEdit.setText(selectedPreset);
                servingsEdit.setText("1");
            }
        });

        AlertDialog dialog = builder.create();

        // 设置按钮点击事件
        positiveButton.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String servingsStr = servingsEdit.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.food_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (servingsStr.isEmpty()) {
                Toast.makeText(requireContext(), R.string.food_servings_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            int servings = Integer.parseInt(servingsStr);
            viewModel.insert(new FoodRecord(name, servings));
            Toast.makeText(requireContext(), R.string.food_add_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        // 设置窗口大小和动画
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                );
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        dialog.show();
    }

    private void showEditDialog(FoodRecord foodRecord) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_food_record, null);
        builder.setView(dialogView);
        builder.setTitle(R.string.food_edit);

        TextInputEditText nameEdit = dialogView.findViewById(R.id.edit_food_name);
        TextInputEditText servingsEdit = dialogView.findViewById(R.id.edit_servings);

        // 填充现有数据
        nameEdit.setText(foodRecord.getFoodName());
        servingsEdit.setText(String.valueOf(foodRecord.getServings()));

        builder.setPositiveButton(R.string.food_save, null); // 先设置为null，后面重新设置监听器
        builder.setNegativeButton(R.string.food_cancel, null);
        builder.setNeutralButton(R.string.food_delete, null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // 设置按钮颜色为黑色
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(getResources().getColor(android.R.color.black));

        // 重新设置Positive按钮的点击监听器（这样可以避免在输入验证失败时对话框自动关闭）
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String servingsStr = servingsEdit.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), R.string.food_name_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (servingsStr.isEmpty()) {
                Toast.makeText(requireContext(), R.string.food_servings_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            int servings = Integer.parseInt(servingsStr);
            foodRecord.setFoodName(name);
            foodRecord.setServings(servings);

            viewModel.update(foodRecord);
            Toast.makeText(requireContext(), R.string.food_edit_success, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        // 设置删除按钮的点击监听器
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.food_delete)
                    .setMessage(R.string.food_delete_confirm)
                    .setPositiveButton(R.string.confirm, (dialogInterface, which) -> {
                        viewModel.delete(foodRecord);
                        Toast.makeText(requireContext(), R.string.food_delete_success, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void showPresetManageDialog() {
        // 创建加载对话框
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setMessage("加载中...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // 获取预设数据
        viewModel.getAllFoodPresets().observe(getViewLifecycleOwner(), new androidx.lifecycle.Observer<List<FoodPreset>>() {
            @Override
            public void onChanged(List<FoodPreset> presets) {
                viewModel.getAllFoodPresets().removeObserver(this);
                loadingDialog.dismiss();
                showPresetDialog(presets);
            }
        });
    }

    private void showPresetDialog(List<FoodPreset> presets) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_preset_manage, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        // 设置标题
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(R.string.food_preset_manage);

        // 设置空状态文本
        TextView emptyText = dialogView.findViewById(R.id.text_empty);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_presets);
        Button addButton = dialogView.findViewById(R.id.btn_add);

        AlertDialog presetDialog = builder.create(); // 先创建对话框

        if (presets.isEmpty()) {
            emptyText.setText(R.string.food_preset_empty);
            emptyText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            // 设置RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            PresetAdapter adapter = new PresetAdapter(presets, preset -> {
                // 点击预设项时显示删除确认对话框
                AlertDialog deleteDialog = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog)
                        .setTitle(R.string.food_preset_delete)
                        .setMessage(getString(R.string.food_preset_delete_confirm, preset.getFoodName()))
                        .setPositiveButton(R.string.confirm, (confirmDialog, w) -> {
                            viewModel.deletePreset(preset);
                            Toast.makeText(requireContext(), R.string.food_preset_delete_success, Toast.LENGTH_SHORT).show();
                            viewModel.triggerReload();
                            presetDialog.dismiss(); // 使用外部定义的presetDialog
                            showPresetManageDialog();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .create();

                deleteDialog.show();

                // 设置删除确认对话框按钮颜色
                deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(android.R.color.black));
                deleteDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.black));
            });
            recyclerView.setAdapter(adapter);
        }

        // 添加按钮点击事件
        addButton.setOnClickListener(v -> {
            presetDialog.dismiss();
            showAddPresetDialog();
        });

        // 设置窗口大小和动画
        presetDialog.setOnShowListener(dialogInterface -> {
            Window window = presetDialog.getWindow();
            if (window != null) {
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                );
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        presetDialog.show();
    }

    private void showAddPresetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_preset, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        // 设置标题
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText(R.string.food_preset_add);

        TextInputEditText input = dialogView.findViewById(R.id.edit_preset_name);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        // 设置按钮文本
        positiveButton.setText(R.string.confirm);
        negativeButton.setText(R.string.cancel);

        AlertDialog dialog = builder.create();

        // 设置按钮点击事件
        positiveButton.setOnClickListener(v -> {
            String name = input.getText().toString().trim();
            if (!name.isEmpty()) {
                // 检查是否已存在相同名称的预设
                viewModel.getAllFoodPresets().observe(getViewLifecycleOwner(), new Observer<List<FoodPreset>>() {
                    @Override
                    public void onChanged(List<FoodPreset> presets) {
                        viewModel.getAllFoodPresets().removeObserver(this);
                        
                        boolean isDuplicate = false;
                        for (FoodPreset preset : presets) {
                            if (preset.getFoodName().equals(name)) {
                                isDuplicate = true;
                                break;
                            }
                        }

                        if (isDuplicate) {
                            Toast.makeText(requireContext(), "已存在相同名称的预设", Toast.LENGTH_SHORT).show();
                        } else {
                            viewModel.insertPreset(new FoodPreset(name));
                            dialog.dismiss();
                            viewModel.triggerReload();
                            showPresetManageDialog();
                        }
                    }
                });
            }
        });

        negativeButton.setOnClickListener(v -> {
            dialog.dismiss();
            showPresetManageDialog();
        });

        // 设置窗口大小和动画
        dialog.setOnShowListener(dialogInterface -> {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                );
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                window.setBackgroundDrawableResource(android.R.color.transparent);
            }
        });

        dialog.show();

        // 自动显示软键盘
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 200);
    }

    private void showSaveRecordDialog() {
        if (adapter.getCurrentList().isEmpty()) {
            Toast.makeText(requireContext(), "没有可保存的记录", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_record, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText("保存记录");

        TextInputEditText nameEdit = dialogView.findViewById(R.id.edit_record_name);
        Button positiveButton = dialogView.findViewById(R.id.btn_positive);
        Button negativeButton = dialogView.findViewById(R.id.btn_negative);

        positiveButton.setText(R.string.confirm);
        negativeButton.setText(R.string.cancel);

        AlertDialog dialog = builder.create();

        positiveButton.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "请输入记录名称", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.saveCurrentRecords(name);
            Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showHistoryDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_history_list, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.RoundedDialog);
        builder.setView(dialogView);

        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        titleView.setText("历史记录");

        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_history);
        TextView emptyText = dialogView.findViewById(R.id.text_empty);
        Button closeButton = dialogView.findViewById(R.id.btn_close);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        HistoryAdapter adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        AlertDialog dialog = builder.create();

        viewModel.getAllHistories().observe(getViewLifecycleOwner(), histories -> {
            if (histories.isEmpty()) {
                emptyText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.submitList(histories);
            }
        });

        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}