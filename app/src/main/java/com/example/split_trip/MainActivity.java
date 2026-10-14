package com.example.split_trip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private double totalSpent = 0.0;
    private StringBuilder expenseLog = new StringBuilder();
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tripTitleText = findViewById(R.id.tripTitleText);
        Button backButton = findViewById(R.id.backButton);
        TextView totalSpentText = findViewById(R.id.totalSpentText);
        TextView expenseLogText = findViewById(R.id.expenseLogText);

        String tripTitle = getIntent().getStringExtra("TRIP_TITLE");
        if (tripTitle != null) {
            tripTitleText.setText(tripTitle);
        }

        // --- 1. LOAD SAVED DATA ---
        // Open the phone's local storage
        prefs = getSharedPreferences("SplitTripData", MODE_PRIVATE);

        // Load the math and log using the trip's name as the key
        totalSpent = prefs.getFloat(tripTitle + "_total", 0.0f);
        String savedLog = prefs.getString(tripTitle + "_log", "");
        expenseLog = new StringBuilder(savedLog);

        // Display the loaded data
        totalSpentText.setText("Total Trip Cost: ₹" + totalSpent);
        expenseLogText.setText(expenseLog.toString());

        backButton.setOnClickListener(v -> finish());

        // --- 2. LOG & SAVE NEW EXPENSES ---
        EditText payerInput = findViewById(R.id.payerInput);
        EditText amountInput = findViewById(R.id.amountInput);
        EditText descInput = findViewById(R.id.descInput);
        Button addExpenseButton = findViewById(R.id.addExpenseButton);

        addExpenseButton.setOnClickListener(v -> {
            String payer = payerInput.getText().toString();
            String amountStr = amountInput.getText().toString();
            String desc = descInput.getText().toString();

            if (payer.isEmpty() || amountStr.isEmpty() || desc.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double amount = Double.parseDouble(amountStr);
            totalSpent += amount;

            totalSpentText.setText("Total Trip Cost: ₹" + totalSpent);
            expenseLog.insert(0, payer + " paid ₹" + amount + " for " + desc + "\n\n");
            expenseLogText.setText(expenseLog.toString());

            // Save instantly to phone storage
            SharedPreferences.Editor editor = prefs.edit();
            editor.putFloat(tripTitle + "_total", (float) totalSpent);
            editor.putString(tripTitle + "_log", expenseLog.toString());
            editor.apply(); // Applies the save in the background

            payerInput.setText("");
            amountInput.setText("");
            descInput.setText("");
        });
        Button deleteTripButton = findViewById(R.id.deleteTripButton);
        // --- 3. BULLETPROOF DELETE TRIP LOGIC ---
        deleteTripButton.setOnClickListener(v -> {
            // 1. Delete this trip's expenses
            prefs.edit().remove(tripTitle + "_total").remove(tripTitle + "_log").apply();

            // 2. Get the master list
            String savedTrips = prefs.getString("TRIPS_LIST", "");
            String[] tripsArray = savedTrips.split(",");

            // 3. Rebuild the list, completely ignoring the one we want to delete
            StringBuilder updatedList = new StringBuilder();
            for (String trip : tripsArray) {
                // Trim removes hidden spaces. If it doesn't match the title, keep it.
                if (!trip.trim().isEmpty() && !trip.trim().equals(tripTitle.trim())) {
                    if (updatedList.length() > 0) {
                        updatedList.append(",");
                    }
                    updatedList.append(trip.trim());
                }
            }

            // 4. Save the newly rebuilt list back to storage
            prefs.edit().putString("TRIPS_LIST", updatedList.toString()).apply();

            Toast.makeText(MainActivity.this, "Trip Deleted!", Toast.LENGTH_SHORT).show();
            finish(); // Close screen
        });
    }
}