package com.luza.zippy.ui.sidebarList.scrummage.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;

import java.util.List;

@Dao
public interface ScrummageRecordDao {
    @Query("SELECT * FROM scrummage_records ORDER BY date DESC")
    LiveData<List<ScrummageRecord>> getAllRecords();

    @Query("SELECT DISTINCT payer FROM scrummage_records ORDER BY payer ASC")
    LiveData<List<String>> getAllUniquePayerNames();

    @Insert
    void insert(ScrummageRecord record);

    @Update
    void update(ScrummageRecord record);

    @Delete
    void delete(ScrummageRecord record);
} 