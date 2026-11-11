package com.example.split_trip.database;

import android.content.Context;

import androidx.room.Room;

public class DatabaseClient {

    private static volatile AppDatabase instance;

    private DatabaseClient() {
    }

    public static AppDatabase getInstance(Context context) {

        if (instance == null) {

            synchronized (DatabaseClient.class) {

                if (instance == null) {

                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "split_trip_database"
                    ).build();

                }
            }
        }

        return instance;
    }
}
