//
//
//package com.ss.rentmangment;
//
//import android.Manifest;
//import android.app.DatePickerDialog;
//import android.content.ActivityNotFoundException;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.pdf.PdfDocument;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.telephony.SmsManager;
//import android.text.TextUtils;
//import android.widget.*;
//import android.view.View;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.content.FileProvider;
//
//import com.google.firebase.database.*;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//public class CollectRentActivity extends AppCompatActivity {
//    private Spinner spinnerTenants, spinnerPaymentMode, spinnerMonth;
//    private EditText etAmount, etPaymentDate, etNotes;
//    private Button btnSubmit, btnPreviewReceipt;
//    private ProgressBar progressBar;
//    private SearchView tenantSearchView;
//    private DatabaseReference tenantsRef, paymentsRef;
//    private String adminId;
//    private final List<Tenant> tenantList = new ArrayList<>();
//    private final List<String> tenantNames = new ArrayList<>();
//    private final Map<String, Double> paidAmountMap = new HashMap<>();
//    private final Map<String, Set<String>> monthsPaidMap = new HashMap<>();
//    private String selectedTenantMobile = "", selectedTenantRoom = "", selectedTenantName = "";
//    private String selectedMonth = "", selectedMonthLabel = "";
//    private File lastReceiptPdf = null;
//    private static final int REQUEST_SMS_PERMISSION = 1001;
//    private CheckBox chkSendSms, chkAutoShareWhatsapp;
//    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//    private boolean smsSent = false;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_collect_rent);
//
//        spinnerTenants = findViewById(R.id.spinnerTenants);
//        spinnerPaymentMode = findViewById(R.id.spinnerPaymentMode);
//        spinnerMonth = findViewById(R.id.spinnerUnpaidMonth); // Add month spinner to layout!
//        etAmount = findViewById(R.id.etAmount);
//        etPaymentDate = findViewById(R.id.etPaymentDate);
//        etNotes = findViewById(R.id.etNotes);
//        btnSubmit = findViewById(R.id.btnSubmitPayment);
//        btnPreviewReceipt = findViewById(R.id.btnPreviewReceipt);
//        progressBar = findViewById(R.id.progressBar);
//        tenantSearchView = findViewById(R.id.tenantSearchView);
//
//        chkSendSms = findViewById(R.id.chkSendSms);
//        chkAutoShareWhatsapp = findViewById(R.id.chkAutoShareWhatsapp);
//
//        progressBar.setVisibility(View.GONE);
//
//        adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("mobile", "default_admin");
//        tenantsRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("tenants");
//        paymentsRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("rentPayments");
//
//        etPaymentDate.setText(sdf.format(new Date()));
//        setupSearchView();
//        btnSubmit.setOnClickListener(v -> submitPayment());
//        btnPreviewReceipt.setOnClickListener(v -> previewLastReceipt());
//
//        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item,
//                new String[]{"Cash", "Bank Transfer", "UPI", "Cheque"});
//        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPaymentMode.setAdapter(modeAdapter);
//
//        etPaymentDate.setOnClickListener(v -> showDatePicker());
//
//        spinnerTenants.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (position >= 0 && position < tenantList.size()) {
//                    Tenant tenant = tenantList.get(position);
//                    selectedTenantMobile = tenant.mobile;
//                    selectedTenantRoom = tenant.roomNumber;
//                    selectedTenantName = tenant.name;
//                    double paid = paidAmountMap.getOrDefault(selectedTenantMobile, 0.0);
//                    double remaining = tenant.rentAmount - paid;
//                    if (remaining < 0) remaining = 0;
//                    etAmount.setText(String.valueOf(remaining));
//                    populateMonthSpinner(selectedTenantMobile, tenant);
//                }
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                selectedTenantMobile = selectedTenantRoom = selectedTenantName = "";
//                etAmount.setText("");
//            }
//        });
//
//        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedMonth = (String) spinnerMonth.getSelectedItem();
//                selectedMonthLabel = selectedMonth;
//            }
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                selectedMonth = "";
//            }
//        });
//
//        loadTenantsAndPayments();
//    }
//
//    private void setupSearchView() {
//        // Remove style/background: No special styles in XML, just default SearchView.
//        // Click anywhere in SearchView to start searching
//        tenantSearchView.setIconifiedByDefault(false); // Make fully clickable
//        tenantSearchView.setQueryHint("Search tenant by name or room");
//        tenantSearchView.clearFocus();
//        tenantSearchView.setFocusable(true);
//        tenantSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) tenantSearchView.clearFocus();
//        });
//        tenantSearchView.setOnClickListener(v -> tenantSearchView.setIconified(false)); // Click opens
//
//        tenantSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                filterTenants(query);
//                return true;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterTenants(newText);
//                return true;
//            }
//        });
//    }
//
//    private void filterTenants(String query) {
//        List<String> filteredNames = new ArrayList<>();
//        List<Tenant> filteredList = new ArrayList<>();
//        for (Tenant t : tenantList) {
//            double paid = paidAmountMap.getOrDefault(t.mobile, 0.0);
//            boolean canShow = paid < t.rentAmount;
//            if ((t.name.toLowerCase().contains(query.toLowerCase()) ||
//                    t.roomNumber.toLowerCase().contains(query.toLowerCase())) && canShow) {
//                double remaining = t.rentAmount - paid;
//                if (remaining < 0) remaining = 0;
//                filteredNames.add(t.name + " (" + t.roomNumber + ") [" + String.format("%.2f", remaining) + " left]");
//                filteredList.add(t);
//            }
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, filteredNames);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerTenants.setAdapter(adapter);
//        tenantList.clear();
//        tenantList.addAll(filteredList);
//    }
//
//    private void loadTenantsAndPayments() {
//        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
//                paymentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
//                        paidAmountMap.clear();
//                        monthsPaidMap.clear();
//                        // Calculate payments for each tenant by mobile
//                        for (DataSnapshot tenantPayments : paymentSnapshot.getChildren()) {
//                            String mobile = tenantPayments.getKey();
//                            if (mobile == null) continue;
//                            double totalPaid = 0;
//                            Set<String> monthsPaid = new HashSet<>();
//                            for (DataSnapshot pay : tenantPayments.getChildren()) {
//                                RentPayment rp = pay.getValue(RentPayment.class);
//                                if (rp != null && !TextUtils.isEmpty(rp.paymentDate)) {
//                                    try {
//                                        Date payDate = sdf.parse(rp.paymentDate);
//                                        if (payDate != null) {
//                                            Calendar cal = Calendar.getInstance();
//                                            cal.setTime(payDate);
//                                            String monthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
//                                            monthsPaid.add(monthYear);
//                                            totalPaid += rp.amountPaid;
//                                        }
//                                    } catch (ParseException ignored) {}
//                                }
//                            }
//                            paidAmountMap.put(mobile, totalPaid);
//                            monthsPaidMap.put(mobile, monthsPaid);
//                        }
//                        tenantList.clear();
//                        tenantNames.clear();
//                        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
//                            Tenant tenant = ds.getValue(Tenant.class);
//                            if (tenant != null && tenant.mobile != null) {
//                                double paid = paidAmountMap.getOrDefault(tenant.mobile, 0.0);
//                                if (paid < tenant.rentAmount) {
//                                    double remaining = tenant.rentAmount - paid;
//                                    tenantList.add(tenant);
//                                    tenantNames.add(tenant.name + " (" + tenant.roomNumber + ") [" + String.format("%.2f", remaining) + " left]");
//                                }
//                            }
//                        }
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CollectRentActivity.this,
//                                android.R.layout.simple_spinner_item, tenantNames);
//                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        spinnerTenants.setAdapter(adapter);
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//                        Toast.makeText(CollectRentActivity.this, "Failed to load payments", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(CollectRentActivity.this, "Failed to load tenants", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void populateMonthSpinner(String mobile, Tenant tenant) {
//        // Always show last 3 months for which payment is not received
//        List<String> monthsList = new ArrayList<>();
//        Set<String> monthsPaid = monthsPaidMap.getOrDefault(mobile, new HashSet<>());
//        Calendar now = Calendar.getInstance();
//        for (int i = 0; i < 3; i++) {
//            int month = now.get(Calendar.MONTH) - i;
//            int year = now.get(Calendar.YEAR);
//            if (month < 0) {
//                month += 12;
//                year--;
//            }
//            String monthYear = (month + 1) + "/" + year;
//            if (!monthsPaid.contains(monthYear)) monthsList.add(monthYear);
//        }
//        if (monthsList.isEmpty()) monthsList.add(getCurrentMonthYear());
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_spinner_item, monthsList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerMonth.setAdapter(adapter);
//        selectedMonth = monthsList.get(0);
//        selectedMonthLabel = monthsList.get(0);
//    }
//
//    private String getCurrentMonthYear() {
//        Calendar cal = Calendar.getInstance();
//        return (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
//    }
//
//    private void showDatePicker() {
//        Calendar c = Calendar.getInstance();
//        new DatePickerDialog(this, (v, y, m, d) -> etPaymentDate.setText(d + "/" + (m + 1) + "/" + y),
//                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    private void showProgress(boolean show) {
//        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
//        btnSubmit.setEnabled(!show);
//    }
//
//    private void submitPayment() {
//        showProgress(true);
//        smsSent = false; // SMS trigger state
//
//        String amountStr = etAmount.getText().toString().trim();
//        String paymentDate = etPaymentDate.getText().toString().trim();
//        String paymentMode = (String) spinnerPaymentMode.getSelectedItem();
//        String notes = etNotes.getText().toString().trim();
//
//        if (TextUtils.isEmpty(selectedTenantMobile)) {
//            Toast.makeText(this, "Please select a tenant", Toast.LENGTH_SHORT).show();
//            showProgress(false);
//            return;
//        }
//        if (TextUtils.isEmpty(amountStr) || Double.parseDouble(amountStr) <= 0) {
//            etAmount.setError("Enter valid amount");
//            etAmount.requestFocus();
//            showProgress(false);
//            return;
//        }
//        if (TextUtils.isEmpty(paymentDate)) {
//            etPaymentDate.setError("Select payment date");
//            etPaymentDate.requestFocus();
//            showProgress(false);
//            return;
//        }
//        if (TextUtils.isEmpty(selectedMonthLabel)) {
//            Toast.makeText(this, "Select rent month", Toast.LENGTH_SHORT).show();
//            showProgress(false);
//            return;
//        }
//
//        double amount = Double.parseDouble(amountStr);
//        String paymentId = paymentsRef.child(selectedTenantMobile).push().getKey();
//        if (paymentId == null) {
//            Toast.makeText(this, "Error generating payment ID", Toast.LENGTH_SHORT).show();
//            showProgress(false);
//            return;
//        }
//
//        String monthNotes = notes + " (Paid For Month: " + selectedMonthLabel + ")";
//
//        RentPayment payment = new RentPayment(paymentId, selectedTenantMobile,
//                selectedTenantRoom, amount, paymentDate, paymentMode, monthNotes);
//
//        paymentsRef.child(selectedTenantMobile).child(paymentId).setValue(payment)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(this, "Rent collected successfully", Toast.LENGTH_SHORT).show();
//
//                    String smsMsg = "Dear " + selectedTenantName + ", we have received your rent payment of ₹" + amount +
//                            " for " + selectedMonthLabel + ". Thank you.";
//
//                    if (chkSendSms.isChecked() && !smsSent) {
//                        sendSmsToTenant(selectedTenantMobile, smsMsg);
//                        smsSent = true; // Only send once!
//                    }
//                    if (chkAutoShareWhatsapp.isChecked()) {
//                        generatePdfAndSendWhatsApp(payment, selectedTenantMobile);
//                    } else {
//                        try {
//                            lastReceiptPdf = createPdf(payment);
//                        } catch (IOException e) {
//                            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    new Handler().postDelayed(() -> {
//                        showProgress(false);
//                        finish();
//                    }, 1500);
//                })
//                .addOnFailureListener(e -> {
//                    showProgress(false);
//                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void sendSmsToTenant(String phoneNumber, String message) {
//        if (TextUtils.isEmpty(phoneNumber)) {
//            Toast.makeText(this, "No mobile number for tenant", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.SEND_SMS},
//                    REQUEST_SMS_PERMISSION);
//            return;
//        }
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            ArrayList<String> parts = smsManager.divideMessage(message);
//            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
//            Toast.makeText(this, "SMS sent to tenant", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_SMS_PERMISSION) {
//            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void generatePdfAndSendWhatsApp(RentPayment payment, String mobile) {
//        try {
//            File pdfFile = createPdf(payment);
//            lastReceiptPdf = pdfFile;
//            if (pdfFile != null && pdfFile.exists()) {
//                openWhatsAppDirectChatWithPdf(pdfFile, mobile);
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
//
//    // WhatsApp direct chat using API URL (phone must be in international format without +)
//    private void openWhatsAppDirectChatWithPdf(File pdfFile, String mobile) {
//        if (TextUtils.isEmpty(mobile)) {
//            Toast.makeText(this, "No WhatsApp number for this tenant", Toast.LENGTH_LONG).show();
//            return;
//        }
//        if (pdfFile == null || !pdfFile.exists()) {
//            Toast.makeText(this, "Receipt PDF not found", Toast.LENGTH_LONG).show();
//            return;
//        }
//        // Prepare the WhatsApp API URL for direct chat
//        String formattedMobile = mobile;
//        if (mobile.startsWith("0")) {
//            formattedMobile = "91" + mobile.substring(1); // assuming India; use country code!
//        } else if (!mobile.startsWith("91")) {
//            formattedMobile = "91" + mobile; // default to India
//        }
//        Uri pdfUri = FileProvider.getUriForFile(this,
//                getPackageName() + ".fileprovider", pdfFile);
//        Intent sendIntent = new Intent(Intent.ACTION_SEND);
//        sendIntent.setType("application/pdf");
//        sendIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
//        sendIntent.putExtra(Intent.EXTRA_TEXT,
//                "Dear " + selectedTenantName + ", please find your rent receipt attached.");
//        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        // WhatsApp direct chat: open chat with number
//        sendIntent.setPackage("com.whatsapp");
//        sendIntent.putExtra("jid", formattedMobile + "@s.whatsapp.net"); // direct chat JID
//
//        try {
//            startActivity(sendIntent);
//        } catch (ActivityNotFoundException e) {
//            // fallback to WhatsApp API URL
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setData(Uri.parse("https://wa.me/" + formattedMobile + "?text=" +
//                    Uri.encode("Dear " + selectedTenantName + ", please find your rent receipt attached.")));
//            try {
//                startActivity(intent);
//            } catch (Exception ex) {
//                Toast.makeText(this, "WhatsApp not installed on device", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private File createPdf(RentPayment payment) throws IOException {
//        PdfDocument doc = new PdfDocument();
//        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 440, 1).create();
//        PdfDocument.Page page = doc.startPage(pageInfo);
//        Canvas c = page.getCanvas();
//        Paint p = new Paint();
//        p.setColor(Color.BLACK);
//        p.setTextSize(18);
//        p.setTextAlign(Paint.Align.CENTER);
//        int y = 40;
//        c.drawText("RENT RECEIPT", pageInfo.getPageWidth() / 2, y, p);
//        p.setTextSize(12);
//        p.setTextAlign(Paint.Align.LEFT);
//        y += 30;
//        c.drawText("Tenant Name: " + selectedTenantName, 20, y, p);
//        y += 20;
//        c.drawText("Room No : " + selectedTenantRoom, 20, y, p);
//        y += 20;
//        c.drawText("Paid For : " + extractMonthFromNotes(payment.notes), 20, y, p);
//        y += 20;
//        c.drawText("Payment Date: " + payment.paymentDate, 20, y, p);
//        y += 20;
//        c.drawText("Payment Mode: " + payment.paymentMode, 20, y, p);
//        y += 20;
//        c.drawText(String.format(Locale.getDefault(), "Amount Paid: ₹%.2f", payment.amountPaid), 20, y, p);
//        y += 20;
//        String noteText = TextUtils.isEmpty(payment.notes) ? "N/A" : payment.notes;
//        c.drawText("Notes: " + noteText, 20, y, p);
//        doc.finishPage(page);
//        File dir = new File(getCacheDir(), "pdfs");
//        if (!dir.exists()) dir.mkdirs();
//        File file = new File(dir, "RentReceipt_" + System.currentTimeMillis() + ".pdf");
//        try (FileOutputStream fos = new FileOutputStream(file)) {
//            doc.writeTo(fos);
//        }
//        doc.close();
//        return file;
//    }
//
//    private String extractMonthFromNotes(String notes) {
//        if (notes.contains("Paid For Month: ")) {
//            return notes.substring(notes.indexOf("Paid For Month: ") + 16).replace(")", "").trim();
//        }
//        return "N/A";
//    }
//
//    private void previewLastReceipt() {
//        if (lastReceiptPdf != null && lastReceiptPdf.exists()) {
//            Uri pdfUri = FileProvider.getUriForFile(this,
//                    getPackageName() + ".fileprovider", lastReceiptPdf);
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(pdfUri, "application/pdf");
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            try {
//                startActivity(intent);
//            } catch (ActivityNotFoundException e) {
//                Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
//            }
//        } else {
//            Toast.makeText(this, "No receipt to preview", Toast.LENGTH_SHORT).show();
//        }
//    }
//}


//upper code proper run , proper means proper



package com.ss.rentmangment;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.widget.*;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CollectRentActivity extends AppCompatActivity {
    private Spinner spinnerTenants, spinnerPaymentMode, spinnerMonth;
    private EditText etAmount, etPaymentDate, etNotes;
    private Button btnSubmit, btnPreviewReceipt;
    private ProgressBar progressBar;
    private SearchView tenantSearchView;
    private DatabaseReference tenantsRef, paymentsRef;
    private String adminId;
    private final List<Tenant> tenantList = new ArrayList<>();
    private final List<String> tenantNames = new ArrayList<>();
    private final Map<String, Double> paidAmountMap = new HashMap<>();
    private final Map<String, Set<String>> monthsPaidMap = new HashMap<>();
    private String selectedTenantMobile = "", selectedTenantRoom = "", selectedTenantName = "";
    private String selectedMonth = "", selectedMonthLabel = "";
    private File lastReceiptPdf = null;
    private static final int REQUEST_SMS_PERMISSION = 1001;
    private CheckBox chkSendSms, chkAutoShareWhatsapp;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private boolean smsSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_rent);

        spinnerTenants = findViewById(R.id.spinnerTenants);
        spinnerPaymentMode = findViewById(R.id.spinnerPaymentMode);
        spinnerMonth = findViewById(R.id.spinnerUnpaidMonth); // Add month spinner to layout!
        etAmount = findViewById(R.id.etAmount);
        etPaymentDate = findViewById(R.id.etPaymentDate);
        etNotes = findViewById(R.id.etNotes);
        btnSubmit = findViewById(R.id.btnSubmitPayment);
        btnPreviewReceipt = findViewById(R.id.btnPreviewReceipt);
        progressBar = findViewById(R.id.progressBar);
        tenantSearchView = findViewById(R.id.tenantSearchView);

        chkSendSms = findViewById(R.id.chkSendSms);
        chkAutoShareWhatsapp = findViewById(R.id.chkAutoShareWhatsapp);

        progressBar.setVisibility(View.GONE);

        adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("mobile", "default_admin");
        tenantsRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("tenants");
        paymentsRef = FirebaseDatabase.getInstance().getReference("users").child(adminId).child("rentPayments");

        etPaymentDate.setText(sdf.format(new Date()));
        setupSearchView();
        btnSubmit.setOnClickListener(v -> submitPayment());
        btnPreviewReceipt.setOnClickListener(v -> previewLastReceipt());

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Cash", "Bank Transfer", "UPI", "Cheque"});
        modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaymentMode.setAdapter(modeAdapter);

        etPaymentDate.setOnClickListener(v -> showDatePicker());

        spinnerTenants.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < tenantList.size()) {
                    Tenant tenant = tenantList.get(position);
                    selectedTenantMobile = tenant.mobile;
                    selectedTenantRoom = tenant.roomNumber;
                    selectedTenantName = tenant.name;
                    double paid = paidAmountMap.getOrDefault(selectedTenantMobile, 0.0);
                    double remaining = tenant.rentAmount - paid;
                    if (remaining < 0) remaining = 0;
                    etAmount.setText(String.valueOf(remaining));
                    populateMonthSpinner(selectedTenantMobile, tenant);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedTenantMobile = selectedTenantRoom = selectedTenantName = "";
                etAmount.setText("");
            }
        });

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = (String) spinnerMonth.getSelectedItem();
                selectedMonthLabel = selectedMonth;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMonth = "";
            }
        });

        loadTenantsAndPayments();
    }

    private void setupSearchView() {
        tenantSearchView.setIconifiedByDefault(false); // Make fully clickable
        tenantSearchView.setQueryHint("Search tenant by name or room");
        tenantSearchView.clearFocus();
        tenantSearchView.setFocusable(true);
        tenantSearchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) tenantSearchView.clearFocus();
        });
        tenantSearchView.setOnClickListener(v -> tenantSearchView.setIconified(false));

        tenantSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterTenants(query);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterTenants(newText);
                return true;
            }
        });
    }

    private void filterTenants(String query) {
        List<String> filteredNames = new ArrayList<>();
        List<Tenant> filteredList = new ArrayList<>();
        for (Tenant t : tenantList) {
            double paid = paidAmountMap.getOrDefault(t.mobile, 0.0);
            boolean canShow = paid < t.rentAmount;
            if ((t.name.toLowerCase().contains(query.toLowerCase()) ||
                    t.roomNumber.toLowerCase().contains(query.toLowerCase())) && canShow) {
                double remaining = t.rentAmount - paid;
                if (remaining < 0) remaining = 0;
                filteredNames.add(t.name + " (" + t.roomNumber + ") [" + String.format("%.2f", remaining) + " left]");
                filteredList.add(t);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filteredNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTenants.setAdapter(adapter);
        tenantList.clear();
        tenantList.addAll(filteredList);
    }

    private void loadTenantsAndPayments() {
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                paymentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
                        paidAmountMap.clear();
                        monthsPaidMap.clear();
                        for (DataSnapshot tenantPayments : paymentSnapshot.getChildren()) {
                            String mobile = tenantPayments.getKey();
                            if (mobile == null) continue;
                            double totalPaid = 0;
                            Set<String> monthsPaid = new HashSet<>();
                            for (DataSnapshot pay : tenantPayments.getChildren()) {
                                RentPayment rp = pay.getValue(RentPayment.class);
                                if (rp != null && !TextUtils.isEmpty(rp.paymentDate)) {
                                    try {
                                        Date payDate = sdf.parse(rp.paymentDate);
                                        if (payDate != null) {
                                            Calendar cal = Calendar.getInstance();
                                            cal.setTime(payDate);
                                            String monthYear = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
                                            monthsPaid.add(monthYear);
                                            totalPaid += rp.amountPaid;
                                        }
                                    } catch (ParseException ignored) {}
                                }
                            }
                            paidAmountMap.put(mobile, totalPaid);
                            monthsPaidMap.put(mobile, monthsPaid);
                        }

                        tenantList.clear();
                        tenantNames.clear();
                        Calendar now = Calendar.getInstance();

                        final int lookbackMonths = 2; // Check dues for last 2 months

                        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
                            Tenant tenant = ds.getValue(Tenant.class);
                            if (tenant != null && tenant.mobile != null) {
                                double paid = paidAmountMap.getOrDefault(tenant.mobile, 0.0);
                                double remaining = tenant.rentAmount - paid;
                                if (remaining <= 0) continue; // fully paid, skip

                                Set<String> paidMonths = monthsPaidMap.getOrDefault(tenant.mobile, new HashSet<>());
                                boolean hasPending = false;

                                for (int i = 0; i < lookbackMonths; i++) {
                                    int month = now.get(Calendar.MONTH) - i;
                                    int year = now.get(Calendar.YEAR);
                                    if (month < 0) {
                                        month += 12;
                                        year--;
                                    }
                                    String monthYear = (month + 1) + "/" + year;
                                    if (!paidMonths.contains(monthYear)) {
                                        hasPending = true;
                                        break;
                                    }
                                }

                                if (hasPending) {
                                    tenantList.add(tenant);
                                    tenantNames.add(tenant.name + " (" + tenant.roomNumber + ") [" + String.format("%.2f", remaining) + " remaining]");
                                }
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(CollectRentActivity.this,
                                android.R.layout.simple_spinner_item, tenantNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTenants.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CollectRentActivity.this, "Failed to load payments", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CollectRentActivity.this, "Failed to load tenants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateMonthSpinner(String mobile, Tenant tenant) {
        List<String> monthsList = new ArrayList<>();
        Set<String> monthsPaid = monthsPaidMap.getOrDefault(mobile, new HashSet<>());
        Calendar now = Calendar.getInstance();
        for (int i = 0; i < 3; i++) {
            int month = now.get(Calendar.MONTH) - i;
            int year = now.get(Calendar.YEAR);
            if (month < 0) {
                month += 12;
                year--;
            }
            String monthYear = (month + 1) + "/" + year;
            if (!monthsPaid.contains(monthYear)) monthsList.add(monthYear);
        }
        if (monthsList.isEmpty()) monthsList.add(getCurrentMonthYear());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, monthsList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);
        selectedMonth = monthsList.get(0);
        selectedMonthLabel = monthsList.get(0);
    }

    private String getCurrentMonthYear() {
        Calendar cal = Calendar.getInstance();
        return (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> etPaymentDate.setText(d + "/" + (m + 1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!show);
    }

    private void submitPayment() {
        showProgress(true);
        smsSent = false;

        String amountStr = etAmount.getText().toString().trim();
        String paymentDate = etPaymentDate.getText().toString().trim();
        String paymentMode = (String) spinnerPaymentMode.getSelectedItem();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(selectedTenantMobile)) {
            Toast.makeText(this, "Please select a tenant", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }
        if (TextUtils.isEmpty(amountStr) || Double.parseDouble(amountStr) <= 0) {
            etAmount.setError("Enter valid amount");
            etAmount.requestFocus();
            showProgress(false);
            return;
        }
        if (TextUtils.isEmpty(paymentDate)) {
            etPaymentDate.setError("Select payment date");
            etPaymentDate.requestFocus();
            showProgress(false);
            return;
        }
        if (TextUtils.isEmpty(selectedMonthLabel)) {
            Toast.makeText(this, "Select rent month", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String paymentDateKey;
        try {
            Date date = sdf.parse(paymentDate);
            paymentDateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid payment date format", Toast.LENGTH_SHORT).show();
            showProgress(false);
            return;
        }

        // Use paymentDateKey as paymentId under mobile number
        String paymentId = paymentDateKey;

        String monthNotes = notes + " (Paid For Month: " + selectedMonthLabel + ")";

        RentPayment payment = new RentPayment(paymentId, selectedTenantMobile,
                selectedTenantRoom, amount, paymentDate, paymentMode, monthNotes);

        paymentsRef.child(selectedTenantMobile).child(paymentId).setValue(payment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rent collected successfully", Toast.LENGTH_SHORT).show();

                    String smsMsg = "Dear " + selectedTenantName + ", we have received your rent payment of ₹"
                            + amount + " for " + selectedMonthLabel + ". Thank you.";

                    if (chkSendSms.isChecked() && !smsSent) {
                        sendSmsToTenant(selectedTenantMobile, smsMsg);
                        smsSent = true;
                    }
                    if (chkAutoShareWhatsapp.isChecked()) {
                        generatePdfAndSendWhatsApp(payment, selectedTenantMobile);
                    } else {
                        try {
                            lastReceiptPdf = createPdf(payment);
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    new Handler().postDelayed(() -> {
                        showProgress(false);
                        finish();
                    }, 1500);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void sendSmsToTenant(String phoneNumber, String message) {
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "No mobile number for tenant", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    REQUEST_SMS_PERMISSION);
            return;
        }
        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
            Toast.makeText(this, "SMS sent to tenant", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SMS_PERMISSION) {
            Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void generatePdfAndSendWhatsApp(RentPayment payment, String mobile) {
        try {
            File pdfFile = createPdf(payment);
            lastReceiptPdf = pdfFile;
            if (pdfFile != null && pdfFile.exists()) {
                openWhatsAppDirectChatWithPdf(pdfFile, mobile);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openWhatsAppDirectChatWithPdf(File pdfFile, String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "No WhatsApp number for this tenant", Toast.LENGTH_LONG).show();
            return;
        }
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(this, "Receipt PDF not found", Toast.LENGTH_LONG).show();
            return;
        }
        String formattedMobile = mobile;
        if (mobile.startsWith("0")) {
            formattedMobile = "91" + mobile.substring(1);
        } else if (!mobile.startsWith("91")) {
            formattedMobile = "91" + mobile;
        }
        Uri pdfUri = FileProvider.getUriForFile(this,
                getPackageName() + ".fileprovider", pdfFile);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("application/pdf");
        sendIntent.putExtra(Intent.EXTRA_STREAM, pdfUri);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "Dear " + selectedTenantName + ", please find your rent receipt attached.");
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        sendIntent.setPackage("com.whatsapp");
        sendIntent.putExtra("jid", formattedMobile + "@s.whatsapp.net");
        try {
            startActivity(sendIntent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + formattedMobile + "?text=" +
                    Uri.encode("Dear " + selectedTenantName + ", please find your rent receipt attached.")));
            try {
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "WhatsApp not installed on device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File createPdf(RentPayment payment) throws IOException {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 440, 1).create();
        PdfDocument.Page page = doc.startPage(pageInfo);
        Canvas c = page.getCanvas();
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(18);
        p.setTextAlign(Paint.Align.CENTER);
        int y = 40;
        c.drawText("RENT RECEIPT", pageInfo.getPageWidth() / 2, y, p);
        p.setTextSize(12);
        p.setTextAlign(Paint.Align.LEFT);
        y += 30;
        c.drawText("Tenant Name: " + selectedTenantName, 20, y, p);
        y += 20;
        c.drawText("Room No : " + selectedTenantRoom, 20, y, p);
        y += 20;
        c.drawText("Paid For : " + extractMonthFromNotes(payment.notes), 20, y, p);
        y += 20;
        c.drawText("Payment Date: " + payment.paymentDate, 20, y, p);
        y += 20;
        c.drawText("Payment Mode: " + payment.paymentMode, 20, y, p);
        y += 20;
        c.drawText(String.format(Locale.getDefault(), "Amount Paid: ₹%.2f", payment.amountPaid), 20, y, p);
        y += 20;
        String noteText = TextUtils.isEmpty(payment.notes) ? "N/A" : payment.notes;
        c.drawText("Notes: " + noteText, 20, y, p);
        doc.finishPage(page);
        File dir = new File(getCacheDir(), "pdfs");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, "RentReceipt_" + System.currentTimeMillis() + ".pdf");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            doc.writeTo(fos);
        }
        doc.close();
        return file;
    }

    private String extractMonthFromNotes(String notes) {
        if (notes.contains("Paid For Month: ")) {
            return notes.substring(notes.indexOf("Paid For Month: ") + 16).replace(")", "").trim();
        }
        return "N/A";
    }

    private void previewLastReceipt() {
        if (lastReceiptPdf != null && lastReceiptPdf.exists()) {
            Uri pdfUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", lastReceiptPdf);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No receipt to preview", Toast.LENGTH_SHORT).show();
        }
    }
}
