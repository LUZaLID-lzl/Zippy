package com.luza.zippy.ui.sidebarList.scrummage;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;
import com.luza.zippy.ui.sidebarList.scrummage.data.repository.Repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ScrummageViewModel extends AndroidViewModel {
    private Repository repository;
    private LiveData<List<ScrummageRecord>> allRecords;
    private LiveData<List<String>> allUniquePayerNames;
    private MediatorLiveData<List<ScrummageRecord>> filteredRecords;
    
    private String currentTypeFilter;
    private String currentNameFilter;
    private Double currentMinAmount;
    private Double currentMaxAmount;
    private String currentStartDate;
    private String currentEndDate;

    public ScrummageViewModel(Application application) {
        super(application);
        repository = new Repository(application);
        allRecords = repository.getAllRecords();
        allUniquePayerNames = repository.getAllUniquePayerNames();
        
        filteredRecords = new MediatorLiveData<>();
        filteredRecords.addSource(allRecords, records -> {
            applyFilters();
        });
    }

    public LiveData<List<ScrummageRecord>> getAllRecords() {
        return filteredRecords;
    }

    public LiveData<List<String>> getAllUniquePayerNames() {
        return allUniquePayerNames;
    }

    public void insert(ScrummageRecord record) {
        repository.insert(record);
    }

    public void update(ScrummageRecord record) {
        repository.update(record);
    }

    public void delete(ScrummageRecord record) {
        repository.delete(record);
    }

    public void filterByType(String type) {
        currentTypeFilter = type;
        applyFilters();
    }

    public void filterByName(String name) {
        currentNameFilter = name;
        applyFilters();
    }

    public void filterByAmount(Double minAmount, Double maxAmount) {
        currentMinAmount = minAmount;
        currentMaxAmount = maxAmount;
        applyFilters();
    }

    public void filterByDate(String startDate, String endDate) {
        currentStartDate = startDate;
        currentEndDate = endDate;
        applyFilters();
    }

    public void clearFilter() {
        currentTypeFilter = null;
        currentNameFilter = null;
        currentMinAmount = null;
        currentMaxAmount = null;
        currentStartDate = null;
        currentEndDate = null;
        applyFilters();
    }

    private void applyFilters() {
        List<ScrummageRecord> records = allRecords.getValue();
        if (records == null) return;

        List<ScrummageRecord> filtered = records.stream()
            .filter(record -> {
                if (currentTypeFilter != null && !currentTypeFilter.equals(record.getTitle())) {
                    return false;
                }
                if (currentNameFilter != null && !currentNameFilter.equals(record.getPayer())) {
                    return false;
                }
                if (currentMinAmount != null && record.getAmount() < currentMinAmount) {
                    return false;
                }
                if (currentMaxAmount != null && record.getAmount() > currentMaxAmount) {
                    return false;
                }
                if (currentStartDate != null || currentEndDate != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        Date recordDate = sdf.parse(record.getDate().split(" ")[0]);
                        if (currentStartDate != null) {
                            Date startDate = sdf.parse(currentStartDate);
                            if (recordDate.before(startDate)) {
                                return false;
                            }
                        }
                        if (currentEndDate != null) {
                            Date endDate = sdf.parse(currentEndDate);
                            if (recordDate.after(endDate)) {
                                return false;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());

        filteredRecords.setValue(filtered);
    }

    public String getCurrentTypeFilter() {
        return currentTypeFilter;
    }

    public String getCurrentNameFilter() {
        return currentNameFilter;
    }

    public Double getCurrentMinAmount() {
        return currentMinAmount;
    }

    public Double getCurrentMaxAmount() {
        return currentMaxAmount;
    }

    public String getCurrentStartDate() {
        return currentStartDate;
    }

    public String getCurrentEndDate() {
        return currentEndDate;
    }

    // 计算每人应付金额
    public double calculatePerPerson(ScrummageRecord record) {
        if (record.getParticipants() == null || record.getParticipants().isEmpty()) {
            return 0;
        }
        String[] participants = record.getParticipants().split(",");
        return record.getAmount() / participants.length;
    }
} 