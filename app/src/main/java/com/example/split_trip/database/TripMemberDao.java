
package com.example.split_trip.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.split_trip.models.TripMember;

import java.util.List;

@Dao
public interface TripMemberDao {

    @Insert
    void insert(TripMember member);

    @Update
    void update(TripMember member);

    @Delete
    void delete(TripMember member);

    @Query("SELECT * FROM trip_members WHERE tripId = :tripId ORDER BY id ASC")
    List<TripMember> getMembersForTrip(int tripId);

    @Query("SELECT COUNT(*) FROM trip_members WHERE tripId = :tripId")
    int getMemberCount(int tripId);
}
