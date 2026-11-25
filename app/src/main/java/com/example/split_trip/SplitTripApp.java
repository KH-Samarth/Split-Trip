package com.example.split_trip;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SplitTripApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences preferences = getSharedPreferences("splittrip_session", MODE_PRIVATE);
        int nightMode = preferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
