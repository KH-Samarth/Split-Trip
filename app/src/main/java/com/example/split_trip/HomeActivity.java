package com.example.split_trip;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TripRepository tripRepository;
    private LinearLayout tripListContainer;
    private TextView dashboardEmptyText;
    private LinearLayout dashboardRecentTripsContainer;

    private ScrollView homeScreen;
    private ScrollView tripsScreen;
    private ScrollView profileScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tripRepository = new TripRepository(this);

        tripListContainer = findViewById(R.id.tripListContainer);
        dashboardEmptyText = findViewById(R.id.dashboardEmptyText);
        dashboardRecentTripsContainer =
                findViewById(R.id.dashboardRecentTripsContainer);

        Button newTripButton = findViewById(R.id.newTripButton);
        newTripButton.setOnClickListener(v -> showCreateTripDialog());

        homeScreen = findViewById(R.id.home_screen);
        tripsScreen = findViewById(R.id.trips_screen);
        profileScreen = findViewById(R.id.profile_screen);

        Button logoutButton = findViewById(R.id.logoutButton);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> performLogout());
        }

        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView aboutVersionText = findViewById(R.id.aboutVersionText);
            if (aboutVersionText != null) {
                aboutVersionText.setText("Version " + versionName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        refreshDashboard();
    }

    private void showCreateTripDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_trip, null);

        EditText nameInput = dialogView.findViewById(R.id.dialogTripNameInput);
        Button startDateButton = dialogView.findViewById(R.id.dialogStartDateButton);
        Button endDateButton = dialogView.findViewById(R.id.dialogEndDateButton);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button createButton = dialogView.findViewById(R.id.dialogCreateButton);

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayMillis = todayCal.getTimeInMillis();

        final long[] startDateMillis = {todayMillis};
        final long[] endDateMillis = {todayMillis + 3L * 24 * 60 * 60 * 1000L};

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        startDateButton.setText("Start: " + dateFormat.format(new Date(startDateMillis[0])));
        endDateButton.setText("End: " + dateFormat.format(new Date(endDateMillis[0])));

        startDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(startDateMillis[0]);
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, day, 0, 0, 0);
                        chosen.set(Calendar.MILLISECOND, 0);
                        startDateMillis[0] = chosen.getTimeInMillis();
                        startDateButton.setText("Start: " + dateFormat.format(chosen.getTime()));
                        if (endDateMillis[0] < startDateMillis[0]) {
                            endDateMillis[0] = startDateMillis[0];
                            endDateButton.setText("End: " + dateFormat.format(chosen.getTime()));
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.getDatePicker().setMinDate(todayMillis);
            picker.show();
        });

        endDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(endDateMillis[0]);
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, day, 0, 0, 0);
                        chosen.set(Calendar.MILLISECOND, 0);
                        endDateMillis[0] = chosen.getTimeInMillis();
                        endDateButton.setText("End: " + dateFormat.format(chosen.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.getDatePicker().setMinDate(startDateMillis[0]);
            picker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        createButton.setOnClickListener(v -> {
            String newTripName = nameInput.getText().toString().trim();

            if (newTripName.isEmpty()) {
                Toast.makeText(this, "Trip name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDateMillis[0] < todayMillis) {
                Toast.makeText(this, "Start date cannot be in the past for new trips", Toast.LENGTH_SHORT).show();
                return;
            }

            if (endDateMillis[0] < startDateMillis[0]) {
                Toast.makeText(this, "End date cannot be earlier than start date", Toast.LENGTH_SHORT).show();
                return;
            }

            Trip newTrip = new Trip(
                    newTripName,
                    System.currentTimeMillis(),
                    0L,
                    startDateMillis[0],
                    endDateMillis[0]
            );

            tripRepository.insertTrip(newTrip, tripId -> {
                Toast.makeText(this, "Trip Created!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                refreshTripList();
                refreshDashboard();
            });
        });

        dialog.show();
    }

    private void refreshTripList() {

        tripRepository.getAllTrips(trips -> {

            tripListContainer.removeAllViews();

            LayoutInflater inflater =
                    LayoutInflater.from(this);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            for (Trip trip : trips) {

                View tripCard = inflater.inflate(
                        R.layout.item_trip,
                        tripListContainer,
                        false
                );

                TextView tripNameText =
                        tripCard.findViewById(R.id.tripNameText);

                TextView tripDatesText =
                        tripCard.findViewById(R.id.tripDatesText);

                tripNameText.setText(trip.getName());
                if (tripDatesText != null) {
                    tripDatesText.setText("🗓 " + dateFormat.format(new Date(trip.getStartDate())) + " – " + dateFormat.format(new Date(trip.getEndDate())));
                }

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
    private void refreshDashboard() {

        tripRepository.getAllTrips(trips -> {

            Calendar todayCal = Calendar.getInstance();
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            long todayMillis = todayCal.getTimeInMillis();

            List<Trip> upcomingTrips = new java.util.ArrayList<>();
            List<Trip> completedTrips = new java.util.ArrayList<>();

            for (Trip trip : trips) {
                if (trip.getEndDate() >= todayMillis) {
                    upcomingTrips.add(trip);
                } else {
                    completedTrips.add(trip);
                }
            }

            // Sort upcoming trips by startDate ASC (soonest first)
            upcomingTrips.sort((a, b) -> Long.compare(a.getStartDate(), b.getStartDate()));

            // Sort completed trips by endDate DESC (most recently completed first)
            completedTrips.sort((a, b) -> Long.compare(b.getEndDate(), a.getEndDate()));

            // Populate Next Adventures
            LinearLayout upcomingContainer = findViewById(R.id.upcomingTripsContainer);
            View upcomingEmptyCard = findViewById(R.id.upcomingTripsEmptyCard);

            if (upcomingContainer != null) {
                upcomingContainer.removeAllViews();
            }

            if (upcomingTrips.isEmpty()) {
                if (upcomingEmptyCard != null) upcomingEmptyCard.setVisibility(View.VISIBLE);
            } else {
                if (upcomingEmptyCard != null) upcomingEmptyCard.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                for (Trip trip : upcomingTrips) {
                    View tripCard = inflater.inflate(R.layout.item_trip, upcomingContainer, false);
                    TextView tripNameText = tripCard.findViewById(R.id.tripNameText);
                    TextView tripDatesText = tripCard.findViewById(R.id.tripDatesText);

                    tripNameText.setText(trip.getName());
                    if (tripDatesText != null) {
                        tripDatesText.setText("🗓 " + dateFormat.format(new Date(trip.getStartDate())) + " – " + dateFormat.format(new Date(trip.getEndDate())));
                    }

                    tripCard.setOnClickListener(v -> {
                        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                        intent.putExtra("TRIP_ID", trip.getId());
                        intent.putExtra("TRIP_TITLE", trip.getName());
                        startActivity(intent);
                    });

                    upcomingContainer.addView(tripCard);
                }
            }

            // Populate Recently Completed Trips (max 5)
            dashboardRecentTripsContainer.removeAllViews();

            if (completedTrips.isEmpty()) {
                dashboardEmptyText.setVisibility(View.VISIBLE);
                dashboardEmptyText.setText("No completed trips yet. Past adventures will appear here once their dates pass.");
            } else {
                dashboardEmptyText.setVisibility(View.GONE);
                LayoutInflater inflater = LayoutInflater.from(this);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

                int count = Math.min(5, completedTrips.size());
                for (int i = 0; i < count; i++) {
                    Trip trip = completedTrips.get(i);
                    View tripCard = inflater.inflate(R.layout.item_trip, dashboardRecentTripsContainer, false);
                    TextView tripNameText = tripCard.findViewById(R.id.tripNameText);
                    TextView tripDatesText = tripCard.findViewById(R.id.tripDatesText);

                    tripNameText.setText(trip.getName());
                    if (tripDatesText != null) {
                        tripDatesText.setText("🗓 " + dateFormat.format(new Date(trip.getStartDate())) + " – " + dateFormat.format(new Date(trip.getEndDate())));
                    }

                    tripCard.setOnClickListener(v -> {
                        Intent intent = new Intent(
                                HomeActivity.this,
                                MainActivity.class
                        );

                        intent.putExtra("TRIP_ID", trip.getId());
                        intent.putExtra("TRIP_TITLE", trip.getName());

                        startActivity(intent);
                    });

                    dashboardRecentTripsContainer.addView(tripCard);
                }
            }
        });
    }

    private void performLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of SplitTrip?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log Out", (dialog, which) -> {
                    SharedPreferences preferences = getSharedPreferences("splittrip_session", MODE_PRIVATE);
                    preferences.edit().putBoolean("isLoggedIn", false).apply();

                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
