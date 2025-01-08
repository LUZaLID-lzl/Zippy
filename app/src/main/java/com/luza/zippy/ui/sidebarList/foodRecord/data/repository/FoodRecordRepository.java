package com.luza.zippy.ui.sidebarList.foodRecord.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.luza.zippy.ui.sidebarList.foodRecord.data.AppDatabase;
import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodPresetDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodRecordDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.dao.FoodRecordHistoryDao;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecordHistory;

import java.util.List;

public class FoodRecordRepository {
    private FoodRecordDao foodRecordDao;
    private FoodPresetDao foodPresetDao;
    private FoodRecordHistoryDao foodRecordHistoryDao;
    private LiveData<List<FoodRecord>> allFoodRecords;
    private LiveData<List<FoodPreset>> allFoodPresets;
    private LiveData<List<FoodRecordHistory>> allHistories;

    public FoodRecordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        foodRecordDao = db.foodRecordDao();
        foodPresetDao = db.foodPresetDao();
        foodRecordHistoryDao = db.foodRecordHistoryDao();
        allFoodRecords = foodRecordDao.getAllFoodRecords();
        allFoodPresets = foodPresetDao.getAllFoodPresets();
        allHistories = foodRecordHistoryDao.getAllHistories();
    }

    public LiveData<List<FoodRecord>> getAllFoodRecords() {
        return allFoodRecords;
    }

    public LiveData<List<FoodPreset>> getAllFoodPresets() {
        return allFoodPresets;
    }

    public void insert(FoodRecord foodRecord) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordDao.insert(foodRecord);
        });
    }

    public void update(FoodRecord foodRecord) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordDao.update(foodRecord);
        });
    }

    public void delete(FoodRecord foodRecord) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordDao.delete(foodRecord);
        });
    }

    public void insertPreset(FoodPreset foodPreset) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodPresetDao.insert(foodPreset);
        });
    }

    public void deletePreset(FoodPreset foodPreset) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodPresetDao.delete(foodPreset);
        });
    }

    public int getPresetCount() {
        return foodPresetDao.getPresetCount();
    }

    public void insertHistory(FoodRecordHistory history) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordHistoryDao.insert(history);
        });
    }

    public void deleteHistory(FoodRecordHistory history) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordHistoryDao.delete(history);
        });
    }

    public LiveData<List<FoodRecordHistory>> getAllHistories() {
        return allHistories;
    }

    public void deleteAllFoodRecords() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            foodRecordDao.deleteAll();
        });
    }
} 