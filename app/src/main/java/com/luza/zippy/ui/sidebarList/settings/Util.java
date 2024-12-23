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

}
