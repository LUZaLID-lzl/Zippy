package com.luza.zippy.ui.sidebarList.foodRecord.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_record_history")
public class FoodRecordHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String recordDate;
    private String foodRecords; // JSON格式存储食物记录列表
    
    public FoodRecordHistory(String name, String recordDate, String foodRecords) {
        this.name = name;
        this.recordDate = recordDate;
        this.foodRecords = foodRecords;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public String getFoodRecords() { return foodRecords; }
    public void setFoodRecords(String foodRecords) { this.foodRecords = foodRecords; }
} 