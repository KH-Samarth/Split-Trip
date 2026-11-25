package com.example.split_trip.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "planned_places",
        foreignKeys = @ForeignKey(
                entity = Trip.class,
                parentColumns = "id",
                childColumns = "tripId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("tripId")}
)
public class PlannedPlace {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int tripId;
    private String name;
    private String location;
    private long plannedDate;
    private String note;

    public PlannedPlace(int tripId, String name, String location,
                        long plannedDate, String note) {
        this.tripId = tripId;
        this.name = name;
        this.location = location;
        this.plannedDate = plannedDate;
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(long plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
