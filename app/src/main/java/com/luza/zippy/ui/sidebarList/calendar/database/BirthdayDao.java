package com.luza.zippy.ui.sidebarList.calendar.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import com.luza.zippy.ui.sidebarList.calendar.Birthday;

@Dao
public interface BirthdayDao {
    @Query("SELECT * FROM birthday")
    List<Birthday> getAll();

    @Insert
    long insert(Birthday birthday);

    @Update
    void update(Birthday birthday);

    @Delete
    void delete(Birthday birthday);

    @Query("SELECT * FROM birthday WHERE month = :month AND day = :day")
    List<Birthday> getBirthdaysByDate(int month, int day);
} 