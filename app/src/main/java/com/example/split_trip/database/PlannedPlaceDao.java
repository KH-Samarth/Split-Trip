package com.example.split_trip.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.split_trip.models.PlannedPlace;

import java.util.List;

@Dao
public interface PlannedPlaceDao {

    @Insert
    long insert(PlannedPlace place);

    @Update
    void update(PlannedPlace place);

    @Delete
    void delete(PlannedPlace place);

    @Query("SELECT * FROM planned_places WHERE tripId = :tripId ORDER BY plannedDate ASC")
    List<PlannedPlace> getPlannedPlacesByTripId(int tripId);
}
