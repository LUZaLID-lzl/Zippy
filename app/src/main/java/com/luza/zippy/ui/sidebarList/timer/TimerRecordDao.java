package com.luza.zippy.ui.sidebarList.timer;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TimerRecordDao {
    @Insert
    void insert(TimerRecord record);

    @Delete
    void delete(TimerRecord record);

    @Query("SELECT * FROM timer_records ORDER BY timestamp DESC")
    LiveData<List<TimerRecord>> getAllRecords();

    @Query("DELETE FROM timer_records")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM timer_records")
    int getCount();

    // 获取指定类型的最长时间（考虑选项）
    @Query("SELECT MAX(duration) FROM timer_records WHERE isStopwatch = :isStopwatch AND " +
           "(:optionType = 1 AND option1 = 1 OR :optionType = 2 AND option2 = 1 OR :optionType = 3 AND option3 = 1)")
    long getLongestDurationByOption(boolean isStopwatch, int optionType);

    // 获取指定类型的最短时间（考虑选项）
    @Query("SELECT MIN(duration) FROM timer_records WHERE isStopwatch = :isStopwatch AND " +
           "(:optionType = 1 AND option1 = 1 OR :optionType = 2 AND option2 = 1 OR :optionType = 3 AND option3 = 1)")
    long getShortestDurationByOption(boolean isStopwatch, int optionType);

    // 获取指定类型的平均时间（考虑选项）
    @Query("SELECT AVG(duration) FROM timer_records WHERE isStopwatch = :isStopwatch AND " +
           "(:optionType = 1 AND option1 = 1 OR :optionType = 2 AND option2 = 1 OR :optionType = 3 AND option3 = 1)")
    long getAverageDurationByOption(boolean isStopwatch, int optionType);

    // 获取指定类型的记录总数（考虑选项）
    @Query("SELECT COUNT(*) FROM timer_records WHERE isStopwatch = :isStopwatch AND " +
           "(:optionType = 1 AND option1 = 1 OR :optionType = 2 AND option2 = 1 OR :optionType = 3 AND option3 = 1)")
    int getCountByOption(boolean isStopwatch, int optionType);

    // 获取倒计时模式的统计信息
    @Query("SELECT MAX(duration) FROM timer_records WHERE isStopwatch = 0")
    long getLongestDuration();

    @Query("SELECT MIN(duration) FROM timer_records WHERE isStopwatch = 0")
    long getShortestDuration();

    @Query("SELECT AVG(duration) FROM timer_records WHERE isStopwatch = 0")
    long getAverageDuration();

    @Query("SELECT COUNT(*) FROM timer_records WHERE isStopwatch = 0")
    int getCountByType();
} 