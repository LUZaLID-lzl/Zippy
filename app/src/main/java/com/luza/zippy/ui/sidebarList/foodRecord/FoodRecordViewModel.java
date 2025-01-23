package com.luza.zippy.ui.sidebarList.foodRecord;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.luza.zippy.ui.sidebarList.foodRecord.data.AppDatabase;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecordHistory;
import com.luza.zippy.ui.sidebarList.foodRecord.data.repository.FoodRecordRepository;
import com.luza.zippy.ui.utils.SingleLiveEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;

public class FoodRecordViewModel extends AndroidViewModel {
    private FoodRecordRepository repository;
    private final LiveData<List<FoodRecord>> allFoodRecords;
    private final LiveData<List<FoodPreset>> allFoodPresets;
    private final LiveData<List<FoodRecordHistory>> allHistories;
    private final SingleLiveEvent<Void> initializationEvent = new SingleLiveEvent<>();
    private boolean isInitialized = false;
    private final SingleLiveEvent<Void> reloadEvent = new SingleLiveEvent<>();

    public FoodRecordViewModel(Application application) {
        super(application);
        repository = new FoodRecordRepository(application);
        allFoodRecords = repository.getAllFoodRecords();
        allFoodPresets = repository.getAllFoodPresets();
        allHistories = repository.getAllHistories();
    }

    public void initializeIfNeeded() {
        if (!isInitialized) {
            isInitialized = true;
            AppDatabase.databaseWriteExecutor.execute(() -> {
                if (repository.getPresetCount() == 0) {
                    // insertPreset(new FoodPreset("米饭"));
                    // insertPreset(new FoodPreset("面包"));
                }
                initializationEvent.postValue(null);
            });
        }
    }

    public SingleLiveEvent<Void> getInitializationEvent() {
        return initializationEvent;
    }

    public LiveData<List<FoodRecord>> getAllFoodRecords() {
        return allFoodRecords;
    }

    public LiveData<List<FoodPreset>> getAllFoodPresets() {
        return allFoodPresets;
    }

    public void insert(FoodRecord foodRecord) {
        repository.insert(foodRecord);
    }

    public void update(FoodRecord foodRecord) {
        repository.update(foodRecord);
    }

    public void delete(FoodRecord foodRecord) {
        repository.delete(foodRecord);
    }

    public void insertPreset(FoodPreset foodPreset) {
        repository.insertPreset(foodPreset);
    }

    public void deletePreset(FoodPreset foodPreset) {
        repository.deletePreset(foodPreset);
    }

    public void triggerReload() {
        reloadEvent.setValue(null);
    }

    public SingleLiveEvent<Void> getReloadEvent() {
        return reloadEvent;
    }

    public void saveCurrentRecords(String name) {
        List<FoodRecord> currentRecords = allFoodRecords.getValue();
        if (currentRecords != null && !currentRecords.isEmpty()) {
            Gson gson = new Gson();
            String recordsJson = gson.toJson(currentRecords);
            
            String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            FoodRecordHistory history = new FoodRecordHistory(name, currentDate, recordsJson);
            
            repository.insertHistory(history);
            repository.deleteAllFoodRecords();
        }
    }

    public LiveData<List<FoodRecordHistory>> getAllHistories() {
        return allHistories;
    }

    public void deleteHistory(FoodRecordHistory history) {
        repository.deleteHistory(history);
    }
} 