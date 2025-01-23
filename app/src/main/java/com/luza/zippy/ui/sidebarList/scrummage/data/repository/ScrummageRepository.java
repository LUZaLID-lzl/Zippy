package com.luza.zippy.ui.sidebarList.scrummage.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.luza.zippy.ui.sidebarList.scrummage.data.dao.ScrummageDao;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;
import com.luza.zippy.ui.sidebarList.foodRecord.data.AppDatabase;

import java.util.List;

public class ScrummageRepository {
    private ScrummageDao scrummageDao;
    private LiveData<List<ScrummageRecord>> allRecords;

    public ScrummageRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        scrummageDao = db.scrummageDao();
        allRecords = scrummageDao.getAllRecords();
    }

    public LiveData<List<ScrummageRecord>> getAllRecords() {
        return allRecords;
    }

    public void insert(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageDao.insert(record);
        });
    }

    public void update(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageDao.update(record);
        });
    }

    public void delete(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageDao.delete(record);
        });
    }

    public void deleteAllRecords() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageDao.deleteAllRecords();
        });
    }
} 