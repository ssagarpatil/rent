package com.ss.rentmangment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ss.rentmangment.RentCollectionAdapter;
import com.ss.rentmangment.RentPaymentRecord;
import com.ss.rentmangment.TenantProfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CollectRentActivity extends AppCompatActivity implements RentCollectionAdapter.OnCollectButtonClickListener {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView rvTenants;
    private TextView tvEmptyState;

    private RentCollectionAdapter adapter;
    private List<TenantProfile> allTenants = new ArrayList<>();
    private List<TenantProfile> pendingTenants = new ArrayList<>();
    private List<TenantProfile> collectedTenants = new ArrayList<>();

    private DatabaseReference tenantsRef, paymentsRef;
    private String adminId;
    private ValueEventListener tenantsListener, paymentsListener;

    private static final int TAB_PENDING = 0;
    private static final int TAB_COLLECTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_rent);

        adminId = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).getString("mobile", "default_admin");

        setupUI();
        setupFirebase();
        loadData();
    }

    private void setupUI() {
        toolbar = findViewById(R.id.toolbarCollectRent);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tabLayout = findViewById(R.id.tabLayout);
        rvTenants = findViewById(R.id.rvTenants);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        rvTenants.setLayoutManager(new LinearLayoutManager(this));

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

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tenantsRef = database.getReference("users").child(adminId).child("tenants");
        paymentsRef = database.getReference("users").child(adminId).child("rentPayments");
    }

    private void loadData() {
        // Use a single listener for payments for efficiency
        paymentsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
                // Now fetch tenants, ensuring we have the latest payment data
                fetchTenants(paymentSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CollectRentActivity.this, "Failed to load payments.", Toast.LENGTH_SHORT).show();
            }
        };
        paymentsRef.addValueEventListener(paymentsListener);
    }

    private void fetchTenants(DataSnapshot paymentSnapshot) {
        tenantsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                processTenantAndPaymentData(tenantSnapshot, paymentSnapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CollectRentActivity.this, "Failed to load tenants.", Toast.LENGTH_SHORT).show();
            }
        };
        tenantsRef.orderByChild("name").addValueEventListener(tenantsListener);
    }

    private void processTenantAndPaymentData(DataSnapshot tenantSnapshot, DataSnapshot paymentSnapshot) {
        allTenants.clear();
        pendingTenants.clear();
        collectedTenants.clear();

        String currentMonthYear = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        for (DataSnapshot ds : tenantSnapshot.getChildren()) {
            Tenant tenant = ds.getValue(Tenant.class);
            if (tenant != null && "Active".equalsIgnoreCase(tenant.status)) {
                TenantProfile profile = new TenantProfile(tenant);

                boolean hasPaid = paymentSnapshot.child(tenant.mobile).hasChild(currentMonthYear);
                profile.setPaidForCurrentMonth(hasPaid);

                if (hasPaid) {
                    collectedTenants.add(profile);
                } else {
                    int pendingMonths = calculatePendingMonths(tenant.leaseStartDate, paymentSnapshot.child(tenant.mobile));
                    profile.setPendingMonths(pendingMonths);
                    pendingTenants.add(profile);
                }
            }
        }

        allTenants.addAll(pendingTenants);
        allTenants.addAll(collectedTenants);

        filterListByTab(tabLayout.getSelectedTabPosition());
    }

    private int calculatePendingMonths(String leaseStartDateStr, DataSnapshot tenantPayments) {
        if (leaseStartDateStr == null || leaseStartDateStr.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Calendar leaseStart = Calendar.getInstance();
            leaseStart.setTime(sdf.parse(leaseStartDateStr));

            Calendar today = Calendar.getInstance();

            int monthsPassed = 0;
            while(leaseStart.before(today)) {
                leaseStart.add(Calendar.MONTH, 1);
                monthsPassed++;
            }

            int paidCount = 0;
            if(tenantPayments.exists()){
                paidCount = (int) tenantPayments.getChildrenCount();
            }

            int pending = monthsPassed - paidCount;
            return Math.max(0, pending);

        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private void filterListByTab(int position) {
        List<TenantProfile> listToShow = (position == TAB_PENDING) ? pendingTenants : collectedTenants;
        if (adapter == null) {
            adapter = new RentCollectionAdapter(this, listToShow, this);
            rvTenants.setAdapter(adapter);
        } else {
            adapter.updateList(listToShow);
        }

        tvEmptyState.setVisibility(listToShow.isEmpty() ? View.VISIBLE : View.GONE);
        if (position == TAB_PENDING) {
            tvEmptyState.setText("All rents collected!");
        } else {
            tvEmptyState.setText("No payments collected yet.");
        }
    }

    @Override
    public void onCollectClick(TenantProfile tenantProfile) {
        showConfirmPaymentDialog(tenantProfile);
    }

    private void showConfirmPaymentDialog(TenantProfile profile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_payment, null);
        builder.setView(dialogView);

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etPaymentDate = dialogView.findViewById(R.id.etPaymentDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        tvDialogTitle.setText("Collect from " + profile.getTenant().name);
        etAmount.setText(String.valueOf((int) profile.getTenant().rentAmount));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etPaymentDate.setText(sdf.format(Calendar.getInstance().getTime()));

        etPaymentDate.setOnClickListener(v -> showDatePickerDialog(etPaymentDate));

        builder.setPositiveButton("Confirm Payment", (dialog, which) -> {
            String amountStr = etAmount.getText().toString();
            String dateStr = etPaymentDate.getText().toString();
            String notes = etNotes.getText().toString().trim();

            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Amount cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            double amountPaid = Double.parseDouble(amountStr);
            savePaymentToFirebase(profile, amountPaid, dateStr, notes);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePickerDialog(final EditText etPaymentDate) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    etPaymentDate.setText(sdf.format(selectedDate.getTime()));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void savePaymentToFirebase(TenantProfile profile, double amount, String date, String notes) {
        String tenantMobile = profile.getTenant().mobile;
        String monthYearKey = new SimpleDateFormat("MM-yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        DatabaseReference paymentNode = paymentsRef.child(tenantMobile).child(monthYearKey);

        String paymentId = tenantMobile + "_" + monthYearKey;
        RentPaymentRecord record = new RentPaymentRecord(paymentId, amount, date, adminId, notes);

        paymentNode.setValue(record).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Payment recorded successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to record payment.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search by Name or Mobile...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

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
        // Important to remove listeners to prevent memory leaks
        if (tenantsRef != null && tenantsListener != null) {
            tenantsRef.removeEventListener(tenantsListener);
        }
        if (paymentsRef != null && paymentsListener != null) {
            paymentsRef.removeEventListener(paymentsListener);
        }
    }
}

