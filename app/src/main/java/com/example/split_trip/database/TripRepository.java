package com.example.split_trip.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.split_trip.models.Trip;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripRepository {

    private final TripDao tripDao;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    public TripRepository(Context context) {

        AppDatabase database =
                DatabaseClient.getInstance(context);

        tripDao = database.tripDao();
    }

    public interface OnTripInsertedListener {
        void onInserted(long tripId);
    }

    public interface OnTripsLoadedListener {
        void onLoaded(List<Trip> trips);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public void insertTrip(
            Trip trip,
            OnTripInsertedListener listener
    ) {

        executor.execute(() -> {

            long tripId = tripDao.insert(trip);

            if (listener != null) {
                mainHandler.post(() ->
                        listener.onInserted(tripId)
                );
            }
        });
    }

    public void getAllTrips(
            OnTripsLoadedListener listener
    ) {

        executor.execute(() -> {

            List<Trip> trips = tripDao.getAllTrips();

            if (listener != null) {
                mainHandler.post(() ->
                        listener.onLoaded(trips)
                );
            }
        });
    }

    public void updateTrip(
            Trip trip,
            OnOperationCompleteListener listener
    ) {

        executor.execute(() -> {

            tripDao.update(trip);

            if (listener != null) {
                mainHandler.post(listener::onComplete);
            }
        });
    }

    public void deleteTrip(
            Trip trip,
            OnOperationCompleteListener listener
    ) {

        executor.execute(() -> {

            tripDao.delete(trip);

            if (listener != null) {
                mainHandler.post(listener::onComplete);
            }
        });
    }
}
