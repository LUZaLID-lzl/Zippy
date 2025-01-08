package com.luza.zippy.ui.sidebarList.foodRecord.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_presets")
public class FoodPreset {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String foodName;

    public FoodPreset(String foodName) {
        this.foodName = foodName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
} 