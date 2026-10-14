package com.example.split_trip;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private LinearLayout tripListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tripListContainer = findViewById(R.id.tripListContainer);
        Button newTripButton = findViewById(R.id.newTripButton);
        prefs = getSharedPreferences("SplitTripData", MODE_PRIVATE);

        // Click triggers the popup dialog
        newTripButton.setOnClickListener(v -> showCreateTripDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTripList();
    }

    // --- POPUP WINDOW LOGIC ---
    private void showCreateTripDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New Trip");

        final EditText input = new EditText(this);
        input.setHint("e.g., 🗼 Tokyo 2027");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String newTripName = input.getText().toString().trim();
            if (!newTripName.isEmpty()) {
                String currentTrips = prefs.getString("TRIPS_LIST", "");

                if (currentTrips.isEmpty()) {
                    currentTrips = newTripName;
                } else {
                    currentTrips = currentTrips + "," + newTripName;
                }

                prefs.edit().putString("TRIPS_LIST", currentTrips).apply();

                refreshTripList();
                Toast.makeText(HomeActivity.this, "Trip Created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Trip name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- DRAW BUTTONS LOGIC ---
    private void refreshTripList() {
        tripListContainer.removeAllViews(); // Clear old buttons

        String savedTrips = prefs.getString("TRIPS_LIST", "");
        if (savedTrips.isEmpty()) return;

        ArrayList<String> myTripsList = new ArrayList<>(Arrays.asList(savedTrips.split(",")));

        for (String tripName : myTripsList) {
            Button dynamicButton = new Button(this);
            dynamicButton.setText(tripName);
            dynamicButton.setAllCaps(false);
            dynamicButton.setTextSize(18);
            dynamicButton.setPadding(20, 40, 20, 40);

            // Add a little space beneath each trip button
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 15);
            dynamicButton.setLayoutParams(params);

            dynamicButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("TRIP_TITLE", tripName);
                startActivity(intent);
            });

            tripListContainer.addView(dynamicButton);
        }
    }
}