package com.luza.zippy.ui.sidebarList.scrummage;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScrummageTypeViewModel extends ViewModel {
    private MutableLiveData<List<String>> scrummageTypes;

    public ScrummageTypeViewModel() {
        scrummageTypes = new MutableLiveData<>(new ArrayList<>(Arrays.asList("结婚", "住房", "白事")));
    }

    public LiveData<List<String>> getScrummageTypes() {
        return scrummageTypes;
    }

    public void addType(String type) {
        List<String> currentTypes = new ArrayList<>(scrummageTypes.getValue());
        currentTypes.add(type);
        scrummageTypes.setValue(currentTypes);
    }

    public void updateType(int position, String newType) {
        List<String> currentTypes = new ArrayList<>(scrummageTypes.getValue());
        currentTypes.set(position, newType);
        scrummageTypes.setValue(currentTypes);
    }

    public void deleteType(int position) {
        List<String> currentTypes = new ArrayList<>(scrummageTypes.getValue());
        currentTypes.remove(position);
        scrummageTypes.setValue(currentTypes);
    }
} 