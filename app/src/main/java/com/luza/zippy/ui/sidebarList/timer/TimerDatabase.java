package com.luza.zippy.ui.sidebarList.timer;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {TimerRecord.class}, version = 2)
public abstract class TimerDatabase extends RoomDatabase {
    private static volatile TimerDatabase INSTANCE;
    public abstract TimerRecordDao timerRecordDao();

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 添加 isStopwatch 列，默认值为 true（表示正计时）
            database.execSQL("ALTER TABLE timer_records ADD COLUMN isStopwatch INTEGER NOT NULL DEFAULT 1");
        }
    };

    public static TimerDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TimerDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TimerDatabase.class, "timer_database")
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static TimerDatabase getDatabase(Context context) {
        return getInstance(context);
    }
} 