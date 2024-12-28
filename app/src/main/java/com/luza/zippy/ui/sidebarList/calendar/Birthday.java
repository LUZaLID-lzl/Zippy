package com.luza.zippy.ui.sidebarList.calendar;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "birthday")
public class Birthday {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private int year;
    private int month;
    private int day;
    private boolean isLunar;

    public int getLunarMonth() {
        return lunarMonth;
    }

    public void setLunarMonth(int lunarMonth) {
        this.lunarMonth = lunarMonth;
    }

    public int getLunarDay() {
        return lunarDay;
    }

    public void setLunarDay(int lunarDay) {
        this.lunarDay = lunarDay;
    }

    private int lunarMonth;
    private int lunarDay;

    public Birthday(String name, int year, int month, int day, boolean isLunar) {
        this.name = name;
        this.year = year;
        this.month = month;
        this.day = day;
        this.isLunar = isLunar;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }
    public boolean isLunar() { return isLunar; }
    public void setLunar(boolean lunar) { isLunar = lunar; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
} 