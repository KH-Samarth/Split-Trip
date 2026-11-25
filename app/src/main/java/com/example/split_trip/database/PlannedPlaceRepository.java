package com.example.split_trip.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.split_trip.models.PlannedPlace;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlannedPlaceRepository {

    private final PlannedPlaceDao plannedPlaceDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnPlacesLoadedListener {
        void onLoaded(List<PlannedPlace> places);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public interface OnPlaceInsertedListener {
        void onInserted(long placeId);
    }

    public PlannedPlaceRepository(Context context) {
        AppDatabase database =
                DatabaseClient.getInstance(context);

        plannedPlaceDao = database.plannedPlaceDao();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insertPlace(
            PlannedPlace place,
            OnPlaceInsertedListener listener) {

        executorService.execute(() -> {
            long placeId = plannedPlaceDao.insert(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onInserted(placeId);
                }
            });
        });
    }

    public void getPlannedPlacesByTripId(
            int tripId,
            OnPlacesLoadedListener listener) {

        executorService.execute(() -> {
            List<PlannedPlace> places =
                    plannedPlaceDao.getPlannedPlacesByTripId(tripId);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(places);
                }
            });
        });
    }

    public void updatePlace(
            PlannedPlace place,
            OnOperationCompleteListener listener) {

        executorService.execute(() -> {
            plannedPlaceDao.update(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
        });
    }

    public void deletePlace(
            PlannedPlace place,
            OnOperationCompleteListener listener) {

        executorService.execute(() -> {
            plannedPlaceDao.delete(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
        });
    }
}
