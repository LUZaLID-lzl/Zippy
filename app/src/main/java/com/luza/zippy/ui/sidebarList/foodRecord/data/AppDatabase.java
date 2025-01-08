package com.luza.zippy.ui.sidebarList.foodRecord.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodPresetDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodRecordDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodRecordHistoryDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecordHistory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {FoodRecord.class, FoodPreset.class, FoodRecordHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FoodRecordDao foodRecordDao();
    public abstract FoodPresetDao foodPresetDao();
    public abstract FoodRecordHistoryDao foodRecordHistoryDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "food_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
} 