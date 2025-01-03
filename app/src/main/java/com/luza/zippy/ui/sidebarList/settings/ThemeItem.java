package com.luza.zippy.ui.sidebarList.settings;

import android.graphics.Bitmap;

public class ThemeItem {
    public String name;
    public String description;
    public Bitmap imageRes;
    public int backgroundColor;
    public boolean isEnabled;

    ThemeItem(String name, String description, Bitmap imageRes, int backgroundColor) {
        this.name = name;
        this.description = description;
        this.imageRes = imageRes;
        this.backgroundColor = backgroundColor;
        this.isEnabled = false;
    }
}
