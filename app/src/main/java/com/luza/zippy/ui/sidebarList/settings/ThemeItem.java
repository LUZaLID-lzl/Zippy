package com.luza.zippy.ui.sidebarList.settings;

public class ThemeItem {
    public String name;
    public String description;
    public int imageRes;
    public int backgroundColor;
    public boolean isEnabled;

    ThemeItem(String name, String description, int imageRes, int backgroundColor) {
        this.name = name;
        this.description = description;
        this.imageRes = imageRes;
        this.backgroundColor = backgroundColor;
        this.isEnabled = false;
    }
}
