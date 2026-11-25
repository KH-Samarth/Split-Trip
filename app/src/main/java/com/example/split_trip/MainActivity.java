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
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.split_trip.database.ExpenseRepository;
import com.example.split_trip.database.TripRepository;
import com.example.split_trip.models.Trip;
import com.example.split_trip.models.Expense;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import com.example.split_trip.models.VisitedPlace;
import com.example.split_trip.models.PlannedPlace;

import com.example.split_trip.database.TripMemberRepository;
import com.example.split_trip.models.TripMember;
import com.example.split_trip.database.VisitedPlaceRepository;
import com.example.split_trip.database.PlannedPlaceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private int tripId;
    private String tripTitle;
    private Trip currentTrip;

    private ExpenseRepository expenseRepository;
    private TripRepository tripRepository;
    private TripMemberRepository tripMemberRepository;
    private VisitedPlaceRepository visitedPlaceRepository;
    private PlannedPlaceRepository plannedPlaceRepository;
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
        visitedPlaceRepository = new VisitedPlaceRepository(this);
        plannedPlaceRepository = new PlannedPlaceRepository(this);

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

        Button addPlannedPlaceButton = findViewById(R.id.addPlannedPlaceButton);
        if (addPlannedPlaceButton != null) {
            addPlannedPlaceButton.setOnClickListener(v -> showAddPlannedPlaceDialog(null));
        }

        Button addActivityButton = findViewById(R.id.addActivityButton);
        addActivityButton.setOnClickListener(v -> showAddPlaceDialog(null));

        itineraryTabButton = findViewById(R.id.itineraryTabButton);
        expensesTabButton = findViewById(R.id.expensesTabButton);

        itineraryTabButton.setOnClickListener(v -> showTripSection(true));
        expensesTabButton.setOnClickListener(v -> showTripSection(false));

