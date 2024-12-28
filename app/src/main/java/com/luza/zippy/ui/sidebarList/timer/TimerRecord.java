package com.luza.zippy.ui.sidebarList.timer;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "timer_records")
public class TimerRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long duration;
    private long timestamp;
    private boolean option1;
    private boolean option2;
    private boolean option3;
    private boolean isStopwatch;

    public TimerRecord(long duration, long timestamp, boolean option1, boolean option2, boolean option3, boolean isStopwatch) {
        this.duration = duration;
        this.timestamp = timestamp;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.isStopwatch = isStopwatch;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isOption1() {
        return option1;
    }

    public void setOption1(boolean option1) {
        this.option1 = option1;
    }

    public boolean isOption2() {
        return option2;
    }

    public void setOption2(boolean option2) {
        this.option2 = option2;
    }

    public boolean isOption3() {
        return option3;
    }

    public void setOption3(boolean option3) {
        this.option3 = option3;
    }

    public boolean isStopwatch() {
        return isStopwatch;
    }

    public void setStopwatch(boolean stopwatch) {
        isStopwatch = stopwatch;
    }
} 