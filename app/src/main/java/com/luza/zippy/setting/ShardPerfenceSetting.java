package com.luza.zippy.setting;

import android.content.Context;
import android.content.SharedPreferences;

public class ShardPerfenceSetting {
    private static final String TAG = "ShardPerfenceSetting";
    private static final String PREF_NAME = "zippy_settings";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static ShardPerfenceSetting instance;
    private Context mContext;

    // 定义多个数据项的键
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HOMETHEM = "homeTheme";
    private static final String KEY_HOMEANIMATIONNUM = "homeAnimationNum";

    // 实际数据
    private String language;        //en - zh
    private String homeTheme;       //pikachu - bulbasaur - squirtle - mew
    private int homeAnimationNum;   //10 -> 100

    public ShardPerfenceSetting(Context context) {
        //构造函数
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //初始化时读取 SharedPreferences 中的数据
        language = sharedPreferences.getString(KEY_LANGUAGE, "en");
        homeTheme = sharedPreferences.getString(KEY_HOMETHEM, "squirtle");
        homeAnimationNum = sharedPreferences.getInt(KEY_HOMEANIMATIONNUM, 45);
        logToString();
    }

    public static synchronized ShardPerfenceSetting getInstance(Context context) {
        if (instance == null) {
            instance = new ShardPerfenceSetting(context.getApplicationContext());
        }
        return instance;
    }

    public String getHomeTheme() {
        return homeTheme;
    }

    public void setHomeTheme(String homeTheme) {
        this.homeTheme = homeTheme;
        editor.putString(KEY_HOMETHEM, homeTheme);
        editor.apply();
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public int getHomeAnimationNum() {
        return homeAnimationNum;
    }

    public void setHomeAnimationNum(int homeAnimationNum) {
        this.homeAnimationNum = homeAnimationNum;
        editor.putInt(KEY_HOMEANIMATIONNUM, homeAnimationNum);
        editor.apply();
    }

    public void logToString() {
        String log =  "ShardPerfenceSetting{" +
                "language='" + language + '\'' +
                ", homeTheme='" + homeTheme + '\'' +
                ", homeAnimationNum='" + homeAnimationNum + '\'' +
                '}';
        android.util.Log.d(TAG,log);
    }
}
