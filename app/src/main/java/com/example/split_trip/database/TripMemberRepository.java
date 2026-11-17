
package com.example.split_trip.database;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.split_trip.models.TripMember;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TripMemberRepository {

    private final TripMemberDao tripMemberDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public TripMemberRepository(Context context) {
        AppDatabase database = DatabaseClient.getInstance(context);
        tripMemberDao = database.tripMemberDao();

        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insertMember(TripMember member, Runnable callback) {
        executor.execute(() -> {
            tripMemberDao.insert(member);
            mainHandler.post(callback);
        });
    }

    public void getMembersForTrip(int tripId, MemberListCallback callback) {
        executor.execute(() -> {
            List<TripMember> members =
                    tripMemberDao.getMembersForTrip(tripId);

            mainHandler.post(() -> callback.onResult(members));
        });
    }

    public void deleteMember(TripMember member, Runnable callback) {
        executor.execute(() -> {
            tripMemberDao.delete(member);
            mainHandler.post(callback);
        });
    }

    public interface MemberListCallback {
        void onResult(List<TripMember> members);
    }
}
