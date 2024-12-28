package com.luza.zippy.ui.sidebarList.calendar.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.luza.zippy.ui.sidebarList.calendar.Birthday;

@Database(
    entities = {Birthday.class}, 
    version = 3,
    exportSchema = false
)
public abstract class BirthdayDatabase extends RoomDatabase {
    private static BirthdayDatabase instance;
    public abstract BirthdayDao birthdayDao();

    public static synchronized BirthdayDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                BirthdayDatabase.class,
                "birthday_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
} 