// Open the trip on the Itinerary section by default.
        showTripSection(true);
        refreshPlannedPlaces();
        refreshVisitedPlaces();

        payerInput = findViewById(R.id.payerInput);
        amountInput = findViewById(R.id.amountInput);
        descInput = findViewById(R.id.descInput);

        addExpenseButton = findViewById(R.id.addExpenseButton);
        Button deleteTripButton = findViewById(R.id.deleteTripButton);

        Button editTripButton = findViewById(R.id.editTripButton);
        if (editTripButton != null) {
            editTripButton.setOnClickListener(v -> showEditTripDialog());
        }

        tripTitleText.setText(tripTitle);

        tripRepository.getTripById(tripId, trip -> {
            currentTrip = trip;
            updateTripHeader();
        });

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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                String payer = payerField.getText().toString().trim();
                String amountString = amountField.getText().toString().trim();
                String description = descriptionField.getText().toString().trim();

                if (payer.isEmpty() || amountString.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    long amountPaise = new BigDecimal(amountString)
                            .movePointRight(2)
                            .longValueExact();

                    if (amountPaise <= 0) {
                        Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    expense.setPayer(payer);
                    expense.setAmountPaise(amountPaise);
                    expense.setDescription(description);

                    expenseRepository.updateExpense(expense, () -> {
                        refreshExpenses();
                        Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
                    });

                    dialog.dismiss();

                } catch (NumberFormatException | ArithmeticException e) {
                    Toast.makeText(this, "Enter a valid amount (maximum 2 decimal places)", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
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

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Trip Member")
                .setView(layout)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = nameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                    return;
                }

                TripMember member = new TripMember(
                        tripId, name, email, false
                );

                tripMemberRepository.insertMember(member, () -> {
                    refreshMembers();
                    Toast.makeText(this, "Member added", Toast.LENGTH_SHORT).show();
                });

                dialog.dismiss();
            });
        });

        dialog.show();
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

    private void showAddPlaceDialog(VisitedPlace existingPlace) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_place, null);

        TextView titleText = dialogView.findViewById(R.id.dialogTitleText);
        TextView subtitleText = dialogView.findViewById(R.id.dialogSubtitleText);
        EditText nameInput = dialogView.findViewById(R.id.dialogNameInput);
        EditText locationInput = dialogView.findViewById(R.id.dialogLocationInput);
        Button dateButton = dialogView.findViewById(R.id.dialogDateButton);
        EditText noteInput = dialogView.findViewById(R.id.dialogNoteInput);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        titleText.setText(existingPlace == null ? "Add a visited place" : "Edit visited place");
        subtitleText.setText("Keep a record of the places you experienced.");

        final long[] selectedDate = {System.currentTimeMillis()};
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        dateButton.setText("🗓 " + dateFormat.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate[0]);

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, day, 0, 0, 0);
                        chosen.set(Calendar.MILLISECOND, 0);
                        selectedDate[0] = chosen.getTimeInMillis();
                        dateButton.setText("🗓 " + dateFormat.format(chosen.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        if (existingPlace != null) {
            nameInput.setText(existingPlace.getName());
            locationInput.setText(existingPlace.getLocation());
            noteInput.setText(existingPlace.getNote());
            selectedDate[0] = existingPlace.getVisitDate();
            dateButton.setText("🗓 " + dateFormat.format(new Date(selectedDate[0])));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String note = noteInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar chosenCal = Calendar.getInstance();
            chosenCal.setTimeInMillis(selectedDate[0]);
            chosenCal.set(Calendar.HOUR_OF_DAY, 0);
            chosenCal.set(Calendar.MINUTE, 0);
            chosenCal.set(Calendar.SECOND, 0);
            chosenCal.set(Calendar.MILLISECOND, 0);
            long chosenMillis = chosenCal.getTimeInMillis();

            Runnable saveAction = () -> {
                if (existingPlace == null) {
                    VisitedPlace place = new VisitedPlace(
                            tripId, name, location, selectedDate[0], note
                    );
                    visitedPlaceRepository.insertPlace(place, placeId -> refreshVisitedPlaces());
                } else {
                    existingPlace.setName(name);
                    existingPlace.setLocation(location);
                    existingPlace.setVisitDate(selectedDate[0]);
                    existingPlace.setNote(note);
                    visitedPlaceRepository.updatePlace(existingPlace, this::refreshVisitedPlaces);
                }
                dialog.dismiss();
            };

            if (currentTrip != null && (chosenMillis < currentTrip.getStartDate() || chosenMillis > currentTrip.getEndDate())) {
                SimpleDateFormat dateFormatPrompt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String tripDatesStr = dateFormatPrompt.format(new Date(currentTrip.getStartDate())) + " – " + dateFormatPrompt.format(new Date(currentTrip.getEndDate()));
                String chosenDateStr = dateFormatPrompt.format(new Date(chosenMillis));

                new AlertDialog.Builder(this)
                        .setTitle("Date outside trip duration")
                        .setMessage("The selected visit date (" + chosenDateStr + ") falls outside your trip duration (" + tripDatesStr + ").\n\nWhat would you like to do?")
                        .setPositiveButton("Keep date anyway", (w, whichBtn) -> saveAction.run())
                        .setNeutralButton("Change trip dates", (w, whichBtn) -> {
                            dialog.dismiss();
                            showEditTripDialog(() -> showAddPlaceDialog(existingPlace));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                saveAction.run();
            }
        });

        dialog.show();
    }
    private void refreshVisitedPlaces() {

        LinearLayout placeListContainer =
                findViewById(R.id.itineraryListContainer);

        View emptyCard = findViewById(R.id.itineraryEmptyCard);

        visitedPlaceRepository.getPlacesByTripId(tripId, places -> {

            placeListContainer.removeAllViews();

            if (places.isEmpty()) {
                emptyCard.setVisibility(View.VISIBLE);
                return;
            }

            emptyCard.setVisibility(View.GONE);

            LayoutInflater inflater = LayoutInflater.from(this);

            for (VisitedPlace place : places) {

                View placeCard = inflater.inflate(
                        R.layout.item_visited_place,
                        placeListContainer,
                        false
                );

                TextView nameText =
                        placeCard.findViewById(R.id.placeNameText);

                TextView locationText =
                        placeCard.findViewById(R.id.placeLocationText);

                TextView dateText =
                        placeCard.findViewById(R.id.placeDateText);

                TextView noteText =
                        placeCard.findViewById(R.id.placeNoteText);

                Button editButton =
                        placeCard.findViewById(R.id.editPlaceButton);

                Button deleteButton =
                        placeCard.findViewById(R.id.deletePlaceButton);

                nameText.setText(place.getName());

                if (place.getLocation() == null ||
                        place.getLocation().trim().isEmpty()) {
                    locationText.setVisibility(View.GONE);
                } else {
                    locationText.setVisibility(View.VISIBLE);
                    locationText.setText("📍 " + place.getLocation());
                }

                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                dateText.setText(
                        "🗓 Visited on " +
                                dateFormat.format(new Date(place.getVisitDate()))
                );

                if (place.getNote() == null ||
                        place.getNote().trim().isEmpty()) {
                    noteText.setVisibility(View.GONE);
                } else {
                    noteText.setVisibility(View.VISIBLE);
                    noteText.setText(place.getNote());
                }

                editButton.setOnClickListener(v ->
                        showAddPlaceDialog(place)
                );

                deleteButton.setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Delete visited place?")
                                .setMessage("Remove " + place.getName() + " from your memories?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (dialog, which) ->
                                        visitedPlaceRepository.deletePlace(
                                                place,
                                                this::refreshVisitedPlaces
                                        )
                                )
                                .show()
                );

                placeListContainer.addView(placeCard);
            }
        });
    }

    private void showAddPlannedPlaceDialog(PlannedPlace existingPlace) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_place, null);

        TextView titleText = dialogView.findViewById(R.id.dialogTitleText);
        TextView subtitleText = dialogView.findViewById(R.id.dialogSubtitleText);
        EditText nameInput = dialogView.findViewById(R.id.dialogNameInput);
        EditText locationInput = dialogView.findViewById(R.id.dialogLocationInput);
        Button dateButton = dialogView.findViewById(R.id.dialogDateButton);
        EditText noteInput = dialogView.findViewById(R.id.dialogNoteInput);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button saveButton = dialogView.findViewById(R.id.dialogSaveButton);

        titleText.setText(existingPlace == null ? "Add a place to visit" : "Edit place to visit");
        subtitleText.setText("Plan your upcoming stops and itinerary.");

        Calendar todayCal = Calendar.getInstance();
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        long todayMillis = todayCal.getTimeInMillis();

        final long[] selectedDate = {todayMillis};
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        dateButton.setText("🗓 " + dateFormat.format(new Date(selectedDate[0])));

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate[0]);

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        Calendar chosen = Calendar.getInstance();
                        chosen.set(year, month, day, 0, 0, 0);
                        chosen.set(Calendar.MILLISECOND, 0);
                        selectedDate[0] = chosen.getTimeInMillis();
                        dateButton.setText("🗓 " + dateFormat.format(chosen.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.getDatePicker().setMinDate(todayMillis);
            picker.show();
        });

        if (existingPlace != null) {
            nameInput.setText(existingPlace.getName());
            locationInput.setText(existingPlace.getLocation());
            noteInput.setText(existingPlace.getNote());
            selectedDate[0] = existingPlace.getPlannedDate();
            dateButton.setText("🗓 " + dateFormat.format(new Date(selectedDate[0])));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            String note = noteInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar chosenCal = Calendar.getInstance();
            chosenCal.setTimeInMillis(selectedDate[0]);
            chosenCal.set(Calendar.HOUR_OF_DAY, 0);
            chosenCal.set(Calendar.MINUTE, 0);
            chosenCal.set(Calendar.SECOND, 0);
            chosenCal.set(Calendar.MILLISECOND, 0);
            long chosenMillis = chosenCal.getTimeInMillis();

            if (chosenMillis < todayMillis) {
                Toast.makeText(this, "Planned visit date cannot be in the past", Toast.LENGTH_SHORT).show();
                return;
            }

            Runnable saveAction = () -> {
                if (existingPlace == null) {
                    PlannedPlace place = new PlannedPlace(
                            tripId, name, location, selectedDate[0], note
                    );

                    plannedPlaceRepository.insertPlace(place, placeId -> refreshPlannedPlaces());

                } else {
                    existingPlace.setName(name);
                    existingPlace.setLocation(location);
                    existingPlace.setPlannedDate(selectedDate[0]);
                    existingPlace.setNote(note);

                    plannedPlaceRepository.updatePlace(existingPlace, this::refreshPlannedPlaces);
                }

                dialog.dismiss();
            };

            if (currentTrip != null && (chosenMillis < currentTrip.getStartDate() || chosenMillis > currentTrip.getEndDate())) {
                SimpleDateFormat dateFormatPrompt = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                String tripDatesStr = dateFormatPrompt.format(new Date(currentTrip.getStartDate())) + " – " + dateFormatPrompt.format(new Date(currentTrip.getEndDate()));
                String chosenDateStr = dateFormatPrompt.format(new Date(chosenMillis));

                new AlertDialog.Builder(this)
                        .setTitle("Date outside trip duration")
                        .setMessage("The selected date (" + chosenDateStr + ") falls outside your trip duration (" + tripDatesStr + ").\n\nWhat would you like to do?")
                        .setPositiveButton("Keep date anyway", (w, whichBtn) -> saveAction.run())
                        .setNeutralButton("Change trip dates", (w, whichBtn) -> {
                            dialog.dismiss();
                            showEditTripDialog(() -> showAddPlannedPlaceDialog(existingPlace));
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                saveAction.run();
            }
        });

        dialog.show();
    }

    private void updateTripHeader() {
        TextView tripTitleText = findViewById(R.id.tripTitleText);
        TextView tripDatesHeaderText = findViewById(R.id.tripDatesHeaderText);

        if (currentTrip != null) {
            this.tripTitle = currentTrip.getName();
            if (tripTitleText != null) {
                tripTitleText.setText(currentTrip.getName());
            }

            if (tripDatesHeaderText != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                tripDatesHeaderText.setText("🗓 " + dateFormat.format(new Date(currentTrip.getStartDate())) + " – " + dateFormat.format(new Date(currentTrip.getEndDate())));
            }
        }
    }

    private void showEditTripDialog() {
        showEditTripDialog(null);
    }

    private void showEditTripDialog(Runnable onUpdated) {
        if (currentTrip == null) return;

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_create_trip, null);

        TextView titleText = dialogView.findViewById(R.id.dialogTitleText);
        TextView subtitleText = dialogView.findViewById(R.id.dialogSubtitleText);
        EditText nameInput = dialogView.findViewById(R.id.dialogTripNameInput);
        Button startDateButton = dialogView.findViewById(R.id.dialogStartDateButton);
        Button endDateButton = dialogView.findViewById(R.id.dialogEndDateButton);
        Button cancelButton = dialogView.findViewById(R.id.dialogCancelButton);
        Button saveButton = dialogView.findViewById(R.id.dialogCreateButton);

        if (titleText != null) titleText.setText("Edit Trip Dates");
        if (subtitleText != null) subtitleText.setText("Update your journey name and travel dates.");
        if (saveButton != null) saveButton.setText("Save");

        nameInput.setText(currentTrip.getName());

        final long[] startDateMillis = {currentTrip.getStartDate()};
        final long[] endDateMillis = {currentTrip.getEndDate()};

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

        saveButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Trip name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (endDateMillis[0] < startDateMillis[0]) {
                Toast.makeText(this, "End date cannot be before start date", Toast.LENGTH_SHORT).show();
                return;
            }

            currentTrip.setName(name);
            currentTrip.setStartDate(startDateMillis[0]);
            currentTrip.setEndDate(endDateMillis[0]);

            tripRepository.updateTrip(currentTrip, () -> {
                Toast.makeText(this, "Trip dates updated!", Toast.LENGTH_SHORT).show();
                updateTripHeader();
                dialog.dismiss();
                if (onUpdated != null) {
                    onUpdated.run();
                }
            });
        });

        dialog.show();
    }

    private void refreshPlannedPlaces() {

        LinearLayout placeListContainer =
                findViewById(R.id.plannedPlaceListContainer);

        View emptyCard = findViewById(R.id.plannedPlaceEmptyCard);

        if (placeListContainer == null || emptyCard == null) return;

        plannedPlaceRepository.getPlannedPlacesByTripId(tripId, places -> {

            placeListContainer.removeAllViews();

            if (places.isEmpty()) {
                emptyCard.setVisibility(View.VISIBLE);
                return;
            }

            emptyCard.setVisibility(View.GONE);

            LayoutInflater inflater = LayoutInflater.from(this);

            for (PlannedPlace place : places) {

                View placeCard = inflater.inflate(
                        R.layout.item_planned_place,
                        placeListContainer,
                        false
                );

                TextView nameText =
                        placeCard.findViewById(R.id.plannedPlaceNameText);

                TextView locationText =
                        placeCard.findViewById(R.id.plannedPlaceLocationText);

                TextView dateText =
                        placeCard.findViewById(R.id.plannedPlaceDateText);

                TextView noteText =
                        placeCard.findViewById(R.id.plannedPlaceNoteText);

                Button editButton =
                        placeCard.findViewById(R.id.editPlannedPlaceButton);

                Button deleteButton =
                        placeCard.findViewById(R.id.deletePlannedPlaceButton);

                nameText.setText(place.getName());

                if (place.getLocation() == null ||
                        place.getLocation().trim().isEmpty()) {
                    locationText.setVisibility(View.GONE);
                } else {
                    locationText.setVisibility(View.VISIBLE);
                    locationText.setText("📍 " + place.getLocation());
                }

                SimpleDateFormat dateFormat =
                        new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());

                dateText.setText(
                        "🗓 Planned for " +
                                dateFormat.format(new Date(place.getPlannedDate()))
                );

                if (place.getNote() == null ||
                        place.getNote().trim().isEmpty()) {
                    noteText.setVisibility(View.GONE);
                } else {
                    noteText.setVisibility(View.VISIBLE);
                    noteText.setText(place.getNote());
                }

                editButton.setOnClickListener(v ->
                        showAddPlannedPlaceDialog(place)
                );

                deleteButton.setOnClickListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Delete place to visit?")
                                .setMessage("Remove " + place.getName() + " from your planned destinations?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (dialog, which) ->
                                        plannedPlaceRepository.deletePlace(
                                                place,
                                                this::refreshPlannedPlaces
                                        )
                                )
                                .show()
                );

                placeListContainer.addView(placeCard);
            }
        });
    }
}
