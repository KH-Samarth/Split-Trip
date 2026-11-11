package com.example.split_trip;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.split_trip.database.TripRepository;
import com.example.split_trip.models.Trip;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TripRepository tripRepository;
    private LinearLayout tripListContainer;

    private ScrollView homeScreen;
    private ScrollView tripsScreen;
    private ScrollView profileScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tripRepository = new TripRepository(this);

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

                    Trip newTrip = new Trip(
                            newTripName,
                            System.currentTimeMillis(),
                            0L
                    );

                    tripRepository.insertTrip(newTrip, tripId -> {

                        Toast.makeText(this,
                                "Trip Created!",
                                Toast.LENGTH_SHORT).show();

                        refreshTripList();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshTripList() {

        tripRepository.getAllTrips(trips -> {

            tripListContainer.removeAllViews();

            LayoutInflater inflater =
                    LayoutInflater.from(this);

            for (Trip trip : trips) {

                View tripCard = inflater.inflate(
                        R.layout.item_trip,
                        tripListContainer,
                        false
                );

                TextView tripNameText =
                        tripCard.findViewById(R.id.tripNameText);

                tripNameText.setText(trip.getName());

                tripCard.setOnClickListener(v -> {

                    Intent intent =
                            new Intent(HomeActivity.this, MainActivity.class);

                    intent.putExtra("TRIP_ID", trip.getId());
                    intent.putExtra("TRIP_TITLE", trip.getName());

                    startActivity(intent);
                });

                tripListContainer.addView(tripCard);
            }
        });
    }
}
