package com.luza.zippy.ui.sidebarList.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.luza.zippy.MainActivity;
import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;

import java.util.Locale;

public class Util {
    public void updateLocale(Context context){
        ShardPerfenceSetting shardPerfenceSetting = new ShardPerfenceSetting(context);
        String language = shardPerfenceSetting.getLanguage();
        android.util.Log.d("ShardPerfenceSetting","language: " + language);
        Locale locale = new Locale(language);
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public void updateTheme(Activity activity) {
        ShardPerfenceSetting shardPerfenceSetting = new ShardPerfenceSetting(activity.getBaseContext());
        String theme = shardPerfenceSetting.getHomeTheme();
        Log.d("liziluo","activity.getComponentName() : " + activity.getComponentName());
        if (activity.getComponentName().toString().contains("SplashActivity")){
            switch (theme) {
                case "pikachu":
                    activity.setTheme(R.style.PikachuSplashTheme);
                    break;
                case "bulbasaur":
                    activity.setTheme(R.style.BulbasaurSplashTheme);
                    break;
                default:
            }
        }else if (activity.getComponentName().toString().contains("MainActivity")){
            if (MainActivity.needRecreate){
                switch (theme) {
                    case "pikachu":
                        Log.d("liziluo","pikachu : ");
                        activity.setTheme(R.style.PikachuTheme);
                        break;
                    case "bulbasaur":
                        Log.d("liziluo","bulbasaur : ");
                        activity.setTheme(R.style.BulbasaurTheme);
                        break;
                    default:
                        activity.setTheme(R.style.PikachuTheme);
                        break;
                }
                MainActivity.needRecreate = false;
            }
        }
    }

}
