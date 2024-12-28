package com.luza.zippy.ui.sidebarList.calendar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.luza.zippy.R;
import com.luza.zippy.MainActivity;
import com.luza.zippy.ui.sidebarList.calendar.database.BirthdayDatabase;

import java.util.Calendar;
import java.util.List;

public class BirthdayCheckWorker extends Worker {
    private static final String CHANNEL_ID = "birthday_channel";
    private static final int DAYS_BEFORE = 3; // 提前3天提醒

    public BirthdayCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // 直接发送测试通知
        String message = "测试提醒：还有3天就是张三的生日了！";
        sendNotification(message);
        return Result.success();
    }

    private void checkUpcomingBirthdays() {
        Calendar today = Calendar.getInstance();
        int currentYear = today.get(Calendar.YEAR);
        int dayOfYear = today.get(Calendar.DAY_OF_YEAR);

        List<Birthday> birthdays = BirthdayDatabase.getInstance(getApplicationContext())
            .birthdayDao()
            .getAll();

        for (Birthday birthday : birthdays) {
            if (birthday.isLunar()) {
                checkLunarBirthday(birthday, currentYear, dayOfYear);
            } else {
                checkSolarBirthday(birthday, currentYear, dayOfYear);
            }
        }
    }

    private void checkLunarBirthday(Birthday birthday, int currentYear, int todayDayOfYear) {
        // 获取农历生日今年对应的公历日期
        int[] solarDate = PaseDateUtil.lunarToSolar(
            currentYear,
            birthday.getLunarMonth(),
            birthday.getLunarDay(),
            false
        );

        if (solarDate != null) {
            Calendar birthdayCal = Calendar.getInstance();
            birthdayCal.set(solarDate[0], solarDate[1] - 1, solarDate[2]);
            int birthdayDayOfYear = birthdayCal.get(Calendar.DAY_OF_YEAR);
            
            checkAndNotify(birthday, birthdayDayOfYear, todayDayOfYear);
        }
    }

    private void checkSolarBirthday(Birthday birthday, int currentYear, int todayDayOfYear) {
        Calendar birthdayCal = Calendar.getInstance();
        birthdayCal.set(currentYear, birthday.getMonth() - 1, birthday.getDay());
        int birthdayDayOfYear = birthdayCal.get(Calendar.DAY_OF_YEAR);
        
        checkAndNotify(birthday, birthdayDayOfYear, todayDayOfYear);
    }

    private void checkAndNotify(Birthday birthday, int birthdayDayOfYear, int todayDayOfYear) {
        int daysUntilBirthday = birthdayDayOfYear - todayDayOfYear;
        
        // 如果生日已过，计算到明年的天数
        if (daysUntilBirthday < 0) {
            Calendar nextYear = Calendar.getInstance();
            nextYear.add(Calendar.YEAR, 1);
            daysUntilBirthday = birthdayDayOfYear + (nextYear.getActualMaximum(Calendar.DAY_OF_YEAR) - todayDayOfYear);
        }

        if (daysUntilBirthday <= DAYS_BEFORE && daysUntilBirthday >= 0) {
            String message = "xxxxxxxxxxxxxxxxxxxxxxxxxxx";
            sendNotification(message);
        }
    }

    private void sendNotification(String message) {
        Context context = getApplicationContext();
        
        // 创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.birthday_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(context.getString(R.string.birthday_channel_description));
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // 创建点击通知时的意图
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // 构建通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.birthday_notification_title))
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(new long[]{0, 1000, 500, 1000})  // 添加震动效果
            .setLights(0xFF0000FF, 300, 700);  // 添加呼吸灯效果

        // 发送通知
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }
} 