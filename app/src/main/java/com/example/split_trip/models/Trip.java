package com.example.split_trip.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "trips")
public class Trip {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    private long createdAt;

    private long totalBudgetPaise;

    public Trip(String name, long createdAt, long totalBudgetPaise) {
        this.name = name;
        this.createdAt = createdAt;
        this.totalBudgetPaise = totalBudgetPaise;
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
}
