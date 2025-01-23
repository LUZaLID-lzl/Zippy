package com.luza.zippy.ui.sidebarList.timer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimerFragment extends BaseFragment implements TimerService.TimerListener {
    private TextView timeText;
    private Button startButton, stopButton;
    private ImageButton switchButton;
    private TextView modeText;
    private TextInputLayout inputLayoutMinutes;
    private EditText editMinutes;
    private TextView recordCountText;
    private TimerService timerService;
    private boolean isBound = false;
    private boolean isRunning = false;
    private boolean isCountdownMode = false;
    private TextView textStatsMode;
    private TextView textStatsLongest;
    private TextView textStatsShortest;
    private TextView textStatsAverage;
    private TextView textStatsCount;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            timerService.setListener(TimerFragment.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            timerService = null;
            isBound = false;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);
        timeText = view.findViewById(R.id.text_time);
        startButton = view.findViewById(R.id.btn_start);
        stopButton = view.findViewById(R.id.btn_stop);
        switchButton = view.findViewById(R.id.btn_switch_mode);
        modeText = view.findViewById(R.id.text_mode);
        inputLayoutMinutes = view.findViewById(R.id.input_layout_minutes);
        editMinutes = view.findViewById(R.id.edit_minutes);
        recordCountText = view.findViewById(R.id.text_record_count);

        // 绑定服务
        Intent intent = new Intent(requireContext(), TimerService.class);
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // 设置模式切换监听
        switchButton.setOnClickListener(v -> {
            if (!isRunning) {
                isCountdownMode = !isCountdownMode;
                animateModeSwitch();
            }
        });

        // 设置按钮点击事件
        startButton.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });

        stopButton.setOnClickListener(v -> stopTimer());

        MaterialCardView recordsCard = view.findViewById(R.id.card_view_records);

        // 设置记录卡片点击事件
        recordsCard.setOnClickListener(v -> showRecordsDialog());

        // 更新记录数量
        updateRecordCount();

        // 初始化统计信息视图
        textStatsMode = view.findViewById(R.id.text_stats_mode);
        textStatsLongest = view.findViewById(R.id.text_stats_longest);
        textStatsShortest = view.findViewById(R.id.text_stats_shortest);
        textStatsAverage = view.findViewById(R.id.text_stats_average);
        textStatsCount = view.findViewById(R.id.text_stats_count);

        // 更新统计信息
        updateTimerStats();

        return view;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_timer);
    }

    @Override
    protected void initViews(View view) {
        timeText = view.findViewById(R.id.text_time);
        startButton = view.findViewById(R.id.btn_start);
        stopButton = view.findViewById(R.id.btn_stop);
        switchButton = view.findViewById(R.id.btn_switch_mode);
        modeText = view.findViewById(R.id.text_mode);
        inputLayoutMinutes = view.findViewById(R.id.input_layout_minutes);
        editMinutes = view.findViewById(R.id.edit_minutes);
        recordCountText = view.findViewById(R.id.text_record_count);

        // 绑定服务
        Intent intent = new Intent(requireContext(), TimerService.class);
        requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

        // 设置模式切换监听
        switchButton.setOnClickListener(v -> {
            if (!isRunning) {
                isCountdownMode = !isCountdownMode;
                animateModeSwitch();
            }
        });

        // 设置按钮点击事件
        startButton.setOnClickListener(v -> {
            if (!isRunning) {
                startTimer();
            } else {
                pauseTimer();
            }
        });

        stopButton.setOnClickListener(v -> stopTimer());

        MaterialCardView recordsCard = view.findViewById(R.id.card_view_records);

        // 设置记录卡片点击事件
        recordsCard.setOnClickListener(v -> showRecordsDialog());

        // 更新记录数量
        updateRecordCount();
    }

    private void animateModeSwitch() {
        // 创建渐变动画
        float startRotation = switchButton.getRotation();
        float endRotation = startRotation + 180;
        
        // 图标旋转动画
        switchButton.animate()
                .rotation(endRotation)
                .setDuration(300)
                .start();

        // 文字渐变动画
        modeText.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    modeText.setText(isCountdownMode ? R.string.timer_mode_countdown : R.string.timer_mode_stopwatch);
                    modeText.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();

        // 输入框动画
        if (isCountdownMode) {
            inputLayoutMinutes.setVisibility(View.VISIBLE);
            inputLayoutMinutes.setAlpha(0f);
            inputLayoutMinutes.setScaleY(0.8f);
            inputLayoutMinutes.animate()
                    .alpha(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start();
        } else {
            inputLayoutMinutes.animate()
                    .alpha(0f)
                    .scaleY(0.8f)
                    .setDuration(300)
                    .withEndAction(() -> inputLayoutMinutes.setVisibility(View.GONE))
                    .start();
        }

        // 时间显示动画
        timeText.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> {
                    timeText.setText("00:00:00");
                    timeText.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void startTimer() {
        if (!isRunning) {
            if (startButton.getText().toString().equals(getString(R.string.timer_resume))) {
                // 继续计时
                timerService.resumeTimer();
            } else {
                // 开始新计时
                if (isCountdownMode) {
                    String minutesStr = editMinutes.getText().toString();
                    if (minutesStr.isEmpty()) {
                        editMinutes.setError(getString(R.string.timer_input_minutes));
                        return;
                    }
                    float minutes = Float.parseFloat(minutesStr);
                    long milliseconds = (long) (minutes * 60 * 1000);
                    timerService.startTimer(true, milliseconds);
                } else {
                    timerService.startTimer(false, 0);
                }
            }
            isRunning = true;
            startButton.setText(R.string.timer_pause);
            stopButton.setVisibility(View.VISIBLE);
            switchButton.setEnabled(false);
            switchButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.light_gray)));
            inputLayoutMinutes.setEnabled(false);
        } else {
            pauseTimer();
        }
    }

    private void pauseTimer() {
        if (timerService != null) {
            timerService.pauseTimer();
            isRunning = false;
            startButton.setText(R.string.timer_resume);
        }
    }

    private void stopTimer() {
        if (timerService != null) {
            timerService.stopTimer();
            resetTimer();
        }
    }

    private void resetTimer() {
        isRunning = false;
        startButton.setText(R.string.timer_start);
        stopButton.setVisibility(View.GONE);
        switchButton.setEnabled(true);
        switchButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue_500)));
        inputLayoutMinutes.setEnabled(true);
        timeText.setText("00:00:00");
    }

    @Override
    public void onTimerTick(long millisUntilFinished) {
        updateTimerText(millisUntilFinished);
    }

    @Override
    public void onTimerStop(long totalTime) {
        requireActivity().runOnUiThread(() -> {
            // 只在正计时模式下显示记录对话框
            if (!isCountdownMode) {
                showRecordDialog(totalTime);
            } else {
                resetTimer();
            }
        });
    }

    @Override
    public void onCountdownFinish() {
        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(), R.string.timer_countdown_finish, Toast.LENGTH_SHORT).show();
            resetTimer();
        });
    }

    private void updateTimerText(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;
        long milliseconds = millis % 1000;

        requireActivity().runOnUiThread(() -> {
            String formattedTime;
            if (hours > 0) {
                formattedTime = String.format(Locale.getDefault(),
                        "%02d:%02d:%02d.%03d",
                        hours, minutes, seconds, milliseconds);
            } else if (minutes > 0) {
                formattedTime = String.format(Locale.getDefault(),
                        "%02d:%02d.%03d",
                        minutes, seconds, milliseconds);
            } else {
                formattedTime = String.format(Locale.getDefault(),
                        "%02d.%03d",
                        seconds, milliseconds);
            }
            timeText.setText(formattedTime);
        });
    }

    private void showRecordDialog(long totalTime) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_timer_record, null);
        TextView durationText = dialogView.findViewById(R.id.text_timer_duration);
        TextView timestampText = dialogView.findViewById(R.id.text_timer_timestamp);
        com.google.android.material.chip.Chip option1 = dialogView.findViewById(R.id.checkbox_option1);
        com.google.android.material.chip.Chip option2 = dialogView.findViewById(R.id.checkbox_option2);
        com.google.android.material.chip.Chip option3 = dialogView.findViewById(R.id.checkbox_option3);

        // 设置单选逻辑
        option1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                option2.setChecked(false);
                option3.setChecked(false);
            }
        });
        option2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                option1.setChecked(false);
                option3.setChecked(false);
            }
        });
        option3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                option1.setChecked(false);
                option2.setChecked(false);
            }
        });

        // 设置时长和时间戳
        long hours = totalTime / (1000 * 60 * 60);
        long minutes = (totalTime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (totalTime % (1000 * 60)) / 1000;
        long milliseconds = totalTime % 1000;

        String formattedTime;
        if (hours > 0) {
            formattedTime = String.format(Locale.getDefault(),
                    "%02d:%02d:%02d.%03d",
                    hours, minutes, seconds, milliseconds);
        } else if (minutes > 0) {
            formattedTime = String.format(Locale.getDefault(),
                    "%02d:%02d.%03d",
                    minutes, seconds, milliseconds);
        } else {
            formattedTime = String.format(Locale.getDefault(),
                    "%02d.%03d",
                    seconds, milliseconds);
        }
        
        durationText.setText(String.format(getString(R.string.timer_record_time), formattedTime));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        timestampText.setText(String.format(getString(R.string.timer_record_timestamp),
                sdf.format(new Date())));

        // 默认选中第一个选项
        option1.setChecked(true);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.timer_record_title)
                .setView(dialogView)
                .create();

        // 设置按钮点击事件
        dialogView.findViewById(R.id.btn_save).setOnClickListener(v -> {
            TimerRecord record = new TimerRecord(
                    totalTime,
                    System.currentTimeMillis(),
                    option1.isChecked(),
                    option2.isChecked(),
                    option3.isChecked(),
                    !isCountdownMode
            );
            
            // 保存记录到数据库
            new Thread(() -> {
                TimerDatabase db = TimerDatabase.getDatabase(requireContext());
                db.timerRecordDao().insert(record);
                requireActivity().runOnUiThread(() -> {
                    updateRecordCount();
                    updateTimerStats();
                    Toast.makeText(requireContext(), R.string.timer_record_saved, Toast.LENGTH_SHORT).show();
                });
            }).start();

            dialog.dismiss();
            resetTimer();
        });

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            resetTimer();
        });

        dialog.show();
    }

    private void updateRecordCount() {
        new Thread(() -> {
            TimerDatabase db = TimerDatabase.getDatabase(requireContext());
            int count = db.timerRecordDao().getCount();
            requireActivity().runOnUiThread(() -> {
                recordCountText.setText(String.format("(%d)", count));
            });
        }).start();
    }

    private void showRecordsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_timer_records, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_records);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        TimerRecordAdapter adapter = new TimerRecordAdapter();
        recyclerView.setAdapter(adapter);

        // 设置删除监听器
        adapter.setOnDeleteClickListener(record -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.timer_delete_record)
                    .setMessage(R.string.timer_delete_record_confirm)
                    .setNegativeButton(R.string.cancel, null);

            // 创建对话框
            AlertDialog deleteDialog = builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
                new Thread(() -> {
                    TimerDatabase db = TimerDatabase.getDatabase(requireContext());
                    db.timerRecordDao().delete(record);
                    requireActivity().runOnUiThread(() -> {
                        loadRecords(adapter);
                        updateRecordCount();
                        Toast.makeText(requireContext(), R.string.timer_record_deleted, Toast.LENGTH_SHORT).show();
                    });
                }).start();
            }).create();

            // 显示对话框并设置确定按钮的文字颜色
            deleteDialog.show();
            deleteDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue_500));
        });

        // 加载记录
        loadRecords(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        dialog.show();
    }

    private void loadRecords(TimerRecordAdapter adapter) {
        TimerDatabase db = TimerDatabase.getDatabase(requireContext());
        db.timerRecordDao().getAllRecords().observe(getViewLifecycleOwner(), records -> {
            adapter.setRecords(records);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isBound) {
            requireContext().unbindService(connection);
            isBound = false;
        }
    }

    // 更新统计信息
    private void updateTimerStats() {
        boolean isStopwatch = !isCountdownMode;
        textStatsMode.setText(isStopwatch ? R.string.timer_stats_stopwatch : R.string.timer_stats_countdown);

        // 在后台线程中获取统计数据
        new Thread(() -> {
            TimerRecordDao dao = TimerDatabase.getInstance(requireContext()).timerRecordDao();
            
            if (isStopwatch) {
                // 正计时模式：显示三种类型的统计
                StringBuilder statsText = new StringBuilder();
                
                // 遍历三种类型
                for (int optionType = 1; optionType <= 3; optionType++) {
                    int count = dao.getCountByOption(true, optionType);
                    if (count > 0) {
                        // 添加类型标题
                        if (statsText.length() > 0) {
                            statsText.append("\n\n"); // 在不同类型之间添加额外的空行
                        }
                        // 根据类型添加对应图标名称
                        String iconName = optionType == 1 ? "⚡" :  // 用 ⚡ 代表 ic_spark
                                    optionType == 2 ? "🍃" :  // 用 🍃 代表 ic_leaf
                                    "🍑";  // 用 🍑 代表 ic_peach
                        statsText.append(String.format(Locale.getDefault(), "%s ", iconName));
                        
                        long longest = dao.getLongestDurationByOption(true, optionType);
                        long shortest = dao.getShortestDurationByOption(true, optionType);
                        long average = dao.getAverageDurationByOption(true, optionType);
                        
                        statsText.append("\n").append(getString(R.string.timer_stats_longest, formatTime(longest)));
                        statsText.append("\n").append(getString(R.string.timer_stats_shortest, formatTime(shortest)));
                        statsText.append("\n").append(getString(R.string.timer_stats_average, formatTime(average)));
                        statsText.append("\n").append(getString(R.string.timer_stats_total_count, count));
                    }
                }
                
                // 在主线程更新UI
                String finalStats = statsText.toString();
                requireActivity().runOnUiThread(() -> {
                    if (finalStats.isEmpty()) {
                        textStatsLongest.setText(R.string.timer_stats_no_records);
                        textStatsShortest.setVisibility(View.GONE);
                        textStatsAverage.setVisibility(View.GONE);
                        textStatsCount.setVisibility(View.GONE);
                    } else {
                        textStatsLongest.setText(finalStats);
                        textStatsShortest.setVisibility(View.GONE);
                        textStatsAverage.setVisibility(View.GONE);
                        textStatsCount.setVisibility(View.GONE);
                    }
                });
            } else {
                // 倒计时模式：显示总体统计
                int count = dao.getCountByType();
                if (count > 0) {
                    long longest = dao.getLongestDuration();
                    long shortest = dao.getShortestDuration();
                    long average = dao.getAverageDuration();

                    requireActivity().runOnUiThread(() -> {
                        textStatsLongest.setText(getString(R.string.timer_stats_longest, formatTime(longest)));
                        textStatsShortest.setVisibility(View.VISIBLE);
                        textStatsAverage.setVisibility(View.VISIBLE);
                        textStatsCount.setVisibility(View.VISIBLE);
                        textStatsShortest.setText(getString(R.string.timer_stats_shortest, formatTime(shortest)));
                        textStatsAverage.setText(getString(R.string.timer_stats_average, formatTime(average)));
                        textStatsCount.setText(getString(R.string.timer_stats_total_count, count));
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        textStatsLongest.setText(R.string.timer_stats_no_records);
                        textStatsShortest.setVisibility(View.GONE);
                        textStatsAverage.setVisibility(View.GONE);
                        textStatsCount.setVisibility(View.GONE);
                    });
                }
            }
        }).start();
    }

    // 在模式切换时更新统计信息
    private void switchMode() {
        if (!isRunning) {
            isCountdownMode = !isCountdownMode;
            animateModeSwitch();
            updateTimerStats();  // 更新统计信息
        }
    }

    private String formatTime(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;
        long milliseconds = millis % 1000;

        if (hours > 0) {
            return String.format(Locale.getDefault(),
                    "%02d:%02d:%02d.%03d",
                    hours, minutes, seconds, milliseconds);
        } else if (minutes > 0) {
            return String.format(Locale.getDefault(),
                    "%02d:%02d.%03d",
                    minutes, seconds, milliseconds);
        } else {
            return String.format(Locale.getDefault(),
                    "%02d.%03d",
                    seconds, milliseconds);
        }
    }

    // 在保存记录后更新统计信息
    private void saveRecord(List<Integer> selectedOptions) {
        // ... existing record saving code ...
        updateTimerStats();
    }
}