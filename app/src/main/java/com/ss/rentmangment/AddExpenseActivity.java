package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddExpenseActivity extends AppCompatActivity {

    private TextInputEditText etDescription, etAmount, etDate;
    private AutoCompleteTextView actvCategory;
    private Spinner spinnerRoom;
    private Button btnSave;

    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        setSupportActionBar(findViewById(R.id.toolbar_add_expense));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        etDescription = findViewById(R.id.etExpenseDescription);
        etAmount = findViewById(R.id.etExpenseAmount);
        actvCategory = findViewById(R.id.actvExpenseCategory);
        spinnerRoom = findViewById(R.id.spinnerRoomForExpense);
        etDate = findViewById(R.id.etExpenseDate);
        btnSave = findViewById(R.id.btnSaveExpense);

        String adminId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("mobile", "");
        rootRef = FirebaseDatabase.getInstance().getReference("users").child(adminId);

        setupCategoryDropdown();
        setupRoomSpinner(); // New method to populate rooms
        setupDatePicker();

        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Maintenance & Repairs", "Utilities", "Taxes", "Management Fees", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        actvCategory.setAdapter(adapter);
    }

    private void setupRoomSpinner() {
        rootRef.child("rooms").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> roomNames = new ArrayList<>();
                roomNames.add("None (General Expense)"); // Default option
                for(DataSnapshot roomSnapshot : snapshot.getChildren()){
                    RoomModel room = roomSnapshot.getValue(RoomModel.class);
                    if(room != null && room.getName() != null){
                        roomNames.add(room.getName());
                    }
                }
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(AddExpenseActivity.this, android.R.layout.simple_spinner_item, roomNames);
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerRoom.setAdapter(roomAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddExpenseActivity.this, "Could not load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.set(year, month, dayOfMonth);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDate.setText(sdf.format(selectedDate.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String roomName = spinnerRoom.getSelectedItem().toString();

        if (TextUtils.isEmpty(description) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(category) || TextUtils.isEmpty(date)) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String expenseId = rootRef.child("expenses").push().getKey();

        // If "None" is selected, save as an empty string for clarity in the database
        if (roomName.contains("None")) {
            roomName = "";
        }

        Expense expense = new Expense(expenseId, description, amount, category, date, roomName);

        if (expenseId != null) {
            rootRef.child("expenses").child(expenseId).setValue(expense).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
