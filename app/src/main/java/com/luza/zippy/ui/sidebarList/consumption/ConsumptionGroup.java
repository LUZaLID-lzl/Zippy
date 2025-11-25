package com.luza.zippy.ui.sidebarList.consumption;

import com.luza.zippy.data.entity.ConsumptionRecord;

import java.util.List;

public class ConsumptionGroup {
    private String date;
    private List<ConsumptionRecord> records;

    public ConsumptionGroup(String date, List<ConsumptionRecord> records) {
        this.date = date;
        this.records = records;
    }

    public String getDate() {
        return date;
    }

    public List<ConsumptionRecord> getRecords() {
        return records;
    }
} 