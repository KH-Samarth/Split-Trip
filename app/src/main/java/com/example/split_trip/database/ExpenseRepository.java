package com.example.split_trip.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.split_trip.models.Expense;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {

    private final ExpenseDao expenseDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public ExpenseRepository(Context context) {

        AppDatabase database =
                DatabaseClient.getInstance(context);

        expenseDao = database.expenseDao();
    }

    public interface OnExpensesLoadedListener {
        void onLoaded(List<Expense> expenses);
    }

    public interface OnTotalLoadedListener {
        void onLoaded(long totalPaise);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public interface OnExpenseInsertedListener {
        void onInserted(long expenseId);
    }

    public void insertExpense(
            Expense expense,
            OnExpenseInsertedListener listener
    ) {

        executor.execute(() -> {

            long expenseId = expenseDao.insert(expense);

            if (listener != null) {
                mainHandler.post(() ->
                        listener.onInserted(expenseId)
                );
            }
        });
    }

    public void getExpensesForTrip(
            int tripId,
            OnExpensesLoadedListener listener
    ) {

        executor.execute(() -> {

            List<Expense> expenses =
                    expenseDao.getExpensesForTrip(tripId);

            if (listener != null) {
                mainHandler.post(() ->
                        listener.onLoaded(expenses)
                );
            }
        });
    }

    public void getTotalExpensesForTrip(
            int tripId,
            OnTotalLoadedListener listener
    ) {

        executor.execute(() -> {

            Long total = expenseDao.getTotalExpensesForTrip(tripId);

            long totalPaise = total == null ? 0L : total;

            if (listener != null) {
                mainHandler.post(() ->
                        listener.onLoaded(totalPaise)
                );
            }
        });
    }

    public void updateExpense(
            Expense expense,
            OnOperationCompleteListener listener
    ) {

        executor.execute(() -> {

            expenseDao.update(expense);

            if (listener != null) {
                mainHandler.post(listener::onComplete);
            }
        });
    }

    public void deleteExpense(
            Expense expense,
            OnOperationCompleteListener listener
    ) {

        executor.execute(() -> {

            expenseDao.delete(expense);

            if (listener != null) {
                mainHandler.post(listener::onComplete);
            }
        });
    }

    public void deleteExpensesForTrip(
            int tripId,
            OnOperationCompleteListener listener
    ) {

        executor.execute(() -> {

            expenseDao.deleteExpensesForTrip(tripId);

            if (listener != null) {
                mainHandler.post(listener::onComplete);
            }
        });
    }
}
