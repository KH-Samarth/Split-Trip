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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.example.split_trip.database.TripMemberRepository;
import com.example.split_trip.models.TripMember;

import java.math.BigDecimal;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int tripId;
    private String tripTitle;

    private ExpenseRepository expenseRepository;
    private TripRepository tripRepository;
    private TripMemberRepository tripMemberRepository;
    private LinearLayout memberListContainer;
    private TextView memberEmptyText;
    private Button addMemberButton;

    private TextView totalSpentText;
    private LinearLayout expenseListContainer;
    private TextView expenseEmptyText;
    private LinearLayout itinerarySection;
    private LinearLayout expensesSection;

    private com.google.android.material.button.MaterialButton itineraryTabButton;
    private com.google.android.material.button.MaterialButton expensesTabButton;

    private EditText payerInput;
    private EditText amountInput;
    private EditText descInput;
    private Button addExpenseButton;

    private long totalSpentPaise = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

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
        tripMemberRepository = new TripMemberRepository(this);

        memberListContainer = findViewById(R.id.memberListContainer);
        memberEmptyText = findViewById(R.id.memberEmptyText);
        addMemberButton = findViewById(R.id.addMemberButton);

        addMemberButton.setOnClickListener(v -> showAddMemberDialog());

        refreshMembers();

        TextView tripTitleText = findViewById(R.id.tripTitleText);
        Button backButton = findViewById(R.id.backButton);

        totalSpentText = findViewById(R.id.totalSpentText);
        expenseListContainer = findViewById(R.id.expenseListContainer);
        expenseEmptyText = findViewById(R.id.expenseEmptyText);

        itinerarySection = findViewById(R.id.itinerarySection);
        expensesSection = findViewById(R.id.expensesSection);

        itineraryTabButton = findViewById(R.id.itineraryTabButton);
        expensesTabButton = findViewById(R.id.expensesTabButton);

        itineraryTabButton.setOnClickListener(v -> showTripSection(true));
        expensesTabButton.setOnClickListener(v -> showTripSection(false));

// Open the trip on the Itinerary section by default.
        showTripSection(true);

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

                    TextView dateText =
                            expenseCard.findViewById(R.id.expenseDateText);

                    TextView amountText =
                            expenseCard.findViewById(R.id.expenseAmountText);

                    descriptionText.setText(expense.getDescription());
                    payerText.setText("Paid by " + expense.getPayer());
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "dd MMM yyyy · hh:mm a",
                            Locale.getDefault()
                    );

                    dateText.setText(dateFormat.format(
                            new Date(expense.getCreatedAt())
                    ));

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

    private void showAddMemberDialog() {
        EditText nameInput = new EditText(this);
        nameInput.setHint("Member name");
        nameInput.setSingleLine(true);

        EditText emailInput = new EditText(this);
        emailInput.setHint("Email (optional)");
        emailInput.setInputType(
                android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        );
        emailInput.setSingleLine(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);
        layout.addView(nameInput);
        layout.addView(emailInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Trip Member")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this,
                                "Please enter a name",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    TripMember member = new TripMember(
                            tripId, name, email, false
                    );

                    tripMemberRepository.insertMember(member, () -> {
                        refreshMembers();
                        Toast.makeText(this,
                                "Member added",
                                Toast.LENGTH_SHORT).show();
                    });
                })
                .show();
    }

    private void refreshMembers() {
        tripMemberRepository.getMembersForTrip(tripId, members -> {
            memberListContainer.removeAllViews();

            if (members.isEmpty()) {
                memberEmptyText.setVisibility(View.VISIBLE);
                memberListContainer.addView(memberEmptyText);
                return;
            }

            memberEmptyText.setVisibility(View.GONE);

            LayoutInflater inflater = LayoutInflater.from(this);

            for (TripMember member : members) {
                View memberRow = inflater.inflate(
                        R.layout.item_member,
                        memberListContainer,
                        false
                );

                TextView initialText =
                        memberRow.findViewById(R.id.memberInitialText);

                TextView nameText =
                        memberRow.findViewById(R.id.memberNameText);

                TextView roleText =
                        memberRow.findViewById(R.id.memberRoleText);

                Button removeButton =
                        memberRow.findViewById(R.id.removeMemberButton);

                nameText.setText(member.getName());

                String initial = member.getName().substring(0, 1).toUpperCase();
                initialText.setText(initial);

                roleText.setText(
                        member.isCreator() ? "Trip Creator" : "Trip Member"
                );

                removeButton.setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Remove Member")
                                .setMessage("Remove " + member.getName() + " from this trip?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Remove", (dialog, which) ->
                                        tripMemberRepository.deleteMember(member, this::refreshMembers)
                                )
                                .show()
                );

                memberListContainer.addView(memberRow);
            }
        });
    }
    private void showTripSection(boolean showItinerary) {

        itinerarySection.setVisibility(
                showItinerary ? View.VISIBLE : View.GONE
        );

        expensesSection.setVisibility(
                showItinerary ? View.GONE : View.VISIBLE
        );

        int selectedColor = android.graphics.Color.parseColor("#7965C8");
        int unselectedColor = android.graphics.Color.parseColor("#EEE9FF");

        int selectedTextColor = android.graphics.Color.WHITE;
        int unselectedTextColor = android.graphics.Color.parseColor("#7965C8");

        itineraryTabButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        showItinerary ? selectedColor : unselectedColor
                )
        );

        expensesTabButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        showItinerary ? unselectedColor : selectedColor
                )
        );

        itineraryTabButton.setTextColor(
                showItinerary ? selectedTextColor : unselectedTextColor
        );

        expensesTabButton.setTextColor(
                showItinerary ? unselectedTextColor : selectedTextColor
        );
    }

}
