package com.luza.zippy.ui.sidebarList.foodRecord.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;

import java.util.List;

@Dao
public interface FoodRecordDao {
    @Insert
    void insert(FoodRecord foodRecord);

    @Update
    void update(FoodRecord foodRecord);

    @Delete
    void delete(FoodRecord foodRecord);

    @Query("SELECT * FROM food_records")
    LiveData<List<FoodRecord>> getAllFoodRecords();

    @Query("DELETE FROM food_records")
    void deleteAll();
} 