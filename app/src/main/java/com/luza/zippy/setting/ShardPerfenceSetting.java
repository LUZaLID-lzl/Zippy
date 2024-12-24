package com.luza.zippy.setting;

import android.content.Context;
import android.content.SharedPreferences;

public class ShardPerfenceSetting {
    private static final String TAG = "ShardPerfenceSetting";
    private static final String PREF_NAME = "zippy_settings";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private String language;        //en - zh
    private String homeTheme;       //pikachu - bulbasaur - squirtle - Mewtwo


    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getHomeTheme() {
        return this.homeTheme;
    }

    public void setHomeTheme(String homeTheme) {
        this.homeTheme = homeTheme;
    }

    public void setSharedPreferences(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("language", language);
        editor.putString("homeTheme",  homeTheme);
        editor.apply();
    }

    public void getSharedPreferences(){
        sharedPreferences = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
        setLanguage(sharedPreferences.getString("language", "en"));
        setHomeTheme(sharedPreferences.getString("homeTheme", "squirtle"));
    }

    public ShardPerfenceSetting(Context context) {
        //构造函数
        this.mContext = context;
        getSharedPreferences();
    }

    public void update(){
        getSharedPreferences();
        logToString();
    }

    public void logToString() {
        String log =  "ShardPerfenceSetting{" +
                "language='" + language + '\'' +
                ", homeTheme='" + homeTheme + '\'' +
                '}';
        android.util.Log.d(TAG,log);
    }
}
