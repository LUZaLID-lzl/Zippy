package com.luza.zippy.ui.sidebarList.minecraft.location;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class LocationViewModel extends AndroidViewModel {
    private LocationRepository repository;
    private LiveData<List<LocationModel>> allLocations;

    public LocationViewModel(Application application) {
        super(application);
        repository = new LocationRepository(application);
        allLocations = repository.getAllLocations();
    }

    public LiveData<List<LocationModel>> getAllLocations() {
        return allLocations;
    }

    public void insert(LocationModel location) {
        repository.insert(location);
    }

    public void update(LocationModel location) {
        repository.update(location);
    }

    public void delete(LocationModel location) {
        repository.delete(location);
    }
} 