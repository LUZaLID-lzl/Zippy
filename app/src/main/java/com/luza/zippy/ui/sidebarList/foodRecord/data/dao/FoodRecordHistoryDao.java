package com.luza.zippy.ui.sidebarList.foodRecord.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecordHistory;

import java.util.List;

@Dao
public interface FoodRecordHistoryDao {
    @Insert
    void insert(FoodRecordHistory history);

    @Delete
    void delete(FoodRecordHistory history);

    @Query("SELECT * FROM food_record_history ORDER BY id DESC")
    LiveData<List<FoodRecordHistory>> getAllHistories();
} 