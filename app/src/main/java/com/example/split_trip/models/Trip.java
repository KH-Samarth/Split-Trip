package com.example.split_trip.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private long createdAt;

    private long totalBudgetPaise;

    private long startDate;

    private long endDate;

    public Trip(String name, long createdAt, long totalBudgetPaise, long startDate, long endDate) {
        this.name = name;
        this.createdAt = createdAt;
        this.totalBudgetPaise = totalBudgetPaise;
        this.startDate = normalizeToMidnight(startDate);
        this.endDate = normalizeToMidnight(endDate);
    }

    @Ignore
    public Trip(String name, long createdAt, long totalBudgetPaise) {
        this.name = name;
        this.createdAt = createdAt;
        this.totalBudgetPaise = totalBudgetPaise;
        long normalized = normalizeToMidnight(createdAt);
        this.startDate = normalized;
        this.endDate = normalized;
    }

    private long normalizeToMidnight(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getTotalBudgetPaise() {
        return totalBudgetPaise;
    }

    public void setTotalBudgetPaise(long totalBudgetPaise) {
        this.totalBudgetPaise = totalBudgetPaise;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = normalizeToMidnight(startDate);
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = normalizeToMidnight(endDate);
    }
}
