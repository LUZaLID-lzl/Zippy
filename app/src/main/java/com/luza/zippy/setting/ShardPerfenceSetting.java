package com.luza.zippy.setting;

import android.content.Context;
import android.content.SharedPreferences;

public class ShardPerfenceSetting {
    private static final String TAG = "ShardPerfenceSetting_settings";
    private static final String PREF_NAME = "zippy_settings";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static ShardPerfenceSetting instance;
    private Context mContext;

    // 定义多个数据项的键
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_HOMETHEM = "homeTheme";
    private static final String KEY_HOMEANIMATIONNUM = "homeAnimationNum";
    private static final String KEY_ACTIVATE = "activate";
    private static final String KEY_ACTIVATE_NAME = "activeName";
    private static final String KEY_LAUNCH_NUM = "launchNum";
    private static final String KEY_ARRANGE = "arrange";

    // 实际数据
    private String language;        //en - zh
    private String homeTheme;       //pikachu - bulbasaur - squirtle - mew - karsa - capoo - maple - winter
    private int homeAnimationNum;   //0 -> 100
    private boolean activate;       //true - false
    private String activeName;
    private int launchNum;          // 0 ->
    private boolean arrange;        //true -> horizontal  false -> vertical

    public Boolean getArrange() {
        return arrange;
    }

    public void setArrange(Boolean arrange) {
        this.arrange = arrange;
        editor.putBoolean(KEY_ARRANGE, arrange);
        editor.apply();
    }

    public int getLaunchNum() {
        return launchNum;
    }

    public void addLaunchNum() {
        this.launchNum++;
        editor.putInt(KEY_LAUNCH_NUM, launchNum);
        editor.apply();
    }

    public String getActiveName() {
        return activeName;
    }

    public void setActiveName(String activeName) {
        this.activeName = activeName;
        editor.putString(KEY_ACTIVATE_NAME, activeName);
        editor.apply();
    }

    public ShardPerfenceSetting(Context context) {
        //构造函数
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //初始化时读取 SharedPreferences 中的数据
        language = sharedPreferences.getString(KEY_LANGUAGE, "zh");
        homeTheme = sharedPreferences.getString(KEY_HOMETHEM, "mew");
        homeAnimationNum = sharedPreferences.getInt(KEY_HOMEANIMATIONNUM, 38);
        activate = sharedPreferences.getBoolean(KEY_ACTIVATE, false);
        activeName = sharedPreferences.getString(KEY_ACTIVATE_NAME, "none");
        launchNum = sharedPreferences.getInt(KEY_LAUNCH_NUM, 0);
        arrange = sharedPreferences.getBoolean(KEY_ARRANGE, true);
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

    public Boolean getActivate() {
        return activate;
    }

    public void setActivate(Boolean activate) {
        this.activate = activate;
        editor.putBoolean(KEY_ACTIVATE, activate);
        editor.apply();
    }

    public void logToString() {
        String log =  "ShardPerfenceSetting{" +
                "language='" + language + '\'' +
                ", homeTheme='" + homeTheme + '\'' +
                ", homeAnimationNum='" + homeAnimationNum + '\'' +
                ", activate='" + activate + '\'' +
                ", acticeName='" + activeName + '\'' +
                ", launchNum='" + launchNum + '\'' +
                ", arrange='" + arrange + '\'' +
                '}';
        android.util.Log.d(TAG,log);
    }
}
