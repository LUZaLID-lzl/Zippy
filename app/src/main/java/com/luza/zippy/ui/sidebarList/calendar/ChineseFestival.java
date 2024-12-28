package com.luza.zippy.ui.sidebarList.calendar;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import com.luza.zippy.R;
import com.luza.zippy.application.ZippyApplication;

public class ChineseFestival {
    private static final Map<String, String> SOLAR_FESTIVALS = new HashMap<>();  // 公历节日
    private static final Map<String, String> LUNAR_FESTIVALS = new HashMap<>();  // 农历节日

    static {
        Context context = ZippyApplication.getContext();  // 需要添加Application类来获取Context
        
        // 公历节日
        SOLAR_FESTIVALS.put("0101", context.getString(R.string.new_year));
        SOLAR_FESTIVALS.put("0214", context.getString(R.string.valentine));
        SOLAR_FESTIVALS.put("0308", context.getString(R.string.women_day));
        SOLAR_FESTIVALS.put("0312", context.getString(R.string.arbor_day));
        SOLAR_FESTIVALS.put("0401", context.getString(R.string.fool_day));
        SOLAR_FESTIVALS.put("0501", context.getString(R.string.labor_day));
        SOLAR_FESTIVALS.put("0504", context.getString(R.string.youth_day));
        SOLAR_FESTIVALS.put("0601", context.getString(R.string.children_day));
        SOLAR_FESTIVALS.put("0701", context.getString(R.string.cpc_day));
        SOLAR_FESTIVALS.put("0801", context.getString(R.string.army_day));
        SOLAR_FESTIVALS.put("0910", context.getString(R.string.teacher_day));
        SOLAR_FESTIVALS.put("1001", context.getString(R.string.national_day));
        SOLAR_FESTIVALS.put("1225", context.getString(R.string.christmas));

        // 农历节日
        LUNAR_FESTIVALS.put("0101", context.getString(R.string.spring_festival));
        LUNAR_FESTIVALS.put("0115", context.getString(R.string.lantern_festival));
        LUNAR_FESTIVALS.put("0505", context.getString(R.string.dragon_boat));
        LUNAR_FESTIVALS.put("0707", context.getString(R.string.chinese_valentine));
        LUNAR_FESTIVALS.put("0815", context.getString(R.string.mid_autumn));
        LUNAR_FESTIVALS.put("0909", context.getString(R.string.double_ninth));
        LUNAR_FESTIVALS.put("1230", context.getString(R.string.new_year_eve));
    }

    // 获取公历节日
    public static String getSolarFestival(int month, int day) {
        String key = String.format("%02d%02d", month, day);
        return SOLAR_FESTIVALS.get(key);
    }

    // 获取农历节日
    public static String getLunarFestival(int month, int day) {
        String key = String.format("%02d%02d", month, day);
        return LUNAR_FESTIVALS.get(key);
    }
} 