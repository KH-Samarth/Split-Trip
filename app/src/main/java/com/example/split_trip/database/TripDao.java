package com.example.split_trip.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.split_trip.models.Trip;

import java.util.List;

@Dao
public interface TripDao {

    @Insert
    long insert(Trip trip);

    @Update
    void update(Trip trip);

    @Delete
    void delete(Trip trip);

    @Query("SELECT * FROM trips ORDER BY createdAt DESC")
    List<Trip> getAllTrips();

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    Trip getTripById(int tripId);
}
