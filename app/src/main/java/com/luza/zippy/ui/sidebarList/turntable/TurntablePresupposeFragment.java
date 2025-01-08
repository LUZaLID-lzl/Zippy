package com.luza.zippy.ui.sidebarList.turntable;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TurntablePresupposeFragment extends BaseFragment {
    private RecyclerView recyclerPresuppose;
    private RecyclerView recyclerOptions;
    private TextView textEmpty;
    private MaterialButton btnAddPresuppose;
    private PresupposeAdapter presupposeAdapter;
    private List<TurntablePresuppose> presupposeList;
    private TurntableDbHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turntable_presuppose, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_turntable_presuppose);
    }

    @Override
    protected void initViews(View view) {
        recyclerPresuppose = view.findViewById(R.id.recycler_presuppose);
        textEmpty = view.findViewById(R.id.text_empty);
        btnAddPresuppose = view.findViewById(R.id.btn_add_presuppose);

        // 初始化数据库
        dbHelper = new TurntableDbHelper(requireContext());

        // 初始化列表
        presupposeList = dbHelper.getAllPresuppose();
        presupposeAdapter = new PresupposeAdapter(presupposeList);
        recyclerPresuppose.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerPresuppose.setAdapter(presupposeAdapter);

        // 添加预设按钮点击事件
        btnAddPresuppose.setOnClickListener(v -> showEditDialog(null));

        // 更新空状态显示
        updateEmptyState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // 显示编辑对话框
    private void showEditDialog(TurntablePresuppose presuppose) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_presuppose_edit, null);
        TextInputEditText inputName = dialogView.findViewById(R.id.input_name);
        recyclerOptions = dialogView.findViewById(R.id.recycler_options);
        MaterialButton btnAddOption = dialogView.findViewById(R.id.btn_add_option);

        // 设置选项列表
        OptionsAdapter optionsAdapter = new OptionsAdapter();
        recyclerOptions.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOptions.setAdapter(optionsAdapter);

        // 如果是编辑模式，填充现有数据
        if (presuppose != null) {
            inputName.setText(presuppose.getName());
            optionsAdapter.setOptions(new ArrayList<>(presuppose.getOptions()));
        } else {
            // 添加一个空选项
            optionsAdapter.addOption("");
        }

        // 添加选项按钮点击事件
        btnAddOption.setOnClickListener(v -> optionsAdapter.addOption(""));

        // 创建对话框
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(presuppose == null ? R.string.turntable_presuppose_add : R.string.turntable_presuppose_edit)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    String name = inputName.getText().toString().trim();
                    List<String> options = optionsAdapter.getOptions();
                    
                    if (name.isEmpty()) {
                        return;
                    }

                    // 确保至少有一个有效选项
                    if (options.isEmpty()) {
                        String currentText = optionsAdapter.getCurrentOptionText();
                        if (!currentText.trim().isEmpty()) {
                            options = new ArrayList<>();
                            options.add(currentText.trim());
                        } else {
                            return;
                        }
                    }

                    if (presuppose == null) {
                        // 添加新预设
                        TurntablePresuppose newPresuppose = new TurntablePresuppose(name, options);
                        long id = dbHelper.addPresuppose(newPresuppose);
                        newPresuppose.setId(id);
                        presupposeList.add(newPresuppose);
                        presupposeAdapter.notifyItemInserted(presupposeList.size() - 1);
                    } else {
                        // 更新现有预设
                        presuppose.setName(name);
                        presuppose.setOptions(options);
                        dbHelper.updatePresuppose(presuppose.getId(), presuppose);
                        presupposeAdapter.notifyDataSetChanged();
                    }
                    updateEmptyState();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        // 设置按钮颜色
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black, null));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.black, null));
        });

        dialog.show();
    }

    // 更新空状态显示
    private void updateEmptyState() {
        if (presupposeList.isEmpty()) {
            textEmpty.setVisibility(View.VISIBLE);
            recyclerPresuppose.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerPresuppose.setVisibility(View.VISIBLE);
        }
    }

    // 预设适配器
    private class PresupposeAdapter extends RecyclerView.Adapter<PresupposeAdapter.ViewHolder> {
        private final List<TurntablePresuppose> presupposeList;

        public PresupposeAdapter(List<TurntablePresuppose> presupposeList) {
            this.presupposeList = presupposeList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_turntable_presuppose, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TurntablePresuppose presuppose = presupposeList.get(position);
            holder.textName.setText(presuppose.getName());

            // 点击预设项
            holder.itemView.setOnClickListener(v -> showEditDialog(presuppose));

            // 编辑按钮
            holder.btnEdit.setOnClickListener(v -> showEditDialog(presuppose));

            // 删除按钮
            holder.btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                        .setTitle(R.string.turntable_presuppose_delete)
                        .setMessage(getString(R.string.delete_confirm, presuppose.getName()))
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            dbHelper.deletePresuppose(presuppose.getId());
                            presupposeList.remove(position);
                            notifyDataSetChanged();
                            updateEmptyState();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create()
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return presupposeList != null ? presupposeList.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textName;
            ImageButton btnEdit;
            ImageButton btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.text_presuppose_name);
                btnEdit = itemView.findViewById(R.id.btn_edit);
                btnDelete = itemView.findViewById(R.id.btn_delete);
            }
        }
    }

    // 选项适配器
    private class OptionsAdapter extends RecyclerView.Adapter<OptionsAdapter.ViewHolder> {
        private List<String> options = new ArrayList<>();
        private int newItemPosition = -1;  // 新添加项的位置

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_presuppose_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            // 移除之前的文本监听器
            holder.inputOption.removeTextChangedListener(holder.textWatcher);
            
            // 设置当前位置的文本
            holder.inputOption.setText(options.get(position));
            
            // 如果是新添加的项，请求焦点
            if (position == newItemPosition) {
                holder.inputOption.post(() -> {
                    holder.inputOption.requestFocus();
                    // 将光标移到文本末尾
                    holder.inputOption.setSelection(holder.inputOption.length());
                });
                newItemPosition = -1;  // 重置标记
            }
            
            // 创建并设置新的文本监听器
            holder.textWatcher = new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(android.text.Editable s) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < options.size()) {
                        options.set(adapterPosition, s.toString());
                    }
                }
            };
            holder.inputOption.addTextChangedListener(holder.textWatcher);

            // 删除按钮
            holder.btnDelete.setOnClickListener(v -> {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION && options.size() > 1) {
                    options.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, options.size());
                }
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        void setOptions(List<String> newOptions) {
            this.options = new ArrayList<>(newOptions);
            if (this.options.isEmpty()) {
                this.options.add("");
            }
            notifyDataSetChanged();
        }

        List<String> getOptions() {
            List<String> validOptions = new ArrayList<>();
            for (String option : options) {
                String trimmed = option.trim();
                if (!trimmed.isEmpty()) {
                    validOptions.add(trimmed);
                }
            }
            return validOptions;
        }

        void addOption(String option) {
            newItemPosition = options.size();  // 记录新项的位置
            options.add(option);
            notifyItemInserted(newItemPosition);
            // 使用外部类的recyclerOptions变量
            if (recyclerOptions != null) {
                recyclerOptions.post(() -> {
                    // 确保位置有效且RecyclerView已经完成布局
                    if (newItemPosition >= 0 && newItemPosition < options.size()) {
                        try {
                            recyclerOptions.smoothScrollToPosition(newItemPosition);
                        } catch (IllegalArgumentException e) {
                            // 如果滚动失败，尝试使用scrollToPosition
                            recyclerOptions.scrollToPosition(newItemPosition);
                        }
                    }
                });
            }
        }

        String getCurrentOptionText() {
            if (!options.isEmpty()) {
                return options.get(0);
            }
            return "";
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextInputEditText inputOption;
            ImageButton btnDelete;
            android.text.TextWatcher textWatcher;

            ViewHolder(View itemView) {
                super(itemView);
                inputOption = itemView.findViewById(R.id.input_option);
                btnDelete = itemView.findViewById(R.id.btn_delete_option);
            }
        }
    }
}

// 预设数据类
class TurntablePresuppose {
    private long id;
    private String name;
    private List<String> options;

    public TurntablePresuppose(String name, List<String> options) {
        this.name = name;
        this.options = options;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}