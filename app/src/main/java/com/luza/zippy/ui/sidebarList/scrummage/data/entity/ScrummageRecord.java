package com.luza.zippy.ui.sidebarList.scrummage.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "scrummage_records")
public class ScrummageRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private double amount;
    private String date;
    private String participants;
    private String payer;
    private String notes;

    public ScrummageRecord(String title, double amount, String date, String participants, String payer, String notes) {
        this.title = title;
        this.amount = amount;
        this.date = date;
        this.participants = participants;
        this.payer = payer;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getParticipants() {
        return participants;
    }

    public void setParticipants(String participants) {
        this.participants = participants;
    }

    public String getPayer() {
        return payer;
    }

    public void setPayer(String payer) {
        this.payer = payer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScrummageRecord that = (ScrummageRecord) o;
        return id == that.id &&
                Double.compare(that.amount, amount) == 0 &&
                Objects.equals(title, that.title) &&
                Objects.equals(date, that.date) &&
                Objects.equals(participants, that.participants) &&
                Objects.equals(payer, that.payer) &&
                Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, amount, date, participants, payer, notes);
    }
} 