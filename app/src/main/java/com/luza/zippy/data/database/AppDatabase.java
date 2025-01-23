package com.luza.zippy.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.luza.zippy.data.dao.ScrummageRecordDao;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;

@Database(entities = {ScrummageRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ScrummageRecordDao scrummageRecordDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "app_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
} 