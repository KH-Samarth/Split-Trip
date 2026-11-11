package com.example.split_trip;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.split_trip.database.ExpenseRepository;
import com.example.split_trip.database.TripRepository;
import com.example.split_trip.models.Trip;
import com.example.split_trip.models.Expense;

import java.math.BigDecimal;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int tripId;
    private String tripTitle;

    private ExpenseRepository expenseRepository;
    private TripRepository tripRepository;

    private TextView totalSpentText;
    private TextView expenseLogText;

    private EditText payerInput;
    private EditText amountInput;
    private EditText descInput;
    private Button addExpenseButton;

    private long totalSpentPaise = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tripId = getIntent().getIntExtra("TRIP_ID", -1);
        tripTitle = getIntent().getStringExtra("TRIP_TITLE");

        if (tripId == -1 || tripTitle == null) {
            Toast.makeText(this,
                    "Unable to open trip",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        expenseRepository = new ExpenseRepository(this);
        tripRepository = new TripRepository(this);

        TextView tripTitleText = findViewById(R.id.tripTitleText);
        Button backButton = findViewById(R.id.backButton);

        totalSpentText = findViewById(R.id.totalSpentText);
        expenseLogText = findViewById(R.id.expenseLogText);

        payerInput = findViewById(R.id.payerInput);
        amountInput = findViewById(R.id.amountInput);
        descInput = findViewById(R.id.descInput);

        addExpenseButton = findViewById(R.id.addExpenseButton);
        Button deleteTripButton = findViewById(R.id.deleteTripButton);

        tripTitleText.setText(tripTitle);

        backButton.setOnClickListener(v -> finish());

        addExpenseButton.setOnClickListener(v -> addExpense());

        deleteTripButton.setOnClickListener(v -> confirmDeleteTrip());

        refreshExpenses();
    }

    private void addExpense() {

        String payer = payerInput.getText().toString().trim();
        String amountStr = amountInput.getText().toString().trim();
        String desc = descInput.getText().toString().trim();

        if (payer.isEmpty() || amountStr.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        long amountPaise;

        try {
            BigDecimal amount = new BigDecimal(amountStr);

            amountPaise = amount
                    .movePointRight(2)
                    .longValueExact();

            if (amountPaise <= 0) {
                Toast.makeText(this,
                        "Enter an amount greater than zero",
                        Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (NumberFormatException | ArithmeticException e) {
            Toast.makeText(this,
                    "Enter a valid amount (maximum 2 decimal places)",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Expense expense = new Expense(
                tripId,
                payer,
                amountPaise,
                desc,
                System.currentTimeMillis()
        );

        addExpenseButton.setEnabled(false);

        expenseRepository.insertExpense(expense, expenseId -> {

            addExpenseButton.setEnabled(true);

            payerInput.setText("");
            amountInput.setText("");
            descInput.setText("");

            refreshExpenses();

            Toast.makeText(this,
                    "Expense added!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshExpenses() {

        expenseRepository.getExpensesForTrip(tripId, expenses -> {

            expenseLogText.setText("");

            if (expenses.isEmpty()) {
                expenseLogText.setText("No expenses recorded yet.");
                return;
            }

            StringBuilder log = new StringBuilder();

            for (Expense expense : expenses) {

                String amount = BigDecimal.valueOf(
                        expense.getAmountPaise(), 2
                ).toPlainString();

                log.append(expense.getPayer())
                        .append(" paid ₹")
                        .append(amount)
                        .append(" for ")
                        .append(expense.getDescription())
                        .append("\n\n");
            }

            expenseLogText.setText(log.toString());
        });

        expenseRepository.getTotalExpensesForTrip(tripId, total -> {

            totalSpentPaise = total;

            String formattedTotal = BigDecimal.valueOf(
                    totalSpentPaise, 2
            ).toPlainString();

            totalSpentText.setText(
                    "Total Trip Cost: ₹" + formattedTotal
            );
        });
    }

    private void confirmDeleteTrip() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Trip?")
                .setMessage("Are you sure you want to delete \""
                        + tripTitle
                        + "\"? All its expenses will also be deleted.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    Trip trip = new Trip(
                            tripTitle,
                            System.currentTimeMillis(),
                            0L
                    );

                    trip.setId(tripId);

                    tripRepository.deleteTrip(trip, () -> {

                        Toast.makeText(this,
                                "Trip deleted",
                                Toast.LENGTH_SHORT).show();

                        finish();
                    });
                })
                .show();
    }
}
