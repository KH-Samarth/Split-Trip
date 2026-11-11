package com.example.split_trip.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.split_trip.models.Expense;
import com.example.split_trip.models.Trip;

@Database(
        entities = {Trip.class, Expense.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TripDao tripDao();

    public abstract ExpenseDao expenseDao();
}
