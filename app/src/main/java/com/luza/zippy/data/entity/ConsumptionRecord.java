package com.luza.zippy.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "consumption_records")
public class ConsumptionRecord {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String purpose;
    private double amount;
    private long timestamp;
    private boolean isPayback;

    public ConsumptionRecord(String purpose, double amount, long timestamp, boolean isPayback) {
        this.purpose = purpose;
        this.amount = amount;
        this.timestamp = timestamp;
        this.isPayback = isPayback;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isPayback() {
        return isPayback;
    }

    public void setPayback(boolean payback) {
        isPayback = payback;
    }
} 