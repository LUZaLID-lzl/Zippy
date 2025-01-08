package com.luza.zippy.ui.sidebarList.foodRecord.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_records")
public class FoodRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String foodName;
    private int servings;  // 份数

    public FoodRecord(String foodName, int servings) {
        this.foodName = foodName;
        this.servings = servings;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }
} 