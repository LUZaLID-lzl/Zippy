package com.luza.zippy.ui.sidebarList.calendar;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.luza.zippy.ui.sidebarList.calendar.LunarCalendar;
import com.luza.zippy.ui.sidebarList.calendar.database.BirthdayDatabase;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.app.AlertDialog;
import android.widget.NumberPicker;
import android.widget.CheckBox;
import android.widget.Toast;
import android.os.AsyncTask;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import androidx.recyclerview.widget.DividerItemDecoration;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.SpannableStringBuilder;
import android.graphics.Typeface;
import com.google.android.material.button.MaterialButton;

public class CalendarFragment extends BaseFragment {
    private GridLayout calendarHeader;
    private GridLayout calendarGrid;
    private TextView currentDateText;
    private Calendar currentCalendar;
    private SimpleDateFormat dateFormat;
    private LunarCalendar lunarCalendar;
    private TextView todaySolarText;
    private TextView todayLunarText;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    private View rootView;

    @Override
    protected String getTitle() {
        return getString(R.string.menu_calendar);
    }

    @Override
    protected void initViews(View view) {
        rootView = view;
        calendarHeader = view.findViewById(R.id.calendar_header);
        calendarGrid = view.findViewById(R.id.calendar_grid);
        currentDateText = view.findViewById(R.id.text_current_date);
        
        currentCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());

        lunarCalendar = new LunarCalendar();

        // 修改月份切换按钮
        ImageButton prevButton = view.findViewById(R.id.btn_prev_month);
        ImageButton nextButton = view.findViewById(R.id.btn_next_month);
        
        prevButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        nextButton.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendar();
        });

        // 初始化日历
        initCalendarHeader();
        updateCalendar();

        // 初始化添加生日按钮
        view.findViewById(R.id.btn_add_birthday).setOnClickListener(v -> showAddBirthdayDialog());
        view.findViewById(R.id.btn_manage_birthday).setOnClickListener(v -> showManageBirthdayDialog());

        todaySolarText = view.findViewById(R.id.text_today_solar);
        todayLunarText = view.findViewById(R.id.text_today_lunar);
        
        updateTodayInfo();

        // 设置每日生日检查
        setupBirthdayCheck();

        // 添加测试提醒按钮
        view.findViewById(R.id.btn_test_notification).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // 检查通知权限
                if (requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    // 请求权限
                    requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE
                    );
                    return;
                }
            }

            // 创建一次性工作请求
            androidx.work.OneTimeWorkRequest testRequest =
                new androidx.work.OneTimeWorkRequest.Builder(BirthdayCheckWorker.class)
                    .build();

            // 立即执行
            androidx.work.WorkManager.getInstance(requireContext())
                .enqueue(testRequest);
            
            Toast.makeText(requireContext(), "已发送测试通知", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // 权限获取成功，重新尝试发送通知
                if (rootView != null) {
                    rootView.findViewById(R.id.btn_test_notification).performClick();
                }
            } else {
                Toast.makeText(requireContext(), "需要通知权限才能发送提醒", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initCalendarHeader() {
        String[] weekDays = {
            getString(R.string.sunday),
            getString(R.string.monday),
            getString(R.string.tuesday),
            getString(R.string.wednesday),
            getString(R.string.thursday),
            getString(R.string.friday),
            getString(R.string.saturday)
        };

        for (String weekDay : weekDays) {
            TextView textView = createTextView(weekDay);
            textView.setTextColor(getResources().getColor(R.color.blue_500));
            calendarHeader.addView(textView);
        }
    }

    private void updateCalendar() {
        // 创建淡出动画
        Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), R.anim.calendar_fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // 在淡出动画结束后更新日历内容
                updateCalendarContent();
                // 开始淡入动画
                Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), R.anim.calendar_fade_in);
                calendarGrid.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        // 开始淡出动画
        calendarGrid.startAnimation(fadeOut);
    }

    private void updateCalendarContent() {
        // 更新当前日期显示
        String dateText = dateFormat.format(currentCalendar.getTime());
        currentDateText.setText(dateText);
        
        // 清空日历网格
        calendarGrid.removeAllViews();
        
        // 重新设置网格布局的行列数
        calendarGrid.setColumnCount(7);
        calendarGrid.setRowCount(6);  // 确保有足够的行数显示所有日期
        
        // 获取当月第一天是星期几
        Calendar temp = (Calendar) currentCalendar.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1;
        
        // 获取当月天数
        int daysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // 获取当月所有生日
        new AsyncTask<Void, Void, Map<Integer, List<Birthday>>>() {
            @Override
            protected Map<Integer, List<Birthday>> doInBackground(Void... voids) {
                List<Birthday> allBirthdays = BirthdayDatabase.getInstance(requireContext())
                    .birthdayDao()
                    .getAll();
                
                Map<Integer, List<Birthday>> birthdayMap = new HashMap<>();
                int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;
                int currentYear = currentCalendar.get(Calendar.YEAR);

                for (Birthday birthday : allBirthdays) {
                    if (birthday.isLunar()) {
                        // 农历生日：使用保存的农历月日计算当年对应的公历日期
                        int[] solarDate = PaseDateUtil.lunarToSolar(
                            currentYear,        // 使用当前年份
                            birthday.getLunarMonth(),  // 使用保存的农历月
                            birthday.getLunarDay(),    // 使用保存的农历日
                            false  // 非闰月
                        );
                        
                        if (solarDate != null && solarDate[1] == currentMonth) {
                            List<Birthday> dayBirthdays = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dayBirthdays = birthdayMap.computeIfAbsent(
                                    solarDate[2], k -> new ArrayList<>());
                            }
                            dayBirthdays.add(birthday);
                        }
                    } else if (birthday.getMonth() == currentMonth) {
                        // 公历生日直接添加
                        List<Birthday> dayBirthdays = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            dayBirthdays = birthdayMap.computeIfAbsent(
                                birthday.getDay(), k -> new ArrayList<>());
                        }
                        dayBirthdays.add(birthday);
                    }
                }
                return birthdayMap;
            }

            @Override
            protected void onPostExecute(Map<Integer, List<Birthday>> birthdayMap) {
                // 填充日历网格
                // 添加空白天数
                for (int i = 0; i < firstDayOfWeek; i++) {
                    addEmptyDay();
                }
                
                // 添加日期
                for (int day = 1; day <= daysInMonth; day++) {
                    View dayView = createDayView(day, birthdayMap.get(day));
                    calendarGrid.addView(dayView);
                }
                
                // 如果需要，添加剩余的空白天数以填满网格
                int totalCells = 42; // 6行7列
                int remainingCells = totalCells - (firstDayOfWeek + daysInMonth);
                for (int i = 0; i < remainingCells; i++) {
                    addEmptyDay();
                }

                // 在所有日历单元格添加完成后更新当前信息
                updateCurrentInfo();
            }
        }.execute();
    }

    private int[] convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay, int targetYear) {
        // 先获取目标年份的农历日期对应的公历日期
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, targetYear);
        
        // 计算该年农历日期对应的公历日期
        int[] solarDate = PaseDateUtil.lunarToSolar(
            targetYear,    // 使用目标年份
            lunarMonth,    // 使用原始农历月份
            lunarDay,      // 使用原始农历日期
            false         // 非闰月
        );
        
        return solarDate;  // 返回转换后的公历日期 [年, 月, 日]
    }

    // 计算某日期是一年中的第几天
    private int getDayOfYear(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day);  // 月份从0开始
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    private void showBirthdayDetails(List<Birthday> birthdays) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_birthday_details, null);
        TextView detailsText = dialogView.findViewById(R.id.text_birthday_details);
        
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < birthdays.size(); i++) {
            Birthday birthday = birthdays.get(i);
            
            // 添加名字（加粗）
            SpannableString name = new SpannableString(birthday.getName());
            name.setSpan(new StyleSpan(Typeface.BOLD), 0, name.length(), 
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.append(name);
            
            // 添加日期信息
            builder.append("\n");
            String dateInfo = String.format("%d年%d月%d日", 
                birthday.getYear(), birthday.getMonth(), birthday.getDay());
            builder.append(dateInfo);
            
            // 添加农历信息
            if (birthday.isLunar()) {
                builder.append(" (农历)");
            } else {
                builder.append(String.format("\n农历：%d月%s", 
                    birthday.getLunarMonth(), 
                    getLunarDayText(birthday.getLunarDay())));
            }
            
            // 不是最后一个生日时添加分隔线
            if (i < birthdays.size() - 1) {
                builder.append("\n\n");
                builder.append("──────────");
                builder.append("\n\n");
            }
        }
        
        detailsText.setText(builder);
        
        new AlertDialog.Builder(requireContext(), R.style.DialogStyle)
            .setTitle("生日信息")
            .setView(dialogView)
            .setPositiveButton(R.string.confirm, null)
            .show();
    }

    private void addEmptyDay() {
        View emptyView = getLayoutInflater().inflate(R.layout.item_calendar_day, null);
        // 设置布局参数
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        params.setMargins(2, 2, 2, 2);
        emptyView.setLayoutParams(params);
        calendarGrid.addView(emptyView);
    }

    private boolean isToday(int day) {
        Calendar today = Calendar.getInstance();
        return currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
               currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
               day == today.get(Calendar.DAY_OF_MONTH);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(requireContext());
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        params.setMargins(2, 2, 2, 2);
        
        textView.setLayoutParams(params);
        textView.setText(text);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        textView.setPadding(0, 16, 0, 16);
        textView.setMinHeight(getResources().getDimensionPixelSize(R.dimen.calendar_cell_height));
        textView.setGravity(android.view.Gravity.CENTER);
        
        return textView;
    }

    private void updateCurrentInfo() {
        // 只显示年月
        String dateInfo = dateFormat.format(currentCalendar.getTime());
        currentDateText.setText(dateInfo);
    }

    private void showAddBirthdayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogStyle);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_birthday, null);
        
        // 初始化控件
        TextInputEditText nameInput = dialogView.findViewById(R.id.edit_name);
        NumberPicker yearPicker = dialogView.findViewById(R.id.picker_year);
        NumberPicker monthPicker = dialogView.findViewById(R.id.picker_month);
        NumberPicker dayPicker = dialogView.findViewById(R.id.picker_day);
        CheckBox lunarCheck = dialogView.findViewById(R.id.check_lunar);
        
        // 设置年份选择器
        int currentYear = currentCalendar.get(Calendar.YEAR);
        yearPicker.setMinValue(1900);  // 设置最小年份
        yearPicker.setMaxValue(currentYear);  // 设置最大年份为当前年
        yearPicker.setValue(currentYear);  // 默认选择当前年
        
        // 设置月份选择器
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(currentCalendar.get(Calendar.MONTH) + 1);
        
        // 设置日期选择器并处理月份变化
        updateDayPicker(dayPicker, monthPicker.getValue(), yearPicker.getValue());
        monthPicker.setOnValueChangedListener((picker, oldVal, newVal) -> 
            updateDayPicker(dayPicker, newVal, yearPicker.getValue()));
        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> 
            updateDayPicker(dayPicker, monthPicker.getValue(), newVal));
        
        AlertDialog dialog = builder.setView(dialogView)
            .setTitle(R.string.add_birthday)
            .setPositiveButton(R.string.confirm, (d, which) -> {
                String name = nameInput.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.input_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                
                saveBirthday(name, yearPicker.getValue(), monthPicker.getValue(), 
                            dayPicker.getValue(), lunarCheck.isChecked());
            })
            .setNegativeButton(R.string.cancel, null)
            .create();

        // 设置按钮文字颜色为主题色
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.blue_500));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(getResources().getColor(R.color.text_secondary));
        });

        dialog.show();
    }

    private void updateDayPicker(NumberPicker dayPicker, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        dayPicker.setMinValue(1);
        dayPicker.setMaxValue(maxDays);
        
        if (dayPicker.getValue() > maxDays) {
            dayPicker.setValue(maxDays);
        }
    }

    private void saveBirthday(String name, int year, int month, int day, boolean isLunar) {
        Birthday birthday = new Birthday(name, year, month, day, isLunar);
        
        // 计算并保存对应的农历日期
        int[] lunarDate = PaseDateUtil.solarToLunar(year, month, day);
        if (lunarDate != null) {
            birthday.setLunarMonth(lunarDate[1]);
            birthday.setLunarDay(lunarDate[2]);
        }
        
        saveBirthdayToDb(birthday);
    }

    private void saveBirthdayToDb(Birthday birthday) {
        new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... voids) {
                return BirthdayDatabase.getInstance(requireContext())
                                     .birthdayDao()
                                     .insert(birthday);
            }

            @Override
            protected void onPostExecute(Long id) {
                if (id > 0) {
                    Toast.makeText(requireContext(), R.string.birthday_added, Toast.LENGTH_SHORT).show();
                    updateCalendar();  // 更新日历显示
                }
            }
        }.execute();
    }

    private void showManageBirthdayDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.DialogStyle);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_manage_birthday, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_birthdays);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 加载生日列表
        new AsyncTask<Void, Void, List<Birthday>>() {
            @Override
            protected List<Birthday> doInBackground(Void... voids) {
                return BirthdayDatabase.getInstance(requireContext())
                                     .birthdayDao()
                                     .getAll();
            }

            @Override
            protected void onPostExecute(List<Birthday> birthdays) {
                BirthdayAdapter adapter = new BirthdayAdapter(
                    birthdays,
                    birthday -> {
                        // 删除生日
                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                BirthdayDatabase.getInstance(requireContext())
                                              .birthdayDao()
                                              .delete(birthday);
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                Toast.makeText(requireContext(), R.string.birthday_deleted, Toast.LENGTH_SHORT).show();
                                // 刷新列表
                                birthdays.remove(birthday);
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        }.execute();
                    },
                    requireContext()
                );
                recyclerView.setAdapter(adapter);
            }
        }.execute();

        AlertDialog dialog = builder.setView(dialogView)
            .setTitle(R.string.manage_birthday)
            .setPositiveButton(R.string.confirm, null)
            .create();

        // 设置按钮文字颜色为主题色
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.blue_500));
        });

        dialog.show();
    }

    private static class BirthdayAdapter extends RecyclerView.Adapter<BirthdayAdapter.ViewHolder> {
        private List<Birthday> birthdays;
        private OnBirthdayDeleteListener listener;
        private Context context;

        interface OnBirthdayDeleteListener {
            void onDelete(Birthday birthday);
        }

        BirthdayAdapter(List<Birthday> birthdays, OnBirthdayDeleteListener listener, Context context) {
            this.birthdays = birthdays;
            this.listener = listener;
            this.context = context;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.item_birthday, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Birthday birthday = birthdays.get(position);
            holder.nameText.setText(birthday.getName());
            
            StringBuilder dateText = new StringBuilder();
            dateText.append(birthday.getYear())
                   .append(context.getString(R.string.year))
                   .append(birthday.getMonth())
                   .append(context.getString(R.string.month))
                   .append(birthday.getDay())
                   .append(context.getString(R.string.day));
            
            if (birthday.isLunar()) {
                dateText.append(" (")
                       .append(context.getString(R.string.lunar_mark))
                       .append(")");
            } else {
                // 如果是公历生日，显示对应的农历日期
                int[] lunarDate = PaseDateUtil.solarToLunar(
                    Calendar.getInstance().get(Calendar.YEAR),
                    birthday.getMonth(),
                    birthday.getDay()
                );
                if (lunarDate != null) {
                    dateText.append("\n")
                           .append(context.getString(R.string.corresponding_lunar))
                           .append(lunarDate[1])
                           .append(context.getString(R.string.month))
                           .append(getLunarDayText(lunarDate[2]));
                }
            }
            
            holder.dateText.setText(dateText.toString());
            
            holder.deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDelete(birthday);
                }
            });
        }

        @Override
        public int getItemCount() {
            return birthdays.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView dateText;
            MaterialButton deleteButton;

            ViewHolder(View view) {
                super(view);
                nameText = view.findViewById(R.id.text_name);
                dateText = view.findViewById(R.id.text_date);
                deleteButton = view.findViewById(R.id.btn_delete);
            }
        }
    }

    private View createDayView(int day, List<Birthday> birthdays) {
        View dayView = getLayoutInflater().inflate(R.layout.item_calendar_day, null);
        TextView dateText = dayView.findViewById(R.id.text_date);
        TextView lunarText = dayView.findViewById(R.id.text_lunar);
        View birthdayMarker = dayView.findViewById(R.id.birthday_marker);
        TextView nameText = dayView.findViewById(R.id.text_name);

        // 设置公历日期
        dateText.setText(String.valueOf(day));

        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentMonth = currentCalendar.get(Calendar.MONTH) + 1;

        // 检查是否是公历节日
        String solarFestival = ChineseFestival.getSolarFestival(currentMonth, day);
        
        // 获取农历日期
        int[] lunarDate = PaseDateUtil.solarToLunar(currentYear, currentMonth, day);
        
        if (lunarDate != null) {
            String lunarDayText = null;
            
            // 检查是否是农历节日
            String lunarFestival = ChineseFestival.getLunarFestival(lunarDate[1], lunarDate[2]);
            
            // 优先显示顺序：节日 > 农历日期
            if (solarFestival != null) {
                lunarDayText = solarFestival;
                lunarText.setTextColor(getResources().getColor(R.color.festival_color));
            } else if (lunarFestival != null) {
                lunarDayText = lunarFestival;
                lunarText.setTextColor(getResources().getColor(R.color.festival_color));
            } else {
                lunarDayText = getLunarDayText(lunarDate[2]);
                lunarText.setTextColor(getResources().getColor(R.color.text_secondary));
            }
            
            lunarText.setText(lunarDayText);
            lunarText.setVisibility(View.VISIBLE);
        }

        // 处理生日显示
        if (birthdays != null && !birthdays.isEmpty()) {
            birthdayMarker.setVisibility(View.VISIBLE);
            nameText.setVisibility(View.VISIBLE);
            nameText.setText(birthdays.get(0).getName());
            if (birthdays.size() > 1) {
                nameText.append("...");
            }
            dayView.setOnClickListener(v -> showBirthdayDetails(birthdays));
        }

        // 设置当天高亮
        if (isToday(day)) {
            dateText.setBackgroundResource(R.drawable.bg_current_day);
            dateText.setTextColor(getResources().getColor(android.R.color.white));
        }

        // 设置布局参数
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED);
        params.setMargins(2, 2, 2, 2);
        dayView.setLayoutParams(params);

        return dayView;
    }

    // 将农历日期转换为显示文本
    private static String getLunarDayText(int lunarDay) {
        String[] lunarDays = {"初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
                "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
                "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"};
        return lunarDays[lunarDay - 1];
    }

    private void updateTodayInfo() {
        Calendar today = Calendar.getInstance();
        
        // 更新公历日期
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyy年MM月dd日 EEEE", Locale.CHINESE);
        todaySolarText.setText(getString(R.string.today_is) + fullDateFormat.format(today.getTime()));
        
        // 更新农历日期
        int[] lunarDate = PaseDateUtil.solarToLunar(
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.DAY_OF_MONTH)
        );
        
        if (lunarDate != null) {
            String lunarText = getString(R.string.lunar_calendar) + 
                String.format("%d%s%s",
                    lunarDate[1],
                    getString(R.string.month),
                    getLunarDayText(lunarDate[2]));
            todayLunarText.setText(lunarText);
        }
    }

    private void setupBirthdayCheck() {
        // 创建每日检查任务
        androidx.work.PeriodicWorkRequest birthdayCheckRequest =
            new androidx.work.PeriodicWorkRequest.Builder(
                BirthdayCheckWorker.class,
                24, // 重复间隔
                TimeUnit.HOURS // 时间单位
            )
            .build();

        // 确保只有一个实例在运行
        androidx.work.WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "BirthdayCheck",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                birthdayCheckRequest
            );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }
} 