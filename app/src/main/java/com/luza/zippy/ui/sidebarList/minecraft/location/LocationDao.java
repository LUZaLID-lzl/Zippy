package com.luza.zippy.ui.sidebarList.minecraft.location;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(LocationModel location);

    @Update
    void update(LocationModel location);

    @Delete
    void delete(LocationModel location);

    @Query("SELECT * FROM locations")
    LiveData<List<LocationModel>> getAllLocations();
} 