package com.luza.zippy.ui.sidebarList.scrummage.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.luza.zippy.ui.sidebarList.scrummage.data.dao.ScrummageRecordDao;
import com.luza.zippy.ui.sidebarList.scrummage.data.database.AppDatabase;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;

import java.util.List;

public class Repository {
    private ScrummageRecordDao scrummageRecordDao;
    private LiveData<List<ScrummageRecord>> allRecords;
    private LiveData<List<String>> allUniquePayerNames;

    public Repository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        scrummageRecordDao = db.scrummageRecordDao();
        allRecords = scrummageRecordDao.getAllRecords();
        allUniquePayerNames = scrummageRecordDao.getAllUniquePayerNames();
    }

    public LiveData<List<ScrummageRecord>> getAllRecords() {
        return allRecords;
    }

    public LiveData<List<String>> getAllUniquePayerNames() {
        return allUniquePayerNames;
    }

    public void insert(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageRecordDao.insert(record);
        });
    }

    public void update(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageRecordDao.update(record);
        });
    }

    public void delete(ScrummageRecord record) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            scrummageRecordDao.delete(record);
        });
    }
} 