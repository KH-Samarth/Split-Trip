
package com.example.split_trip.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.split_trip.models.VisitedPlace;

import java.util.List;

@Dao
public interface VisitedPlaceDao {

    @Insert
    long insert(VisitedPlace place);

    @Update
    void update(VisitedPlace place);

    @Delete
    void delete(VisitedPlace place);

    @Query("SELECT * FROM visited_places WHERE tripId = :tripId ORDER BY visitDate DESC")
    List<VisitedPlace> getPlacesByTripId(int tripId);
}
