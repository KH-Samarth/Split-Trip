package com.example.split_trip;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

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
    private LinearLayout expenseListContainer;
    private TextView expenseEmptyText;

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
        expenseListContainer = findViewById(R.id.expenseListContainer);
        expenseEmptyText = findViewById(R.id.expenseEmptyText);

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

            expenseListContainer.removeAllViews();

            if (expenses.isEmpty()) {
                expenseEmptyText.setVisibility(View.VISIBLE);
            } else {
                expenseEmptyText.setVisibility(View.GONE);

                LayoutInflater inflater = LayoutInflater.from(this);

                for (Expense expense : expenses) {

                    View expenseCard = inflater.inflate(
                            R.layout.item_expense,
                            expenseListContainer,
                            false
                    );

                    TextView descriptionText =
                            expenseCard.findViewById(R.id.expenseDescriptionText);

                    TextView payerText =
                            expenseCard.findViewById(R.id.expensePayerText);

                    TextView amountText =
                            expenseCard.findViewById(R.id.expenseAmountText);

                    descriptionText.setText(expense.getDescription());
                    payerText.setText("Paid by " + expense.getPayer());

                    String amount = BigDecimal.valueOf(
                            expense.getAmountPaise(), 2
                    ).toPlainString();

                    amountText.setText("₹" + amount);

                    Button editButton = expenseCard.findViewById(R.id.editExpenseButton);
                    Button deleteButton = expenseCard.findViewById(R.id.deleteExpenseButton);

                    editButton.setOnClickListener(v -> showEditExpenseDialog(expense));

                    deleteButton.setOnClickListener(v -> confirmDeleteExpense(expense));

                    expenseListContainer.addView(expenseCard);
                }
            }
        });

        expenseRepository.getTotalExpensesForTrip(tripId, total -> {

            totalSpentPaise = total;

            String formattedTotal = BigDecimal.valueOf(
                    totalSpentPaise, 2
            ).toPlainString();

            totalSpentText.setText("Total Trip Cost: ₹" + formattedTotal);
        });
    }

    private void showEditExpenseDialog(Expense expense) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);

        EditText payerField = new EditText(this);
        payerField.setHint("Paid by");
        payerField.setText(expense.getPayer());

        EditText amountField = new EditText(this);
        amountField.setHint("Amount");
        amountField.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        amountField.setText(
                BigDecimal.valueOf(expense.getAmountPaise(), 2).toPlainString()
        );

        EditText descriptionField = new EditText(this);
        descriptionField.setHint("Description");
        descriptionField.setText(expense.getDescription());

        layout.addView(payerField);
        layout.addView(amountField);
        layout.addView(descriptionField);

        new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {

                    String payer = payerField.getText().toString().trim();
                    String amountString = amountField.getText().toString().trim();
                    String description = descriptionField.getText().toString().trim();

                    if (payer.isEmpty() || amountString.isEmpty()
                            || description.isEmpty()) {
                        Toast.makeText(this,
                                "Please fill all fields",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long amountPaise = new BigDecimal(amountString)
                                .movePointRight(2)
                                .longValueExact();

                        if (amountPaise <= 0) {
                            Toast.makeText(this,
                                    "Amount must be greater than zero",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        expense.setPayer(payer);
                        expense.setAmountPaise(amountPaise);
                        expense.setDescription(description);

                        expenseRepository.updateExpense(expense, () -> {
                            refreshExpenses();
                            Toast.makeText(this,
                                    "Expense updated",
                                    Toast.LENGTH_SHORT).show();
                        });

                    } catch (NumberFormatException | ArithmeticException e) {
                        Toast.makeText(this,
                                "Enter a valid amount (maximum 2 decimal places)",
                                Toast.LENGTH_SHORT).show();
                    }

                })
                .show();
    }

    private void confirmDeleteExpense(Expense expense) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Expense?")
                .setMessage("Are you sure you want to delete \""
                        + expense.getDescription() + "\"?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {

                    expenseRepository.deleteExpense(expense, () -> {
                        refreshExpenses();
                        Toast.makeText(this,
                                "Expense deleted",
                                Toast.LENGTH_SHORT).show();
                    });

                })
                .show();
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
