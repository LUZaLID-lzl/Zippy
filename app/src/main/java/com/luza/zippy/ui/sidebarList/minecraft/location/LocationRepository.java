package com.luza.zippy.ui.sidebarList.minecraft.location;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationRepository {
    private LocationDao locationDao;
    private LiveData<List<LocationModel>> allLocations;
    private ExecutorService executorService;

    public LocationRepository(Application application) {
        LocationDatabase db = LocationDatabase.getDatabase(application);
        locationDao = db.locationDao();
        allLocations = locationDao.getAllLocations();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<LocationModel>> getAllLocations() {
        return allLocations;
    }

    public void insert(LocationModel location) {
        executorService.execute(() -> locationDao.insert(location));
    }

    public void update(LocationModel location) {
        executorService.execute(() -> locationDao.update(location));
    }

    public void delete(LocationModel location) {
        executorService.execute(() -> locationDao.delete(location));
    }
} 