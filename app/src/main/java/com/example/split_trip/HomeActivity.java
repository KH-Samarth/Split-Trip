package com.example.split_trip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        EditText newTripNameInput = findViewById(R.id.newTripNameInput);
        Button inlineCreateButton = findViewById(R.id.inlineCreateButton);

        prefs = getSharedPreferences("SplitTripData", MODE_PRIVATE);

        // --- INLINE CREATE TRIP LOGIC ---
        inlineCreateButton.setOnClickListener(v -> {
            String newTripName = newTripNameInput.getText().toString().trim();

            if (!newTripName.isEmpty()) {
                String currentTrips = prefs.getString("TRIPS_LIST", "✈️ Goa Getaway 2026,🏔️ Manali Trek");

                if (currentTrips.isEmpty()) {
                    currentTrips = newTripName;
                } else {
                    currentTrips = currentTrips + "," + newTripName;
                }

                // Save to local storage
                prefs.edit().putString("TRIPS_LIST", currentTrips).apply();

                // Clear the text box and refresh the screen
                newTripNameInput.setText("");
                refreshTripList();
                Toast.makeText(HomeActivity.this, "Trip Created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Enter a trip name first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTripList();
    }

    // --- DRAW BUTTONS LOGIC ---
    private void refreshTripList() {
        tripListContainer.removeAllViews(); // Clear old buttons

        String savedTrips = prefs.getString("TRIPS_LIST", "✈️ Goa Getaway 2026,🏔️ Manali Trek");
        if (savedTrips.isEmpty()) return;

        ArrayList<String> myTripsList = new ArrayList<>(Arrays.asList(savedTrips.split(",")));

        for (String tripName : myTripsList) {
            Button dynamicButton = new Button(this);
            dynamicButton.setText(tripName);
            dynamicButton.setAllCaps(false);
            dynamicButton.setTextSize(18);
            dynamicButton.setPadding(20, 40, 20, 40);

            // Clicking opens the Dashboard where the Delete button lives
            dynamicButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("TRIP_TITLE", tripName);
                startActivity(intent);
            });

            tripListContainer.addView(dynamicButton);
        }
    }
}