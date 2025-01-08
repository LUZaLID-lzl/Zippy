package com.luza.zippy.ui.sidebarList.foodRecord.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;

import java.util.List;

@Dao
public interface FoodPresetDao {
    @Insert
    void insert(FoodPreset foodPreset);

    @Update
    void update(FoodPreset foodPreset);

    @Delete
    void delete(FoodPreset foodPreset);

    @Query("SELECT * FROM food_presets ORDER BY foodName ASC")
    LiveData<List<FoodPreset>> getAllFoodPresets();

    @Query("SELECT COUNT(*) FROM food_presets")
    int getPresetCount();
} 