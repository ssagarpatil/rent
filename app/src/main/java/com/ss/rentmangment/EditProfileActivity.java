package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Patterns;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etBusinessName, etAddress, etBirthDate;
    private ImageView ivSignaturePreview, ivBack;
    private MaterialButton btnSaveChanges, btnUpdateSignature;

    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private String userMobile;
    private String digitalSignatureBase64 = "";

    private static final int SIGNATURE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupToolbar();
        setupBackButton();

        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");

        // Get data from intent
        userMobile = getIntent().getStringExtra("userMobile");

        // Pre-populate fields with data passed from SettingsFragment
        populateFieldsFromIntent();

        setupDatePicker();
        setupClickListeners();

        // Also load current user data from Firebase as backup
        loadCurrentUserData();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etBusinessName = findViewById(R.id.etBusinessName);
        etAddress = findViewById(R.id.etAddress);
        etBirthDate = findViewById(R.id.etBirthDate);
        ivSignaturePreview = findViewById(R.id.ivSignaturePreview);
        ivBack = findViewById(R.id.ivBack);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnUpdateSignature = findViewById(R.id.btnUpdateSignature);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
    }

    private void setupBackButton() {
        ivBack.setOnClickListener(v -> {
            // Simply finish the activity to go back to previous screen
            finish();
        });
    }

    // NEW METHOD: Pre-populate fields with data from intent
    private void populateFieldsFromIntent() {
        String userName = getIntent().getStringExtra("userName");
        String email = getIntent().getStringExtra("email");
        String businessName = getIntent().getStringExtra("businessName");
        String address = getIntent().getStringExtra("address");
        String birthDate = getIntent().getStringExtra("birthDate");
        digitalSignatureBase64 = getIntent().getStringExtra("digitalSignature");

        // Set the fields if data is available
        if (userName != null && !userName.isEmpty()) {
            etName.setText(userName);
        }
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
        if (businessName != null && !businessName.isEmpty()) {
            etBusinessName.setText(businessName);
        }
        if (address != null && !address.isEmpty()) {
            etAddress.setText(address);
        }
        if (birthDate != null && !birthDate.isEmpty()) {
            etBirthDate.setText(birthDate);
        }

        // Display signature if available
        if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
            displaySignature(digitalSignatureBase64);
        }
    }

    private void setupDatePicker() {
        etBirthDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditProfileActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        etBirthDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void setupClickListeners() {
        btnSaveChanges.setOnClickListener(v -> saveChanges());

        btnUpdateSignature.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, SignatureActivity.class);
            startActivityForResult(intent, SIGNATURE_REQUEST_CODE);
        });
    }

    // UPDATED METHOD: Load data from correct Firebase path (info node)
    private void loadCurrentUserData() {
        if (userMobile == null || userMobile.isEmpty()) {
            Toast.makeText(this, "User mobile not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load data from the "info" node under the user's mobile number
        usersRef.child(userMobile).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Load current data into form fields
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String businessName = dataSnapshot.child("businessName").getValue(String.class);
                    String address = dataSnapshot.child("address").getValue(String.class);
                    String birthDate = dataSnapshot.child("birthDate").getValue(String.class);
                    String signature = dataSnapshot.child("digitalSignature").getValue(String.class);

                    // Only populate fields if they are currently empty (to avoid overwriting intent data)
                    if (etName.getText().toString().trim().isEmpty() && name != null) {
                        etName.setText(name);
                    }
                    if (etEmail.getText().toString().trim().isEmpty() && email != null) {
                        etEmail.setText(email);
                    }
                    if (etBusinessName.getText().toString().trim().isEmpty() && businessName != null) {
                        etBusinessName.setText(businessName);
                    }
                    if (etAddress.getText().toString().trim().isEmpty() && address != null) {
                        etAddress.setText(address);
                    }
                    if (etBirthDate.getText().toString().trim().isEmpty() && birthDate != null) {
                        etBirthDate.setText(birthDate);
                    }

                    // Update signature if not already set
                    if ((digitalSignatureBase64 == null || digitalSignatureBase64.isEmpty()) && signature != null) {
                        digitalSignatureBase64 = signature;
                        displaySignature(digitalSignatureBase64);
                    }
                } else {
                    Toast.makeText(EditProfileActivity.this, "User data not found in database", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditProfileActivity.this, "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displaySignature(String signatureBase64) {
        if (signatureBase64 != null && !signatureBase64.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(signatureBase64, Base64.DEFAULT);
                Bitmap signatureBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                if (signatureBitmap != null) {
                    ivSignaturePreview.setImageBitmap(signatureBitmap);
                } else {
                    ivSignaturePreview.setImageResource(R.drawable.ic_signature_placeholder);
                }
            } catch (Exception e) {
                ivSignaturePreview.setImageResource(R.drawable.ic_signature_placeholder);
                Toast.makeText(this, "Error loading signature", Toast.LENGTH_SHORT).show();
            }
        } else {
            ivSignaturePreview.setImageResource(R.drawable.ic_signature_placeholder);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGNATURE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            digitalSignatureBase64 = data.getStringExtra("signature");

            if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
                // Convert base64 back to bitmap for preview
                try {
                    byte[] decodedString = Base64.decode(digitalSignatureBase64, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ivSignaturePreview.setImageBitmap(decodedBitmap);
                    btnUpdateSignature.setText("Update Signature");
                    Toast.makeText(this, "Signature updated", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Error processing signature", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        if (validateInputs(name, email, businessName, address, birthDate)) {
            updateUserProfile(name, email, businessName, address, birthDate);
        }
    }

    private boolean validateInputs(String name, String email, String businessName, String address, String birthDate) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email address is required");
            etEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(businessName)) {
            etBusinessName.setError("Business name is required");
            etBusinessName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Birth date is required");
            etBirthDate.requestFocus();
            return false;
        }
        return true;
    }

    // UPDATED METHOD: Save data to the correct Firebase path (info node)
    private void updateUserProfile(String name, String email, String businessName, String address, String birthDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("businessName", businessName);
        updates.put("address", address);
        updates.put("birthDate", birthDate);

        // Only update signature if it was changed or exists
        if (digitalSignatureBase64 != null && !digitalSignatureBase64.isEmpty()) {
            updates.put("digitalSignature", digitalSignatureBase64);
        }

        updates.put("lastModified", System.currentTimeMillis());

        // Update data in the "info" node ONLY
        usersRef.child(userMobile).child("info").updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to settings
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
