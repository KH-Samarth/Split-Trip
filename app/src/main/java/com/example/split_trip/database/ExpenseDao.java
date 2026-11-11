package com.example.split_trip.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.split_trip.models.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses WHERE tripId = :tripId ORDER BY createdAt DESC")
    List<Expense> getExpensesForTrip(int tripId);

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    Expense getExpenseById(int expenseId);

    @Query("SELECT SUM(amountPaise) FROM expenses WHERE tripId = :tripId")
    Long getTotalExpensesForTrip(int tripId);

    @Query("DELETE FROM expenses WHERE tripId = :tripId")
    void deleteExpensesForTrip(int tripId);
}
