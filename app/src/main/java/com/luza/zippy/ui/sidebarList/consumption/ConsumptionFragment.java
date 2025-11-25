package com.luza.zippy.ui.sidebarList.consumption;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luza.zippy.R;
import com.luza.zippy.data.AppDatabase;
import com.luza.zippy.data.dao.ConsumptionDao;
import com.luza.zippy.data.entity.ConsumptionRecord;
import com.luza.zippy.ui.base.BaseFragment;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.utils.ConsumptionImageGenerator;
import com.luza.zippy.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class ConsumptionFragment extends BaseFragment {
    private static final int STORAGE_PERMISSION_CODE = 200;
    private ConsumptionDao consumptionDao;
    private ExecutorService executorService;
    private RecyclerView recyclerView;
    private ConsumptionAdapter adapter;
    private TextView totalAmountView;
    private TextView pendingAmountView;
    private TextView monthAmountView;
    private TextView lastPaybackTimeView;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final SimpleDateFormat groupDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat paybackTimeFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
    private View cardBackground;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        consumptionDao = AppDatabase.getInstance(requireContext()).consumptionDao();
        executorService = Executors.newSingleThreadExecutor();
        adapter = new ConsumptionAdapter();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_consumption);
    }

    @Override
    protected void initViews(View view) {

    }

    private void updateCardBackground() {
        String theme = ShardPerfenceSetting.getInstance(requireContext()).getHomeTheme();
        String backgroundName = "bg_setting_card_background_" + theme;
        int backgroundResId = getResources().getIdentifier(backgroundName, "drawable", requireContext().getPackageName());
        if (backgroundResId != 0 && cardBackground != null) {
            cardBackground.setBackgroundResource(backgroundResId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_consumption, container, false);

        // 获取卡片背景视图
        cardBackground = view.findViewById(R.id.card_background);
        updateCardBackground();

        recyclerView = view.findViewById(R.id.rv_consumption_records);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        totalAmountView = view.findViewById(R.id.tv_total_amount);
        pendingAmountView = view.findViewById(R.id.tv_pending_amount);
        monthAmountView = view.findViewById(R.id.tv_month_amount);
        lastPaybackTimeView = view.findViewById(R.id.tv_last_payback_time);

        FloatingActionButton fabRecord = view.findViewById(R.id.fab_record);
        FloatingActionButton fabPayback = view.findViewById(R.id.fab_payback);

        fabRecord.setOnClickListener(v -> showRecordDialog(false));
        fabPayback.setOnClickListener(v -> showPaybackConfirmDialog());

        // 添加测试按钮（仅用于开发测试）
        if (BuildConfig.DEBUG) {
            FloatingActionButton fabTest = view.findViewById(R.id.fab_test_image);
            if (fabTest != null) {
                fabTest.setVisibility(View.VISIBLE);
                fabTest.setOnClickListener(v -> testImageGeneration());
            }
        }

        // 观察数据变化
        consumptionDao.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                adapter.setRecords(records);
            }
        });

        consumptionDao.getTotalAmount().observe(getViewLifecycleOwner(), totalAmount -> {
            if (totalAmount != null) {
                totalAmountView.setText(String.format(Locale.getDefault(), "¥%.2f", totalAmount));
            } else {
                totalAmountView.setText("¥0.00");
            }
        });

        consumptionDao.getPendingAmount().observe(getViewLifecycleOwner(), pendingAmount -> {
            if (pendingAmount != null) {
                pendingAmountView.setText(String.format(Locale.getDefault(), "¥%.2f", pendingAmount));
            } else {
                pendingAmountView.setText("¥0.00");
            }
        });

        consumptionDao.getCurrentMonthAmount().observe(getViewLifecycleOwner(), monthAmount -> {
            if (monthAmount != null) {
                monthAmountView.setText(String.format(Locale.getDefault(), "¥%.2f", monthAmount));
            } else {
                monthAmountView.setText("¥0.00");
            }
        });

        consumptionDao.getLastPaybackTime().observe(getViewLifecycleOwner(), lastPaybackTime -> {
            if (lastPaybackTime != null) {
                lastPaybackTimeView.setText(paybackTimeFormat.format(lastPaybackTime));
            } else {
                lastPaybackTimeView.setText("从未归还");
            }
        });

        return view;
    }

    private void showRecordDialog(boolean isPayback) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_record_consumption);
        
        // 设置对话框宽度为屏幕宽度的90%
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            window.setAttributes(layoutParams);
        }

        RadioGroup purposeGroup = dialog.findViewById(R.id.rg_purpose);
        View otherPurposeLayout = dialog.findViewById(R.id.til_other_purpose);
        EditText otherPurposeEdit = dialog.findViewById(R.id.et_other_purpose);
        EditText amountEdit = dialog.findViewById(R.id.et_amount);
        RadioButton otherRadio = dialog.findViewById(R.id.rb_other);
        RadioButton groceriesRadio = dialog.findViewById(R.id.rb_groceries);
        RadioButton seasoningRadio = dialog.findViewById(R.id.rb_seasoning);
        RadioButton stapleRadio = dialog.findViewById(R.id.rb_staple);

        purposeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            otherPurposeLayout.setVisibility(checkedId == R.id.rb_other ? View.VISIBLE : View.GONE);
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String purpose;
            if (otherRadio.isChecked()) {
                purpose = otherPurposeEdit.getText().toString().trim();
                if (purpose.isEmpty()) {
                    Toast.makeText(requireContext(), "请输入用途", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else if (groceriesRadio.isChecked()) {
                purpose = "买菜";
            } else if (seasoningRadio.isChecked()) {
                purpose = "调料";
            } else if (stapleRadio.isChecked()) {
                purpose = "主食";
            } else {
                purpose = "其他";
            }

            String amountStr = amountEdit.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "请输入金额", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(requireContext(), "金额必须大于0", Toast.LENGTH_SHORT).show();
                    return;
                }

                ConsumptionRecord record = new ConsumptionRecord(
                        purpose,
                        amount,
                        System.currentTimeMillis(),
                        isPayback
                );

                executorService.execute(() -> {
                    consumptionDao.insert(record);
                });

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "请输入有效金额", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showPaybackConfirmDialog() {
        // 检查存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && 
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                STORAGE_PERMISSION_CODE);
            return;
        }
        
        // 先获取待归还记录和金额
        consumptionDao.getPendingRecords().observe(getViewLifecycleOwner(), pendingRecords -> {
            consumptionDao.getPendingAmount().observe(getViewLifecycleOwner(), pendingAmount -> {
                if (pendingRecords != null && pendingAmount != null && pendingAmount > 0) {
                    showPaybackDialogWithImage(pendingRecords, pendingAmount);
                } else {
                    Toast.makeText(requireContext(), "没有待归还的金额", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showPaybackDialogWithImage(List<ConsumptionRecord> pendingRecords, double pendingAmount) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认归还")
                .setMessage(String.format(Locale.getDefault(), 
                    "是否确认归还所有待归还金额？\n\n待归还金额: ¥%.2f\n记录数量: %d条\n\n归还后将自动生成消费清单图片并保存到相册。", 
                    pendingAmount, pendingRecords.size()))
                .setPositiveButton("确认归还", (dialog, which) -> {
                    // 先生成图片
                    executorService.execute(() -> {
                        try {
                            ConsumptionImageGenerator.generateAndSaveImage(requireContext(), pendingRecords, pendingAmount);
                            
                            // 在主线程显示成功提示
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "消费清单已保存到相册", Toast.LENGTH_LONG).show();
                            });
                        } catch (Exception e) {
                            android.util.Log.e("ConsumptionFragment", "生成图片失败", e);
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "生成图片失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                    
                    // 然后标记为已归还
                    executorService.execute(() -> {
                        consumptionDao.markAllAsPayback(System.currentTimeMillis());
                    });
                })
                .setNegativeButton("取消", null);

        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(android.R.color.black));
            negativeButton.setTextColor(getResources().getColor(android.R.color.black));
        });
        dialog.show();
    }

    private void showDeleteConfirmDialog(ConsumptionRecord record) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("确认删除")
                .setMessage("是否确认删除此条记录？")
                .setPositiveButton("确认", (dialog, which) -> {
                    executorService.execute(() -> {
                        consumptionDao.delete(record);
                    });
                })
                .setNegativeButton("取消", null);

        Dialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
            positiveButton.setTextColor(getResources().getColor(android.R.color.black));
            negativeButton.setTextColor(getResources().getColor(android.R.color.black));
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限获取成功，重新尝试显示归还对话框
                showPaybackConfirmDialog();
            } else {
                Toast.makeText(requireContext(), "需要存储权限才能保存消费清单图片", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void testImageGeneration() {
        // 创建测试数据
        List<ConsumptionRecord> testRecords = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        
        // 添加一些测试记录
        testRecords.add(new ConsumptionRecord("买菜", 25.50, currentTime - 86400000, false));
        testRecords.add(new ConsumptionRecord("调料", 15.80, currentTime - 86400000, false));
        testRecords.add(new ConsumptionRecord("主食", 32.00, currentTime - 172800000, false));
        testRecords.add(new ConsumptionRecord("买菜", 28.90, currentTime - 172800000, false));
        testRecords.add(new ConsumptionRecord("其他", 12.50, currentTime - 259200000, false));
        
        double totalAmount = testRecords.stream().mapToDouble(ConsumptionRecord::getAmount).sum();
        
        executorService.execute(() -> {
            try {
                ConsumptionImageGenerator.generateAndSaveImage(requireContext(), testRecords, totalAmount);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "测试图片已保存到相册", Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                android.util.Log.e("ConsumptionFragment", "测试图片生成失败", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "测试图片生成失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private static class ConsumptionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_DATE = 0;
        private static final int TYPE_RECORD = 1;
        private List<Object> items = new ArrayList<>();
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        private OnItemLongClickListener longClickListener;

        public interface OnItemLongClickListener {
            void onItemLongClick(ConsumptionRecord record);
        }

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.longClickListener = listener;
        }

        public void setRecords(List<ConsumptionRecord> records) {
            items.clear();
            items.addAll(records);
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof String ? TYPE_DATE : TYPE_RECORD;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_DATE) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_consumption_date, parent, false);
                return new DateViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_consumption_record, parent, false);
                return new RecordViewHolder(view, longClickListener, this);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DateViewHolder) {
                ((DateViewHolder) holder).bind((String) items.get(position));
            } else if (holder instanceof RecordViewHolder) {
                ((RecordViewHolder) holder).bind((ConsumptionRecord) items.get(position));
            }
        }

        @Override
        public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            holder.itemView.startAnimation(AnimationUtils.loadAnimation(
                    holder.itemView.getContext(), android.R.anim.fade_in));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class DateViewHolder extends RecyclerView.ViewHolder {
            private final TextView dateView;

            DateViewHolder(@NonNull View itemView) {
                super(itemView);
                dateView = itemView.findViewById(R.id.tv_date);
            }

            void bind(String date) {
                dateView.setText(date);
            }
        }

        static class RecordViewHolder extends RecyclerView.ViewHolder {
            TextView purposeView;
            TextView timeView;
            TextView amountView;
            ConsumptionAdapter adapter;
            private static final SimpleDateFormat recordTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

            RecordViewHolder(@NonNull View itemView, OnItemLongClickListener listener, ConsumptionAdapter adapter) {
                super(itemView);
                purposeView = itemView.findViewById(R.id.tv_purpose);
                timeView = itemView.findViewById(R.id.tv_time);
                amountView = itemView.findViewById(R.id.tv_amount);
                this.adapter = adapter;

                if (listener != null) {
                    itemView.setOnLongClickListener(v -> {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Object item = this.adapter.items.get(position);
                            if (item instanceof ConsumptionRecord) {
                                listener.onItemLongClick((ConsumptionRecord) item);
                                return true;
                            }
                        }
                        return false;
                    });
                }
            }

            void bind(ConsumptionRecord record) {
                purposeView.setText(record.getPurpose());
                timeView.setText(recordTimeFormat.format(new Date(record.getTimestamp())));
                amountView.setText(String.format(Locale.getDefault(),
                        "%s¥%.2f",
                        record.isPayback() ? "-" : "",
                        record.getAmount()));

                // 添加条目动画
                itemView.setAlpha(0f);
                itemView.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.setOnItemLongClickListener(this::showDeleteConfirmDialog);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}