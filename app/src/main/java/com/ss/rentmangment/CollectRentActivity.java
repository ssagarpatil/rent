//package com.ss.rentmangment;
//
//import android.app.DatePickerDialog;
//import android.content.Context;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.SearchView;
//import androidx.appcompat.widget.Toolbar;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.tabs.TabLayout;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//public class CollectRentActivity extends AppCompatActivity implements RentCollectionAdapter.OnCollectButtonClickListener {
//
//    private static final String TAG = "CollectRentActivity";
//
//    // UI Components
//    private Toolbar toolbar;
//    private TabLayout tabLayout;
//    private RecyclerView rvTenants;
//    private TextView tvEmptyState, tvMonthSelector, tvCollectionSummary;
//    private Button btnPreviousMonth, btnNextMonth;
//    private LinearLayout layoutMonthNavigation;
//
//    // Data Lists
//    private RentCollectionAdapter adapter;
//    private List<TenantProfile> allTenants = new ArrayList<>();
//    private List<TenantProfile> pendingTenants = new ArrayList<>();
//    private List<TenantProfile> collectedTenants = new ArrayList<>();
//
//    // Firebase References
//    private DatabaseReference tenantsRef, paymentsRef;
//    private String adminId, selectedMonth;
//    private ValueEventListener tenantsListener, paymentsListener;
//
//    // Month Navigation
//    private Calendar currentCalendar;
//    private Calendar earliestTenantCalendar;
//    private Calendar latestAllowedCalendar;
//
//    // Tab Constants
//    private static final int TAB_PENDING = 0;
//    private static final int TAB_COLLECTED = 1;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_collect_rent);
//
//        adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//                .getString("mobile", "default_admin");
//
//        initializeCalendars();
//        selectedMonth = getCurrentMonthYear();
//
//        setupUI();
//        setupFirebase();
//        loadEarliestTenantDate();
//    }
//
//    private void initializeCalendars() {
//        currentCalendar = Calendar.getInstance();
//        earliestTenantCalendar = Calendar.getInstance();
//        latestAllowedCalendar = Calendar.getInstance();
//        latestAllowedCalendar.add(Calendar.MONTH, 0);
//    }
//
//    private void setupUI() {
//        toolbar = findViewById(R.id.toolbarCollectRent);
//        setSupportActionBar(toolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            getSupportActionBar().setTitle("Rent Collection");
//        }
//
//        tabLayout = findViewById(R.id.tabLayout);
//        rvTenants = findViewById(R.id.rvTenants);
//        tvEmptyState = findViewById(R.id.tvEmptyState);
//        tvMonthSelector = findViewById(R.id.tvMonthSelector);
//        tvCollectionSummary = findViewById(R.id.tvCollectionSummary);
//
//        layoutMonthNavigation = findViewById(R.id.layoutMonthNavigation);
//        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
//        btnNextMonth = findViewById(R.id.btnNextMonth);
//
//        rvTenants.setLayoutManager(new LinearLayoutManager(this));
//
//        setupMonthNavigation();
//        setupTabs();
//    }
//
//    private void setupMonthNavigation() {
//        updateMonthDisplay();
//        updateNavigationButtons();
//
//        tvMonthSelector.setOnClickListener(v -> showMonthSelectorDialog());
//
//        btnPreviousMonth.setOnClickListener(v -> {
//            Calendar cal = parseMonthYear(selectedMonth);
//            cal.add(Calendar.MONTH, -1);
//
//            if (!cal.before(earliestTenantCalendar)) {
//                selectedMonth = formatCalendarToMonthYear(cal);
//                updateMonthDisplay();
//                updateNavigationButtons();
//                loadMonthlyData();
//            }
//        });
//
//        btnNextMonth.setOnClickListener(v -> {
//            Calendar cal = parseMonthYear(selectedMonth);
//            cal.add(Calendar.MONTH, 1);
//
//            if (!cal.after(latestAllowedCalendar)) {
//                selectedMonth = formatCalendarToMonthYear(cal);
//                updateMonthDisplay();
//                updateNavigationButtons();
//                loadMonthlyData();
//            }
//        });
//    }
//
//    private void setupTabs() {
//        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
//        tabLayout.addTab(tabLayout.newTab().setText("Collected"));
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                filterListByTab(tab.getPosition());
//            }
//            @Override public void onTabUnselected(TabLayout.Tab tab) {}
//            @Override public void onTabReselected(TabLayout.Tab tab) {}
//        });
//    }
//
//    private void updateNavigationButtons() {
//        Calendar selectedCal = parseMonthYear(selectedMonth);
//
//        Calendar prevMonth = (Calendar) selectedCal.clone();
//        prevMonth.add(Calendar.MONTH, -1);
//        btnPreviousMonth.setEnabled(!prevMonth.before(earliestTenantCalendar));
//
//        Calendar nextMonth = (Calendar) selectedCal.clone();
//        nextMonth.add(Calendar.MONTH, 1);
//        btnNextMonth.setEnabled(!nextMonth.after(latestAllowedCalendar));
//
//        btnPreviousMonth.setAlpha(btnPreviousMonth.isEnabled() ? 1.0f : 0.5f);
//        btnNextMonth.setAlpha(btnNextMonth.isEnabled() ? 1.0f : 0.5f);
//    }
//
//    private void setupFirebase() {
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        tenantsRef = database.getReference("users").child(adminId).child("tenants");
//        paymentsRef = database.getReference("users").child(adminId).child("rentPayments");
//    }
//
//    private void loadEarliestTenantDate() {
//        tenantsRef.orderByChild("leaseStartDate").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String earliestDate = null;
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    Tenant tenant = ds.getValue(Tenant.class);
//                    if (tenant != null && tenant.leaseStartDate != null && !tenant.leaseStartDate.isEmpty()) {
//                        earliestDate = tenant.leaseStartDate;
//                        break;
//                    }
//                }
//
//                if (earliestDate != null) {
//                    setEarliestTenantDate(earliestDate);
//                } else {
//                    Calendar cal = Calendar.getInstance();
//                    cal.set(Calendar.MONTH, Calendar.JANUARY);
//                    earliestTenantCalendar = cal;
//                }
//
//                updateNavigationButtons();
//                loadMonthlyData();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to load earliest tenant date: " + error.getMessage());
//                Calendar cal = Calendar.getInstance();
//                cal.set(Calendar.MONTH, Calendar.JANUARY);
//                earliestTenantCalendar = cal;
//                updateNavigationButtons();
//                loadMonthlyData();
//            }
//        });
//    }
//
//    private void setEarliestTenantDate(String joiningDate) {
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            Date date = sdf.parse(joiningDate);
//            if (date != null) {
//                earliestTenantCalendar.setTime(date);
//                earliestTenantCalendar.set(Calendar.DAY_OF_MONTH, 1);
//            }
//        } catch (ParseException e) {
//            Log.e(TAG, "Error parsing earliest tenant date: " + e.getMessage());
//            Calendar cal = Calendar.getInstance();
//            cal.set(Calendar.MONTH, Calendar.JANUARY);
//            earliestTenantCalendar = cal;
//        }
//    }
//
//    private void loadMonthlyData() {
//        Log.d(TAG, "Loading data for month: " + selectedMonth);
//        clearAllLists();
//        loadPaymentDataForMonth();
//    }
//
//    private void loadPaymentDataForMonth() {
//        if (paymentsListener != null) {
//            paymentsRef.removeEventListener(paymentsListener);
//        }
//
//        paymentsListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
//                Log.d(TAG, "Payment data loaded, now loading tenants");
//                loadTenantsData(paymentSnapshot);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to load payment data: " + error.getMessage());
//                showError("Failed to load payment data: " + error.getMessage());
//            }
//        };
//
//        paymentsRef.addValueEventListener(paymentsListener);
//    }
//
//    private void loadTenantsData(DataSnapshot paymentSnapshot) {
//        if (tenantsListener != null) {
//            tenantsRef.removeEventListener(tenantsListener);
//        }
//
//        tenantsListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
//                Log.d(TAG, "Tenants data loaded, processing...");
//                processTenantsAndPayments(tenantSnapshot, paymentSnapshot);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "Failed to load tenants: " + error.getMessage());
//                showError("Failed to load tenants: " + error.getMessage());
//            }
//        };
//
//        tenantsRef.orderByChild("name").addValueEventListener(tenantsListener);
//    }
//
//    private void processTenantsAndPayments(DataSnapshot tenantSnapshot, DataSnapshot paymentSnapshot) {
//        clearAllLists();
//
//        final double[] totalExpected = {0.0};
//        final double[] totalCollected = {0.0};
//        final double[] totalPending = {0.0};
//        final int[] fullyPaidCount = {0};
//        final int[] partialPaidCount = {0};
//        final int[] unpaidCount = {0};
//
//        Calendar selectedMonthCal = parseMonthYear(selectedMonth);
//
//        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
//            Tenant tenant = ds.getValue(Tenant.class);
//            if (tenant != null) {
//                // **UPDATED: Include both Active and Left tenants**
//                boolean isActive = "Active".equalsIgnoreCase(tenant.status);
//                boolean isLeft = "Left".equalsIgnoreCase(tenant.status);
//
//                if (isActive || isLeft) {
//                    // **Check if tenant should be shown for the selected month**
//                    if (!shouldTenantAppearInMonth(tenant, selectedMonthCal)) {
//                        Log.d(TAG, String.format("Tenant %s not shown in %s (joined later or left earlier)",
//                                tenant.name, formatMonthYear(selectedMonth)));
//                        continue;
//                    }
//
//                    TenantProfile profile = createTenantProfile(tenant, paymentSnapshot);
//
//                    // **Mark profile status for UI display**
//                    profile.setTenantStatus(tenant.status);
//
//                    totalExpected[0] += tenant.rentAmount;
//
//                    if ("FULL".equals(profile.getPaymentStatus())) {
//                        fullyPaidCount[0]++;
//                        totalCollected [0]+= profile.getAmountPaid();
//                        collectedTenants.add(profile);
//                    } else if ("PARTIAL".equals(profile.getPaymentStatus())) {
//                        partialPaidCount[0]++;
//                        totalCollected[0] += profile.getAmountPaid();
//                        totalPending[0] += profile.getRemainingAmount();
//                        collectedTenants.add(profile);
//                    } else {
//                        unpaidCount[0]++;
//                        totalPending[0] += tenant.rentAmount;
//                        pendingTenants.add(profile);
//                    }
//
//                    allTenants.add(profile);
//
//                    Log.d(TAG, String.format("Added tenant %s (%s) for month %s",
//                            tenant.name, tenant.status, formatMonthYear(selectedMonth)));
//                }
//            }
//        }
//
//        runOnUiThread(() -> {
//            updateCollectionSummary(totalExpected[0], totalCollected[0], totalPending[0],
//                    fullyPaidCount[0], partialPaidCount[0], unpaidCount[0]);
//            updateTabTitles();
//            filterListByTab(tabLayout.getSelectedTabPosition());
//            updateMonthlyStats();
//        });
//
//        Log.d(TAG, String.format("Processed for %s: Total=%d (Active+Left), Pending=%d, Collected=%d",
//                formatMonthYear(selectedMonth), allTenants.size(), pendingTenants.size(), collectedTenants.size()));
//    }
//
//    /**
//     * **UPDATED METHOD: Determines if a tenant should appear in the selected month**
//     * Handles both Active and Left tenants based on their lease dates
//     */
//    private boolean shouldTenantAppearInMonth(Tenant tenant, Calendar selectedMonth) {
//        if (tenant.leaseStartDate == null || tenant.leaseStartDate.isEmpty()) {
//            Log.d(TAG, "No joining date for " + tenant.name + ", including by default");
//            return true;
//        }
//
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            Date joiningDate = sdf.parse(tenant.leaseStartDate);
//            if (joiningDate == null) return true;
//
//            Calendar joiningCal = Calendar.getInstance();
//            joiningCal.setTime(joiningDate);
//
//            int selectedYearMonth = (selectedMonth.get(Calendar.YEAR) * 100) + (selectedMonth.get(Calendar.MONTH) + 1);
//            int joiningYearMonth = (joiningCal.get(Calendar.YEAR) * 100) + (joiningCal.get(Calendar.MONTH) + 1);
//
//            Log.d(TAG, String.format("Tenant %s (%s): Joining=%d, Selected=%d",
//                    tenant.name, tenant.status, joiningYearMonth, selectedYearMonth));
//
//            if (joiningYearMonth > selectedYearMonth) {
//                Log.d(TAG, String.format("Tenant %s not active in %s (joined later in %d)",
//                        tenant.name, formatMonthYear(selectedMonth), joiningYearMonth));
//                return false;
//            }
//
//            // **For Left tenants: Check if they left before the selected month**
//            if ("Left".equalsIgnoreCase(tenant.status) && tenant.leaseEndDate != null && !tenant.leaseEndDate.isEmpty()) {
//                Date leavingDate = sdf.parse(tenant.leaseEndDate);
//                if (leavingDate != null) {
//                    Calendar leavingCal = Calendar.getInstance();
//                    leavingCal.setTime(leavingDate);
//
//                    int leavingYearMonth = (leavingCal.get(Calendar.YEAR) * 100) + (leavingCal.get(Calendar.MONTH) + 1);
//
//                    if (leavingYearMonth < selectedYearMonth) {
//                        Log.d(TAG, String.format("Tenant %s not active in %s (left in %d)",
//                                tenant.name, formatMonthYear(selectedMonth), leavingYearMonth));
//                        return false;
//                    }
//                }
//            }
//
//            Log.d(TAG, String.format("✅ Tenant %s (%s) is active in %s",
//                    tenant.name, tenant.status, formatMonthYear(selectedMonth)));
//            return true;
//
//        } catch (ParseException e) {
//            Log.e(TAG, "Error parsing tenant dates for " + tenant.name + ": " + e.getMessage());
//            return true;
//        }
//    }
//
//    private void updateMonthlyStats() {
//        String monthlyStatsText = String.format(Locale.getDefault(),
//                "📊 Month: %s | 👥 Active Tenants: %d",
//                formatMonthYear(selectedMonth), allTenants.size());
//
//        Log.d(TAG, monthlyStatsText);
//    }
//
//    private TenantProfile createTenantProfile(Tenant tenant, DataSnapshot paymentSnapshot) {
//        TenantProfile profile = new TenantProfile(tenant);
//
//        DataSnapshot monthlyPayment = paymentSnapshot.child(tenant.mobile).child(selectedMonth);
//
//        if (monthlyPayment.exists() && monthlyPayment.child("paymentRecord").exists()) {
//            RentPaymentRecord record = monthlyPayment.child("paymentRecord").getValue(RentPaymentRecord.class);
//
//            if (record != null) {
//                profile.setPaymentStatus(record.getPaymentType());
//                profile.setAmountPaid(record.getAmountPaid());
//                profile.setRemainingAmount(record.getRemainingAmount());
//                profile.setPaidForCurrentMonth(record.getAmountPaid() > 0);
//
//                List<PaymentTransaction> transactions = loadTransactionHistory(monthlyPayment);
//                profile.setTransactionHistory(transactions);
//            }
//        } else {
//            profile.setPaidForCurrentMonth(false);
//            profile.setPaymentStatus("PENDING");
//            profile.setAmountPaid(0);
//            profile.setRemainingAmount(tenant.rentAmount);
//        }
//
//        return profile;
//    }
//
//    private List<PaymentTransaction> loadTransactionHistory(DataSnapshot monthPayment) {
//        List<PaymentTransaction> transactions = new ArrayList<>();
//
//        if (monthPayment.child("transactions").exists()) {
//            for (DataSnapshot transactionSnap : monthPayment.child("transactions").getChildren()) {
//                PaymentTransaction transaction = transactionSnap.getValue(PaymentTransaction.class);
//                if (transaction != null) {
//                    transactions.add(transaction);
//                }
//            }
//        }
//
//        return transactions;
//    }
//
//    private void updateCollectionSummary(double totalExpected, double totalCollected, double totalPending,
//                                         int fullyPaidCount, int partialPaidCount, int unpaidCount) {
//        String summaryText = String.format(Locale.getDefault(),
//                "💰 Expected: ₹%.0f | ✅ Collected: ₹%.0f | ⏳ Pending: ₹%.0f\n" +
//                        "📊 Full: %d | ⚠️ Partial: %d | ❌ Unpaid: %d",
//                totalExpected, totalCollected, totalPending,
//                fullyPaidCount, partialPaidCount, unpaidCount);
//
//        tvCollectionSummary.setText(summaryText);
//    }
//
//    private void updateTabTitles() {
//        TabLayout.Tab pendingTab = tabLayout.getTabAt(TAB_PENDING);
//        TabLayout.Tab collectedTab = tabLayout.getTabAt(TAB_COLLECTED);
//
//        if (pendingTab != null) {
//            pendingTab.setText("Pending (" + pendingTenants.size() + ")");
//        }
//        if (collectedTab != null) {
//            collectedTab.setText("Collected (" + collectedTenants.size() + ")");
//        }
//    }
//
//    private void filterListByTab(int position) {
//        List<TenantProfile> listToShow = (position == TAB_PENDING) ? pendingTenants : collectedTenants;
//
//        Log.d(TAG, "Filtering tab " + position + ", showing " + listToShow.size() + " tenants");
//
//        if (adapter == null) {
//            adapter = new RentCollectionAdapter(this, listToShow, this);
//            rvTenants.setAdapter(adapter);
//        } else {
//            adapter.updateList(listToShow);
//        }
//
//        updateEmptyState(position, listToShow.isEmpty());
//    }
//
//    private void updateEmptyState(int tabPosition, boolean isEmpty) {
//        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
//
//        if (isEmpty) {
//            if (tabPosition == TAB_PENDING) {
//                tvEmptyState.setText("🎉 All rents collected for " + formatMonthYear(selectedMonth) + "!");
//            } else {
//                tvEmptyState.setText("📝 No rent collected for " + formatMonthYear(selectedMonth) + " yet.");
//            }
//        }
//    }
//
//    private void showMonthSelectorDialog() {
//        Calendar calendar = parseMonthYear(selectedMonth);
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
//                (view, year, month, dayOfMonth) -> {
//                    Calendar selectedCal = Calendar.getInstance();
//                    selectedCal.set(year, month, 1);
//
//                    if (selectedCal.before(earliestTenantCalendar)) {
//                        showError("Cannot select month before " + formatMonthYear(formatCalendarToMonthYear(earliestTenantCalendar)));
//                        return;
//                    }
//                    if (selectedCal.after(latestAllowedCalendar)) {
//                        showError("Cannot select future months");
//                        return;
//                    }
//
//                    selectedMonth = String.format(Locale.getDefault(), "%02d-%d", month + 1, year);
//                    updateMonthDisplay();
//                    updateNavigationButtons();
//                    loadMonthlyData();
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                1);
//
//        datePickerDialog.setTitle("Select Month for Rent Collection");
//        datePickerDialog.getDatePicker().setMinDate(earliestTenantCalendar.getTimeInMillis());
//        datePickerDialog.getDatePicker().setMaxDate(latestAllowedCalendar.getTimeInMillis());
//        datePickerDialog.show();
//    }
//
//    private void updateMonthDisplay() {
//        tvMonthSelector.setText("📅 " + formatMonthYear(selectedMonth));
//    }
//
//    private Calendar parseMonthYear(String monthYear) {
//        try {
//            SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
//            Date date = sdf.parse(monthYear);
//            Calendar cal = Calendar.getInstance();
//            if (date != null) {
//                cal.setTime(date);
//            }
//            return cal;
//        } catch (ParseException e) {
//            Log.e(TAG, "Error parsing month year: " + e.getMessage());
//            return Calendar.getInstance();
//        }
//    }
//
//    private String formatCalendarToMonthYear(Calendar calendar) {
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
//        return sdf.format(calendar.getTime());
//    }
//
//    @Override
//    public void onCollectClick(TenantProfile tenantProfile) {
//        showPaymentDialog(tenantProfile);
//    }
//
//    private void showPaymentDialog(TenantProfile profile) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_payment, null);
//        builder.setView(dialogView);
//
//        AlertDialog dialog = builder.create();
//        initializePaymentDialog(dialogView, profile, dialog);
//        dialog.show();
//    }
//
//    private void initializePaymentDialog(View dialogView, TenantProfile profile, AlertDialog dialog) {
//        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
//        TextView tvMonthlyRent = dialogView.findViewById(R.id.tvMonthlyRent);
//        TextView tvAlreadyPaid = dialogView.findViewById(R.id.tvAlreadyPaid);
//        TextView tvRemainingAmount = dialogView.findViewById(R.id.tvRemainingAmount);
//        LinearLayout layoutPaymentDetails = dialogView.findViewById(R.id.layoutPaymentDetails);
//
//        EditText etAmount = dialogView.findViewById(R.id.etAmount);
//        EditText etPaymentDate = dialogView.findViewById(R.id.etPaymentDate);
//        EditText etNotes = dialogView.findViewById(R.id.etNotes);
//
//        tvDialogTitle.setText("Collect from " + profile.getTenant().name +
//                " - " + formatMonthYear(selectedMonth));
//        tvMonthlyRent.setText("₹" + formatAmount(profile.getTenant().rentAmount));
//
//        if (profile.getAmountPaid() > 0) {
//            layoutPaymentDetails.setVisibility(View.VISIBLE);
//            tvAlreadyPaid.setText("₹" + formatAmount(profile.getAmountPaid()));
//            tvRemainingAmount.setText("₹" + formatAmount(profile.getRemainingAmount()));
//            etAmount.setText(formatAmount(profile.getRemainingAmount()));
//        } else {
//            layoutPaymentDetails.setVisibility(View.GONE);
//            etAmount.setText(formatAmount(profile.getTenant().rentAmount));
//        }
//
//        etPaymentDate.setText(getCurrentDate());
//        etPaymentDate.setOnClickListener(v -> showDatePickerDialog(etPaymentDate));
//
//        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
//
//        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
//            processPaymentInput(profile, etAmount, etPaymentDate, etNotes);
//            dialog.dismiss();
//        });
//    }
//
//    private void processPaymentInput(TenantProfile profile, EditText etAmount,
//                                     EditText etPaymentDate, EditText etNotes) {
//
//        String amountStr = etAmount.getText().toString().trim();
//        String dateStr = etPaymentDate.getText().toString().trim();
//        String notes = etNotes.getText().toString().trim();
//
//        if (!validatePaymentInput(amountStr, dateStr)) return;
//
//        try {
//            double amountPaid = Double.parseDouble(amountStr);
//            if (amountPaid <= 0) {
//                showError("Amount must be greater than 0");
//                return;
//            }
//
//            double maxAllowed = profile.getRemainingAmount();
//            if (amountPaid > maxAllowed) {
//                showError("Amount cannot exceed remaining amount: ₹" + formatAmount(maxAllowed));
//                return;
//            }
//
//            savePaymentToFirebase(profile, amountPaid, dateStr, notes);
//
//        } catch (NumberFormatException e) {
//            showError("Invalid amount entered");
//        }
//    }
//
//    private boolean validatePaymentInput(String amount, String date) {
//        if (amount.isEmpty()) {
//            showError("Please enter amount");
//            return false;
//        }
//        if (date.isEmpty()) {
//            showError("Please select payment date");
//            return false;
//        }
//        return true;
//    }
//
//    private void savePaymentToFirebase(TenantProfile profile, double amount, String date, String notes) {
//        String tenantMobile = profile.getTenant().mobile;
//        double monthlyRent = profile.getTenant().rentAmount;
//        String transactionId = generateTransactionId(tenantMobile);
//
//        DatabaseReference monthlyPaymentRef = paymentsRef.child(tenantMobile).child(selectedMonth);
//
//        monthlyPaymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                processAndSavePayment(snapshot, profile, amount, date, notes,
//                        monthlyRent, transactionId, monthlyPaymentRef);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                showError("Failed to process payment: " + error.getMessage());
//            }
//        });
//    }
//
//    private void processAndSavePayment(DataSnapshot snapshot, TenantProfile profile, double amount,
//                                       String date, String notes, double monthlyRent,
//                                       String transactionId, DatabaseReference monthlyPaymentRef) {
//
//        double existingAmount = profile.getAmountPaid();
//        double totalAmount = existingAmount + amount;
//
//        String finalPaymentStatus = (totalAmount >= monthlyRent) ? "FULL" : "PARTIAL";
//
//        RentPaymentRecord record = new RentPaymentRecord(
//                profile.getTenant().mobile + "_" + selectedMonth,
//                totalAmount,
//                date,
//                adminId,
//                "Latest: " + notes,
//                finalPaymentStatus,
//                monthlyRent,
//                selectedMonth,
//                profile.getTenant().mobile
//        );
//
//        PaymentTransaction transaction = new PaymentTransaction(
//                transactionId, amount, date, notes, "RENT", adminId
//        );
//
//        monthlyPaymentRef.child("paymentRecord").setValue(record);
//        monthlyPaymentRef.child("transactions").child(transactionId).setValue(transaction)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            showPaymentSuccess(finalPaymentStatus, monthlyRent, totalAmount);
//                        } else {
//                            String errorMessage = task.getException() != null ?
//                                    task.getException().getMessage() : "Unknown error";
//                            showError("Failed to save payment: " + errorMessage);
//                        }
//                    }
//                });
//    }
//
//    private void showPaymentSuccess(String paymentStatus, double monthlyRent, double totalPaid) {
//        String message;
//        if ("PARTIAL".equals(paymentStatus)) {
//            double remaining = monthlyRent - totalPaid;
//            message = "✅ Partial payment recorded!\n" +
//                    "💰 Paid: ₹" + formatAmount(totalPaid) + "\n" +
//                    "⏳ Remaining: ₹" + formatAmount(remaining);
//        } else {
//            message = "🎉 Full payment completed for " + formatMonthYear(selectedMonth) + "!\n" +
//                    "💰 Amount: ₹" + formatAmount(totalPaid);
//        }
//
//        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//    }
//
//    private void clearAllLists() {
//        allTenants.clear();
//        pendingTenants.clear();
//        collectedTenants.clear();
//    }
//
//    private String getCurrentMonthYear() {
//        return new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
//    }
//
//    private String getCurrentDate() {
//        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
//    }
//
//    private String formatMonthYear(String monthYear) {
//        try {
//            SimpleDateFormat inputFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
//            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
//            return outputFormat.format(inputFormat.parse(monthYear));
//        } catch (ParseException e) {
//            return monthYear;
//        }
//    }
//
//    private String formatMonthYear(Calendar calendar) {
//        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
//        return outputFormat.format(calendar.getTime());
//    }
//
//    private String formatAmount(double amount) {
//        return String.format("%.0f", amount);
//    }
//
//    private String generateTransactionId(String tenantMobile) {
//        return tenantMobile + "_" + System.currentTimeMillis();
//    }
//
//    private void showError(String message) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//    }
//
//    private void showDatePickerDialog(EditText editText) {
//        Calendar cal = Calendar.getInstance();
//        new DatePickerDialog(this,
//                (view, year, month, dayOfMonth) -> {
//                    Calendar selectedDate = Calendar.getInstance();
//                    selectedDate.set(year, month, dayOfMonth);
//                    editText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                            .format(selectedDate.getTime()));
//                },
//                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.search_menu, menu);
//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//        searchView.setQueryHint("Search by name or mobile...");
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) { return false; }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (adapter != null) {
//                    adapter.getFilter().filter(newText);
//                }
//                return true;
//            }
//        });
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == android.R.id.home) {
//            finish();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (tenantsRef != null && tenantsListener != null) {
//            tenantsRef.removeEventListener(tenantsListener);
//        }
//        if (paymentsRef != null && paymentsListener != null) {
//            paymentsRef.removeEventListener(paymentsListener);
//        }
//    }
//}



package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CollectRentActivity extends AppCompatActivity implements RentCollectionAdapter.OnCollectButtonClickListener {

    private static final String TAG = "CollectRentActivity";

    // UI Components - ALL ORIGINAL IDs PRESERVED
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTenants;
    private TextView tvEmptyState, tvMonthSelector, tvCollectionSummary;
    private Button btnPreviousMonth, btnNextMonth;
    private LinearLayout layoutMonthNavigation, layoutEmptyState;

    // Professional Separate Display Components
    private TextView tvExpectedAmount, tvCollectedAmount, tvPendingAmount;

    // **UPDATED: Data Lists with new logic**
    private RentCollectionAdapter adapter;
    private List<TenantProfile> allTenants = new ArrayList<>();
    private List<TenantProfile> pendingTenants = new ArrayList<>();  // Unpaid + Partial
    private List<TenantProfile> collectedTenants = new ArrayList<>(); // Only Fully Paid

    // Firebase References
    private DatabaseReference tenantsRef, paymentsRef;
    private String adminId, selectedMonth;
    private ValueEventListener tenantsListener, paymentsListener;

    // Month Navigation
    private Calendar currentCalendar;
    private Calendar earliestTenantCalendar;
    private Calendar latestAllowedCalendar;

    // Tab Constants
    private static final int TAB_PENDING = 0;
    private static final int TAB_COLLECTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_rent);

        adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("mobile", "default_admin");

        initializeCalendars();
        selectedMonth = getCurrentMonthYear();

        setupUI();
        setupFirebase();
        loadEarliestTenantDate();
    }

    private void initializeCalendars() {
        currentCalendar = Calendar.getInstance();
        earliestTenantCalendar = Calendar.getInstance();
        latestAllowedCalendar = Calendar.getInstance();
        latestAllowedCalendar.add(Calendar.MONTH, 0);
    }

    private void setupUI() {
        // Initialize ALL original IDs + new professional components
        toolbar = findViewById(R.id.toolbarCollectRent);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Rent Collection");
        }

        // Original components
        tabLayout = findViewById(R.id.tabLayout);
        rvTenants = findViewById(R.id.rvTenants);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        tvMonthSelector = findViewById(R.id.tvMonthSelector);
        tvCollectionSummary = findViewById(R.id.tvCollectionSummary);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        layoutMonthNavigation = findViewById(R.id.layoutMonthNavigation);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        // Professional separate amount displays
        tvExpectedAmount = findViewById(R.id.tvExpectedAmount);
        tvCollectedAmount = findViewById(R.id.tvCollectedAmount);
        tvPendingAmount = findViewById(R.id.tvPendingAmount);

        rvTenants.setLayoutManager(new LinearLayoutManager(this));

        setupMonthNavigation();
        setupTabs();
    }

    private void setupMonthNavigation() {
        updateMonthDisplay();
        updateNavigationButtons();

        tvMonthSelector.setOnClickListener(v -> showMonthSelectorDialog());

        btnPreviousMonth.setOnClickListener(v -> {
            Calendar cal = parseMonthYear(selectedMonth);
            cal.add(Calendar.MONTH, -1);

            if (!cal.before(earliestTenantCalendar)) {
                selectedMonth = formatCalendarToMonthYear(cal);
                updateMonthDisplay();
                updateNavigationButtons();
                loadMonthlyData();
            }
        });

        btnNextMonth.setOnClickListener(v -> {
            Calendar cal = parseMonthYear(selectedMonth);
            cal.add(Calendar.MONTH, 1);

            if (!cal.after(latestAllowedCalendar)) {
                selectedMonth = formatCalendarToMonthYear(cal);
                updateMonthDisplay();
                updateNavigationButtons();
                loadMonthlyData();
            }
        });
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Collected"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterListByTab(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateNavigationButtons() {
        Calendar selectedCal = parseMonthYear(selectedMonth);

        Calendar prevMonth = (Calendar) selectedCal.clone();
        prevMonth.add(Calendar.MONTH, -1);
        btnPreviousMonth.setEnabled(!prevMonth.before(earliestTenantCalendar));

        Calendar nextMonth = (Calendar) selectedCal.clone();
        nextMonth.add(Calendar.MONTH, 1);
        btnNextMonth.setEnabled(!nextMonth.after(latestAllowedCalendar));

        btnPreviousMonth.setAlpha(btnPreviousMonth.isEnabled() ? 1.0f : 0.5f);
        btnNextMonth.setAlpha(btnNextMonth.isEnabled() ? 1.0f : 0.5f);
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tenantsRef = database.getReference("users").child(adminId).child("tenants");
        paymentsRef = database.getReference("users").child(adminId).child("rentPayments");
    }

    private void loadEarliestTenantDate() {
        tenantsRef.orderByChild("leaseStartDate").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String earliestDate = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Tenant tenant = ds.getValue(Tenant.class);
                    if (tenant != null && tenant.leaseStartDate != null && !tenant.leaseStartDate.isEmpty()) {
                        earliestDate = tenant.leaseStartDate;
                        break;
                    }
                }

                if (earliestDate != null) {
                    setEarliestTenantDate(earliestDate);
                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.MONTH, Calendar.JANUARY);
                    earliestTenantCalendar = cal;
                }

                updateNavigationButtons();
                loadMonthlyData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load earliest tenant date: " + error.getMessage());
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                earliestTenantCalendar = cal;
                updateNavigationButtons();
                loadMonthlyData();
            }
        });
    }

    private void setEarliestTenantDate(String joiningDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(joiningDate);
            if (date != null) {
                earliestTenantCalendar.setTime(date);
                earliestTenantCalendar.set(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing earliest tenant date: " + e.getMessage());
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            earliestTenantCalendar = cal;
        }
    }

    private void loadMonthlyData() {
        Log.d(TAG, "Loading data for month: " + selectedMonth);
        clearAllLists();
        loadPaymentDataForMonth();
    }

    private void loadPaymentDataForMonth() {
        if (paymentsListener != null) {
            paymentsRef.removeEventListener(paymentsListener);
        }

        paymentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
                Log.d(TAG, "Payment data loaded, now loading tenants");
                loadTenantsData(paymentSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load payment data: " + error.getMessage());
                showError("Failed to load payment data: " + error.getMessage());
            }
        };

        paymentsRef.addValueEventListener(paymentsListener);
    }

    private void loadTenantsData(DataSnapshot paymentSnapshot) {
        if (tenantsListener != null) {
            tenantsRef.removeEventListener(tenantsListener);
        }

        tenantsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                Log.d(TAG, "Tenants data loaded, processing...");
                processTenantsAndPayments(tenantSnapshot, paymentSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load tenants: " + error.getMessage());
                showError("Failed to load tenants: " + error.getMessage());
            }
        };

        tenantsRef.orderByChild("name").addValueEventListener(tenantsListener);
    }

    /**
     * **UPDATED: New logic for tab segregation**
     * - Collected Tab: Only FULLY PAID tenants
     * - Pending Tab: UNPAID + PARTIALLY PAID tenants
     */
    private void processTenantsAndPayments(DataSnapshot tenantSnapshot, DataSnapshot paymentSnapshot) {
        clearAllLists();

        final double[] totalExpected = {0.0};
        final double[] totalCollected = {0.0};
        final double[] totalPending = {0.0};
        final int[] fullyPaidCount = {0};
        final int[] partialPaidCount = {0};
        final int[] unpaidCount = {0};

        Calendar selectedMonthCal = parseMonthYear(selectedMonth);

        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
            Tenant tenant = ds.getValue(Tenant.class);
            if (tenant != null) {
                boolean isActive = "Active".equalsIgnoreCase(tenant.status);
                boolean isLeft = "Left".equalsIgnoreCase(tenant.status);

                if (isActive || isLeft) {
                    if (!shouldTenantAppearInMonth(tenant, selectedMonthCal)) {
                        Log.d(TAG, String.format("Tenant %s not shown in %s (joined later or left earlier)",
                                tenant.name, formatMonthYear(selectedMonth)));
                        continue;
                    }

                    TenantProfile profile = createTenantProfile(tenant, paymentSnapshot);
                    profile.setTenantStatus(tenant.status);

                    totalExpected[0] += tenant.rentAmount;

                    // **UPDATED: New segregation logic**
                    if ("FULL".equals(profile.getPaymentStatus())) {
                        // ✅ FULLY PAID → Goes to COLLECTED tab
                        fullyPaidCount[0]++;
                        totalCollected[0] += profile.getAmountPaid();
                        collectedTenants.add(profile);

                        Log.d(TAG, String.format("✅ FULL PAYMENT: %s → Collected Tab", tenant.name));

                    } else if ("PARTIAL".equals(profile.getPaymentStatus())) {
                        // ⚠️ PARTIALLY PAID → Goes to PENDING tab
                        partialPaidCount[0]++;
                        totalCollected[0] += profile.getAmountPaid();
                        totalPending[0] += profile.getRemainingAmount();
                        pendingTenants.add(profile); // 🔄 MOVED TO PENDING

                        Log.d(TAG, String.format("⚠️ PARTIAL PAYMENT: %s → Pending Tab (₹%.0f paid, ₹%.0f pending)",
                                tenant.name, profile.getAmountPaid(), profile.getRemainingAmount()));

                    } else {
                        // ❌ UNPAID → Goes to PENDING tab
                        unpaidCount[0]++;
                        totalPending[0] += tenant.rentAmount;
                        pendingTenants.add(profile);

                        Log.d(TAG, String.format("❌ UNPAID: %s → Pending Tab", tenant.name));
                    }

                    allTenants.add(profile);

                    Log.d(TAG, String.format("Added tenant %s (%s) for month %s",
                            tenant.name, tenant.status, formatMonthYear(selectedMonth)));
                }
            }
        }

        runOnUiThread(() -> {
            updateCollectionSummary(totalExpected[0], totalCollected[0], totalPending[0],
                    fullyPaidCount[0], partialPaidCount[0], unpaidCount[0]);
            updateProfessionalAmountDisplays(totalExpected[0], totalCollected[0], totalPending[0]);
            updateTabTitles();
            filterListByTab(tabLayout.getSelectedTabPosition());
            updateMonthlyStats();
        });

        Log.d(TAG, String.format("📊 UPDATED SEGREGATION for %s:\n" +
                        "   🔵 Collected Tab (Fully Paid): %d tenants\n" +
                        "   🟡 Pending Tab (Unpaid + Partial): %d tenants\n" +
                        "   📈 Total Tenants: %d",
                formatMonthYear(selectedMonth), collectedTenants.size(), pendingTenants.size(), allTenants.size()));
    }

    /**
     * Determines if a tenant should appear in the selected month
     * Handles both Active and Left tenants based on their lease dates
     */
    private boolean shouldTenantAppearInMonth(Tenant tenant, Calendar selectedMonth) {
        if (tenant.leaseStartDate == null || tenant.leaseStartDate.isEmpty()) {
            Log.d(TAG, "No joining date for " + tenant.name + ", including by default");
            return true;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date joiningDate = sdf.parse(tenant.leaseStartDate);
            if (joiningDate == null) return true;

            Calendar joiningCal = Calendar.getInstance();
            joiningCal.setTime(joiningDate);

            int selectedYearMonth = (selectedMonth.get(Calendar.YEAR) * 100) + (selectedMonth.get(Calendar.MONTH) + 1);
            int joiningYearMonth = (joiningCal.get(Calendar.YEAR) * 100) + (joiningCal.get(Calendar.MONTH) + 1);

            if (joiningYearMonth > selectedYearMonth) {
                return false;
            }

            // For Left tenants: Check if they left before the selected month
            if ("Left".equalsIgnoreCase(tenant.status) && tenant.leaseEndDate != null && !tenant.leaseEndDate.isEmpty()) {
                Date leavingDate = sdf.parse(tenant.leaseEndDate);
                if (leavingDate != null) {
                    Calendar leavingCal = Calendar.getInstance();
                    leavingCal.setTime(leavingDate);

                    int leavingYearMonth = (leavingCal.get(Calendar.YEAR) * 100) + (leavingCal.get(Calendar.MONTH) + 1);

                    if (leavingYearMonth < selectedYearMonth) {
                        return false;
                    }
                }
            }

            return true;

        } catch (ParseException e) {
            Log.e(TAG, "Error parsing tenant dates for " + tenant.name + ": " + e.getMessage());
            return true;
        }
    }

    private void updateMonthlyStats() {
        String monthlyStatsText = String.format(Locale.getDefault(),
                "📊 Month: %s | 👥 Active Tenants: %d",
                formatMonthYear(selectedMonth), allTenants.size());

        Log.d(TAG, monthlyStatsText);
    }

    private TenantProfile createTenantProfile(Tenant tenant, DataSnapshot paymentSnapshot) {
        TenantProfile profile = new TenantProfile(tenant);

        DataSnapshot monthlyPayment = paymentSnapshot.child(tenant.mobile).child(selectedMonth);

        if (monthlyPayment.exists() && monthlyPayment.child("paymentRecord").exists()) {
            RentPaymentRecord record = monthlyPayment.child("paymentRecord").getValue(RentPaymentRecord.class);

            if (record != null) {
                profile.setPaymentStatus(record.getPaymentType());
                profile.setAmountPaid(record.getAmountPaid());
                profile.setRemainingAmount(record.getRemainingAmount());
                profile.setPaidForCurrentMonth(record.getAmountPaid() > 0);

                List<PaymentTransaction> transactions = loadTransactionHistory(monthlyPayment);
                profile.setTransactionHistory(transactions);
            }
        } else {
            profile.setPaidForCurrentMonth(false);
            profile.setPaymentStatus("PENDING");
            profile.setAmountPaid(0);
            profile.setRemainingAmount(tenant.rentAmount);
        }

        return profile;
    }

    private List<PaymentTransaction> loadTransactionHistory(DataSnapshot monthPayment) {
        List<PaymentTransaction> transactions = new ArrayList<>();

        if (monthPayment.child("transactions").exists()) {
            for (DataSnapshot transactionSnap : monthPayment.child("transactions").getChildren()) {
                PaymentTransaction transaction = transactionSnap.getValue(PaymentTransaction.class);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    /**
     * ORIGINAL METHOD: Updates the original text-based collection summary
     */
    private void updateCollectionSummary(double totalExpected, double totalCollected, double totalPending,
                                         int fullyPaidCount, int partialPaidCount, int unpaidCount) {
        String summaryText = String.format(Locale.getDefault(),
                "💰 Expected: ₹%.0f | ✅ Collected: ₹%.0f | ⏳ Pending: ₹%.0f\n" +
                        "📊 Full: %d | ⚠️ Partial: %d | ❌ Unpaid: %d",
                totalExpected, totalCollected, totalPending,
                fullyPaidCount, partialPaidCount, unpaidCount);

        tvCollectionSummary.setText(summaryText);
    }

    /**
     * NEW METHOD: Updates professional separate amount displays
     */
    private void updateProfessionalAmountDisplays(double totalExpected, double totalCollected, double totalPending) {
        if (tvExpectedAmount != null) {
            tvExpectedAmount.setText(String.format(Locale.getDefault(), "₹%.0f", totalExpected));
        }

        if (tvCollectedAmount != null) {
            tvCollectedAmount.setText(String.format(Locale.getDefault(), "₹%.0f", totalCollected));
        }

        if (tvPendingAmount != null) {
            tvPendingAmount.setText(String.format(Locale.getDefault(), "₹%.0f", totalPending));
        }

        Log.d(TAG, String.format("Professional Display Updated - Expected: ₹%.0f, Collected: ₹%.0f, Pending: ₹%.0f",
                totalExpected, totalCollected, totalPending));
    }

    /**
     * **UPDATED: Tab titles reflect new logic**
     */
    private void updateTabTitles() {
        TabLayout.Tab pendingTab = tabLayout.getTabAt(TAB_PENDING);
        TabLayout.Tab collectedTab = tabLayout.getTabAt(TAB_COLLECTED);

        if (pendingTab != null) {
            // Pending = Unpaid + Partial
            pendingTab.setText("Pending (" + pendingTenants.size() + ")");
        }
        if (collectedTab != null) {
            // Collected = Only Fully Paid
            collectedTab.setText("Collected (" + collectedTenants.size() + ")");
        }
    }

    private void filterListByTab(int position) {
        List<TenantProfile> listToShow = (position == TAB_PENDING) ? pendingTenants : collectedTenants;

        Log.d(TAG, "Filtering tab " + position + ", showing " + listToShow.size() + " tenants");

        if (adapter == null) {
            adapter = new RentCollectionAdapter(this, listToShow, this);
            rvTenants.setAdapter(adapter);
        } else {
            adapter.updateList(listToShow);
        }

        updateEmptyState(position, listToShow.isEmpty());
    }

    /**
     * **UPDATED: Empty state messages reflect new logic**
     */
    private void updateEmptyState(int tabPosition, boolean isEmpty) {
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }

        if (isEmpty) {
            if (tabPosition == TAB_PENDING) {
                // Pending tab is empty = No unpaid/partial payments
                tvEmptyState.setText("🎉 All rents fully collected for " + formatMonthYear(selectedMonth) + "!");
            } else {
                // Collected tab is empty = No fully paid tenants
                tvEmptyState.setText("📝 No fully paid rent for " + formatMonthYear(selectedMonth) + " yet.");
            }
        }
    }

    private void showMonthSelectorDialog() {
        Calendar calendar = parseMonthYear(selectedMonth);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, month, 1);

                    if (selectedCal.before(earliestTenantCalendar)) {
                        showError("Cannot select month before " + formatMonthYear(formatCalendarToMonthYear(earliestTenantCalendar)));
                        return;
                    }
                    if (selectedCal.after(latestAllowedCalendar)) {
                        showError("Cannot select future months");
                        return;
                    }

                    selectedMonth = String.format(Locale.getDefault(), "%02d-%d", month + 1, year);
                    updateMonthDisplay();
                    updateNavigationButtons();
                    loadMonthlyData();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                1);

        datePickerDialog.setTitle("Select Month for Rent Collection");
        datePickerDialog.getDatePicker().setMinDate(earliestTenantCalendar.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(latestAllowedCalendar.getTimeInMillis());
        datePickerDialog.show();
    }

    private void updateMonthDisplay() {
        tvMonthSelector.setText("📅 " + formatMonthYear(selectedMonth));
    }

    private Calendar parseMonthYear(String monthYear) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(monthYear);
            Calendar cal = Calendar.getInstance();
            if (date != null) {
                cal.setTime(date);
            }
            return cal;
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing month year: " + e.getMessage());
            return Calendar.getInstance();
        }
    }

    private String formatCalendarToMonthYear(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    @Override
    public void onCollectClick(TenantProfile tenantProfile) {
        showPaymentDialog(tenantProfile);
    }

    private void showPaymentDialog(TenantProfile profile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_payment, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        initializePaymentDialog(dialogView, profile, dialog);
        dialog.show();
    }

    private void initializePaymentDialog(View dialogView, TenantProfile profile, AlertDialog dialog) {
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvMonthlyRent = dialogView.findViewById(R.id.tvMonthlyRent);
        TextView tvAlreadyPaid = dialogView.findViewById(R.id.tvAlreadyPaid);
        TextView tvRemainingAmount = dialogView.findViewById(R.id.tvRemainingAmount);
        LinearLayout layoutPaymentDetails = dialogView.findViewById(R.id.layoutPaymentDetails);

        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etPaymentDate = dialogView.findViewById(R.id.etPaymentDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        tvDialogTitle.setText("Collect from " + profile.getTenant().name +
                " - " + formatMonthYear(selectedMonth));
        tvMonthlyRent.setText("₹" + formatAmount(profile.getTenant().rentAmount));

        if (profile.getAmountPaid() > 0) {
            layoutPaymentDetails.setVisibility(View.VISIBLE);
            tvAlreadyPaid.setText("₹" + formatAmount(profile.getAmountPaid()));
            tvRemainingAmount.setText("₹" + formatAmount(profile.getRemainingAmount()));
            etAmount.setText(formatAmount(profile.getRemainingAmount()));
        } else {
            layoutPaymentDetails.setVisibility(View.GONE);
            etAmount.setText(formatAmount(profile.getTenant().rentAmount));
        }

        etPaymentDate.setText(getCurrentDate());
        etPaymentDate.setOnClickListener(v -> showDatePickerDialog(etPaymentDate));

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(v -> {
            processPaymentInput(profile, etAmount, etPaymentDate, etNotes);
            dialog.dismiss();
        });
    }

    private void processPaymentInput(TenantProfile profile, EditText etAmount,
                                     EditText etPaymentDate, EditText etNotes) {

        String amountStr = etAmount.getText().toString().trim();
        String dateStr = etPaymentDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (!validatePaymentInput(amountStr, dateStr)) return;

        try {
            double amountPaid = Double.parseDouble(amountStr);
            if (amountPaid <= 0) {
                showError("Amount must be greater than 0");
                return;
            }

            double maxAllowed = profile.getRemainingAmount();
            if (amountPaid > maxAllowed) {
                showError("Amount cannot exceed remaining amount: ₹" + formatAmount(maxAllowed));
                return;
            }

            savePaymentToFirebase(profile, amountPaid, dateStr, notes);

        } catch (NumberFormatException e) {
            showError("Invalid amount entered");
        }
    }

    private boolean validatePaymentInput(String amount, String date) {
        if (amount.isEmpty()) {
            showError("Please enter amount");
            return false;
        }
        if (date.isEmpty()) {
            showError("Please select payment date");
            return false;
        }
        return true;
    }

    private void savePaymentToFirebase(TenantProfile profile, double amount, String date, String notes) {
        String tenantMobile = profile.getTenant().mobile;
        double monthlyRent = profile.getTenant().rentAmount;
        String transactionId = generateTransactionId(tenantMobile);

        DatabaseReference monthlyPaymentRef = paymentsRef.child(tenantMobile).child(selectedMonth);

        monthlyPaymentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processAndSavePayment(snapshot, profile, amount, date, notes,
                        monthlyRent, transactionId, monthlyPaymentRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to process payment: " + error.getMessage());
            }
        });
    }

    private void processAndSavePayment(DataSnapshot snapshot, TenantProfile profile, double amount,
                                       String date, String notes, double monthlyRent,
                                       String transactionId, DatabaseReference monthlyPaymentRef) {

        double existingAmount = profile.getAmountPaid();
        double totalAmount = existingAmount + amount;

        String finalPaymentStatus = (totalAmount >= monthlyRent) ? "FULL" : "PARTIAL";

        RentPaymentRecord record = new RentPaymentRecord(
                profile.getTenant().mobile + "_" + selectedMonth,
                totalAmount,
                date,
                adminId,
                "Latest: " + notes,
                finalPaymentStatus,
                monthlyRent,
                selectedMonth,
                profile.getTenant().mobile
        );

        PaymentTransaction transaction = new PaymentTransaction(
                transactionId, amount, date, notes, "RENT", adminId
        );

        monthlyPaymentRef.child("paymentRecord").setValue(record);
        monthlyPaymentRef.child("transactions").child(transactionId).setValue(transaction)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showPaymentSuccess(finalPaymentStatus, monthlyRent, totalAmount);
                        } else {
                            String errorMessage = task.getException() != null ?
                                    task.getException().getMessage() : "Unknown error";
                            showError("Failed to save payment: " + errorMessage);
                        }
                    }
                });
    }

    private void showPaymentSuccess(String paymentStatus, double monthlyRent, double totalPaid) {
        String message;
        if ("PARTIAL".equals(paymentStatus)) {
            double remaining = monthlyRent - totalPaid;
            message = "✅ Partial payment recorded!\n" +
                    "💰 Paid: ₹" + formatAmount(totalPaid) + "\n" +
                    "⏳ Remaining: ₹" + formatAmount(remaining) + "\n" +
                    "📍 Status: Will remain in Pending tab";
        } else {
            message = "🎉 Full payment completed for " + formatMonthYear(selectedMonth) + "!\n" +
                    "💰 Amount: ₹" + formatAmount(totalPaid) + "\n" +
                    "📍 Status: Moved to Collected tab";
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void clearAllLists() {
        allTenants.clear();
        pendingTenants.clear();
        collectedTenants.clear();
    }

    private String getCurrentMonthYear() {
        return new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private String formatMonthYear(String monthYear) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("MM-yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(monthYear));
        } catch (ParseException e) {
            return monthYear;
        }
    }

    private String formatMonthYear(Calendar calendar) {
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        return outputFormat.format(calendar.getTime());
    }

    private String formatAmount(double amount) {
        return String.format("%.0f", amount);
    }

    private String generateTransactionId(String tenantMobile) {
        return tenantMobile + "_" + System.currentTimeMillis();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    editText.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(selectedDate.getTime()));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search by name or mobile...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tenantsRef != null && tenantsListener != null) {
            tenantsRef.removeEventListener(tenantsListener);
        }
        if (paymentsRef != null && paymentsListener != null) {
            paymentsRef.removeEventListener(paymentsListener);
        }
    }
}
