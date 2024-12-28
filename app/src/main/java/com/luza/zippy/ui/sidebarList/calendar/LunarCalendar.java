package com.luza.zippy.ui.sidebarList.calendar;

import android.content.Context;
import com.luza.zippy.R;
import java.util.Calendar;

public class LunarCalendar {
    private static final long[] LUNAR_INFO = new long[]{
        0x04bd8, 0x04ae0, 0x0a570, 0x054d5, 0x0d260, 0x0d950, 0x16554, 0x056a0, 0x09ad0, 0x055d2,
        0x04ae0, 0x0a5b6, 0x0a4d0, 0x0d250, 0x1d255, 0x0b540, 0x0d6a0, 0x0ada2, 0x095b0, 0x14977,
        0x04970, 0x0a4b0, 0x0b4b5, 0x06a50, 0x06d40, 0x1ab54, 0x02b60, 0x09570, 0x052f2, 0x04970,
        0x06566, 0x0d4a0, 0x0ea50, 0x06e95, 0x05ad0, 0x02b60, 0x186e3, 0x092e0, 0x1c8d7, 0x0c950,
        0x0d4a0, 0x1d8a6, 0x0b550, 0x056a0, 0x1a5b4, 0x025d0, 0x092d0, 0x0d2b2, 0x0a950, 0x0b557
    };

    private static final String[] CHINESE_NUMBER = {
        "一", "二", "三", "四", "五", "六", "七", "八", "九", "十",
        "十一", "十二"
    };

    private static final String[] CHINESE_TEN = {
        "初", "十", "廿", "卅"
    };

    public static String getLunarDate(int year, int month, int day) {
        // 使用 PaseDateUtil 进行转换
        int[] lunar = solarToLunar(year, month, day);
        return getLunarDay(lunar[2]);
    }

    // 获取完整的农历日期（包含月份）
    public static String getFullLunarDate(int year, int month, int day) {
        // 使用 PaseDateUtil 进行转换
        int[] lunar = solarToLunar(year, month, day);
        String lunarMonth = CHINESE_NUMBER[lunar[1] - 1] + "月";
        String lunarDay = getLunarDay(lunar[2]);
        return lunarMonth + lunarDay;
    }

    private static String getLunarDay(int day) {
        if (day == 10) return "初十";
        if (day == 20) return "二十";
        if (day == 30) return "三十";
        
        int n = (day % 10 == 0) ? 9 : day % 10;
        String prefix = CHINESE_TEN[day / 10];
        return day % 10 == 0 ? prefix : prefix + CHINESE_NUMBER[n - 1];
    }

    private static int[] solarToLunar(int year, int month, int day) {
        // 使用 PaseDateUtil 进行转换
        return PaseDateUtil.solarToLunar(String.valueOf(year), String.valueOf(month), String.valueOf(day));
    }

    // 获取农历节日
    public static String getLunarFestival(Context context, int month, int day) {
        // 先将公历转换为农历
        int[] lunarDate = solarToLunar(
            Calendar.getInstance().get(Calendar.YEAR),
            month,
            day
        );
        int lunarMonth = lunarDate[1];
        int lunarDay = lunarDate[2];

        // 农历节日判断
        if (lunarMonth == 1 && lunarDay == 1) {
            return context.getString(R.string.lunar_new_year);
        }
        if (lunarMonth == 1 && lunarDay == 15) {
            return context.getString(R.string.lantern_festival);
        }
        if (lunarMonth == 5 && lunarDay == 5) {
            return context.getString(R.string.dragon_boat_festival);
        }
        if (lunarMonth == 8 && lunarDay == 15) {
            return context.getString(R.string.mid_autumn_festival);
        }
        if (lunarMonth == 9 && lunarDay == 9) {
            return context.getString(R.string.double_ninth_festival);
        }

        // 公历节日判断
        if (month == 1 && day == 1) {
            return "元旦";
        }
        if (month == 5 && day == 1) {
            return "劳动节";
        }
        if (month == 10 && day == 1) {
            return "国庆节";
        }
        
        return null;
    }

    // 获取节气
    public static String getSolarTerm(Context context, int month, int day) {
        // 简化的节气判断
        if (month == 2) {
            if (day == 4) return context.getString(R.string.spring_begins);
            if (day == 19) return context.getString(R.string.rain_water);
        }
        if (month == 3) {
            if (day == 6) return context.getString(R.string.insects_awaken);
            if (day == 21) return context.getString(R.string.vernal_equinox);
        }
        if (month == 4) {
            if (day == 5) return context.getString(R.string.clear_and_bright);
            if (day == 20) return context.getString(R.string.grain_rain);
        }
        if (month == 5) {
            if (day == 6) return context.getString(R.string.summer_begins);
            if (day == 21) return context.getString(R.string.grain_buds);
        }
        if (month == 6) {
            if (day == 6) return context.getString(R.string.grain_in_ear);
            if (day == 21) return context.getString(R.string.summer_solstice);
        }
        if (month == 7) {
            if (day == 7) return context.getString(R.string.slight_heat);
            if (day == 23) return context.getString(R.string.great_heat);
        }
        if (month == 8) {
            if (day == 8) return context.getString(R.string.autumn_begins);
            if (day == 23) return context.getString(R.string.stopping_the_heat);
        }
        if (month == 9) {
            if (day == 8) return context.getString(R.string.white_dews);
            if (day == 23) return context.getString(R.string.autumn_equinox);
        }
        if (month == 10) {
            if (day == 8) return context.getString(R.string.cold_dews);
            if (day == 24) return context.getString(R.string.frost_descent);
        }
        if (month == 11) {
            if (day == 7) return context.getString(R.string.winter_begins);
            if (day == 22) return context.getString(R.string.light_snow);
        }
        if (month == 12) {
            if (day == 7) return context.getString(R.string.heavy_snow);
            if (day == 22) return context.getString(R.string.winter_solstice);
        }
        if (month == 1) {
            if (day == 6) return context.getString(R.string.slight_cold);
            if (day == 20) return context.getString(R.string.great_cold);
        }
        return null;
    }

    // 获取日期的完整信息（包括节日和节气）
    public static String getDateInfo(Context context, int year, int month, int day) {
        int[] lunar = solarToLunar(year, month, day);
        String dateInfo = getLunarDay(lunar[2]);
        
        // 检查是否是农历节日
        String festival = getLunarFestival(context, lunar[1], lunar[2]);
        if (festival != null) {
            dateInfo = festival;
        }
        
        // 检查是否是节气
        String solarTerm = getSolarTerm(context, month, day);
        if (solarTerm != null) {
            dateInfo = solarTerm;
        }
        
        return dateInfo;
    }
} 