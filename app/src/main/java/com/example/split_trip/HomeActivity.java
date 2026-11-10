
package com.example.split_trip;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private LinearLayout tripListContainer;

    private ScrollView homeScreen;
    private ScrollView tripsScreen;
    private ScrollView profileScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        prefs = getSharedPreferences("SplitTripData", MODE_PRIVATE);

        tripListContainer = findViewById(R.id.tripListContainer);

        Button newTripButton = findViewById(R.id.newTripButton);
        newTripButton.setOnClickListener(v -> showCreateTripDialog());

        homeScreen = findViewById(R.id.home_screen);
        tripsScreen = findViewById(R.id.trips_screen);
        profileScreen = findViewById(R.id.profile_screen);

        BottomNavigationView bottomNavigation =
                findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                showScreen(homeScreen);
                return true;
            } else if (id == R.id.nav_trips) {
                showScreen(tripsScreen);
                refreshTripList();
                return true;
            } else if (id == R.id.nav_profile) {
                showScreen(profileScreen);
                return true;
            }

            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void showScreen(View selectedScreen) {
        homeScreen.setVisibility(View.GONE);
        tripsScreen.setVisibility(View.GONE);
        profileScreen.setVisibility(View.GONE);

        selectedScreen.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTripList();
    }

    private void showCreateTripDialog() {

        EditText input = new EditText(this);
        input.setHint("e.g. Tokyo 2027");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(this)
                .setTitle("Create New Trip")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {

                    String newTripName =
                            input.getText().toString().trim();

                    if (newTripName.isEmpty()) {
                        Toast.makeText(this,
                                "Trip name cannot be empty",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String currentTrips =
                            prefs.getString("TRIPS_LIST", "");

                    if (currentTrips.isEmpty()) {
                        currentTrips = newTripName;
                    } else {
                        currentTrips += "," + newTripName;
                    }

                    prefs.edit()
                            .putString("TRIPS_LIST", currentTrips)
                            .apply();

                    refreshTripList();

                    Toast.makeText(this,
                            "Trip Created!",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshTripList() {

        tripListContainer.removeAllViews();

        String savedTrips = prefs.getString("TRIPS_LIST", "");

        if (savedTrips.isEmpty()) {
            return;
        }

        ArrayList<String> myTripsList =
                new ArrayList<>(Arrays.asList(savedTrips.split(",")));

        for (String tripName : myTripsList) {

            if (tripName.trim().isEmpty()) {
                continue;
            }

            Button dynamicButton = new Button(this);
            dynamicButton.setText(tripName);
            dynamicButton.setAllCaps(false);
            dynamicButton.setTextSize(16);

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            params.setMargins(0, 0, 0, 12);
            dynamicButton.setLayoutParams(params);

            dynamicButton.setOnClickListener(v -> {

                Intent intent =
                        new Intent(HomeActivity.this, MainActivity.class);

                intent.putExtra("TRIP_TITLE", tripName);
                startActivity(intent);
            });

            tripListContainer.addView(dynamicButton);
        }
    }
}
