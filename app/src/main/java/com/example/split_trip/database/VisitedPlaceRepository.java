
package com.example.split_trip.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.split_trip.models.VisitedPlace;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VisitedPlaceRepository {

    private final VisitedPlaceDao visitedPlaceDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface OnPlacesLoadedListener {
        void onLoaded(List<VisitedPlace> places);
    }

    public interface OnOperationCompleteListener {
        void onComplete();
    }

    public interface OnPlaceInsertedListener {
        void onInserted(long placeId);
    }

    public VisitedPlaceRepository(Context context) {
        AppDatabase database =
                DatabaseClient.getInstance(context);

        visitedPlaceDao = database.VisitedPlaceDao();

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insertPlace(
            VisitedPlace place,
            OnPlaceInsertedListener listener) {

        executorService.execute(() -> {
            long placeId = visitedPlaceDao.insert(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onInserted(placeId);
                }
            });
        });
    }

    public void getPlacesByTripId(
            int tripId,
            OnPlacesLoadedListener listener) {

        executorService.execute(() -> {
            List<VisitedPlace> places =
                    visitedPlaceDao.getPlacesByTripId(tripId);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onLoaded(places);
                }
            });
        });
    }

    public void updatePlace(
            VisitedPlace place,
            OnOperationCompleteListener listener) {

        executorService.execute(() -> {
            visitedPlaceDao.update(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
        });
    }

    public void deletePlace(
            VisitedPlace place,
            OnOperationCompleteListener listener) {

        executorService.execute(() -> {
            visitedPlaceDao.delete(place);

            mainHandler.post(() -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
        });
    }
}
