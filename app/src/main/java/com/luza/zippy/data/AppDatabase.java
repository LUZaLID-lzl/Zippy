package com.luza.zippy.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.luza.zippy.data.dao.ConsumptionDao;
import com.luza.zippy.data.entity.ConsumptionRecord;

@Database(entities = {ConsumptionRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ConsumptionDao consumptionDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "consumption_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
} 