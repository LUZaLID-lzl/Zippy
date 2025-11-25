package com.luza.zippy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.luza.zippy.data.entity.ConsumptionRecord;

import java.util.List;

@Dao
public interface ConsumptionDao {
    @Insert
    void insert(ConsumptionRecord record);

    @Update
    void update(ConsumptionRecord record);

    @Delete
    void delete(ConsumptionRecord record);

    @Query("SELECT * FROM consumption_records ORDER BY timestamp DESC")
    LiveData<List<ConsumptionRecord>> getAllRecords();

    @Query("SELECT * FROM consumption_records WHERE isPayback = 0 ORDER BY timestamp DESC")
    LiveData<List<ConsumptionRecord>> getPendingRecords();

    @Query("SELECT SUM(amount) FROM consumption_records")
    LiveData<Double> getTotalAmount();

    @Query("SELECT SUM(amount) FROM consumption_records WHERE isPayback = 0")
    LiveData<Double> getPendingAmount();

    @Query("SELECT SUM(amount) FROM consumption_records " +
           "WHERE strftime('%Y-%m', datetime(timestamp/1000, 'unixepoch', 'localtime')) = strftime('%Y-%m', 'now', 'localtime')")
    LiveData<Double> getCurrentMonthAmount();

    @Query("UPDATE consumption_records SET isPayback = 1, timestamp = :currentTime WHERE isPayback = 0")
    void markAllAsPayback(long currentTime);

    @Query("SELECT MAX(timestamp) FROM consumption_records WHERE isPayback = 1")
    LiveData<Long> getLastPaybackTime();
} 