
package com.example.split_trip.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "trip_members",
        foreignKeys = @ForeignKey(
                entity = Trip.class,
                parentColumns = "id",
                childColumns = "tripId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("tripId")}
)
public class TripMember {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int tripId;
    private String name;
    private String email;
    private boolean creator;

    public TripMember(int tripId, String name, String email, boolean creator) {
        this.tripId = tripId;
        this.name = name;
        this.email = email;
        this.creator = creator;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isCreator() {
        return creator;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }
}
