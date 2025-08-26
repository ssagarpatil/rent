package com.ss.rentmangment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class ReportsActivity extends AppCompatActivity {

    private Spinner dateFilterSpinner;
    private TextView tvTotalIncome, tvTotalExpenses, tvNetProfit;
    private PieChart pieChart;
    private BarChart barChart;
    private LinearLayout reportContentLayout;

    private DatabaseReference rootRef;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat monthYearSdf = new SimpleDateFormat("MMM yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        Toolbar toolbar = findViewById(R.id.toolbar_reports);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        dateFilterSpinner = findViewById(R.id.spinnerDateFilter);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvNetProfit = findViewById(R.id.tvNetProfit);
        pieChart = findViewById(R.id.pieChartExpenses);
        barChart = findViewById(R.id.barChartComparison);
        reportContentLayout = findViewById(R.id.financial_reports_content_layout);

        String adminId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("mobile", "");
        rootRef = FirebaseDatabase.getInstance().getReference("users").child(adminId);

        setupDateFilterSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reports_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_export_pdf) {
            showExportOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExportOptionsDialog() {
        String[] options = {"Financial Summary", "All Tenants List", "Room & Occupancy Report"};
        new AlertDialog.Builder(this)
                .setTitle("Select Report to Export")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: generatePdfFromView("Financial_Summary"); break;
                        case 1: generateAllTenantsReport(); break;
                        case 2: generateRoomOccupancyReport(); break;
                    }
                })
                .show();
    }


    private void generateAllTenantsReport() {
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String[]> data = new ArrayList<>();
                data.add(new String[]{"Room No", "Room Type", "Tenant Type", "Name", "Mobile"});

                // Collect rooms type map
                Map<String, String> roomTypeMap = new HashMap<>();
                DataSnapshot roomsSnap = snapshot.child("rooms");
                for (DataSnapshot roomSnap : roomsSnap.getChildren()) {
                    roomTypeMap.put(roomSnap.getKey(), roomSnap.child("type").getValue(String.class));
                }

                // Group tenants by room and tenantType, filter status != "Left"
                Map<String, Map<String, List<DataSnapshot>>> grouped = new TreeMap<>();
                DataSnapshot tenantsSnap = snapshot.child("tenants");
                for (DataSnapshot tenantDs : tenantsSnap.getChildren()) {
                    String status = tenantDs.child("status").getValue(String.class);
                    if ("Left".equalsIgnoreCase(status)) continue; // skip left tenants

                    String roomNo = tenantDs.child("roomNumber").getValue(String.class);
                    String tenantType = tenantDs.child("tenantType").getValue(String.class);
                    if (roomNo == null || tenantType == null) continue;

                    grouped.putIfAbsent(roomNo, new HashMap<>());
                    Map<String, List<DataSnapshot>> tenantTypeMap = grouped.get(roomNo);

                    tenantTypeMap.putIfAbsent(tenantType, new ArrayList<>());
                    tenantTypeMap.get(tenantType).add(tenantDs);
                }

                // Prepare data rows grouped by room and tenant type (family then student)
                for (String roomNo : grouped.keySet()) {
                    String roomType = roomTypeMap.getOrDefault(roomNo, "—");
                    Map<String, List<DataSnapshot>> tenantTypeMap = grouped.get(roomNo);

                    if (tenantTypeMap.containsKey("Family")) {
                        for (DataSnapshot tenantDs : tenantTypeMap.get("Family")) {
                            String name = tenantDs.child("name").getValue(String.class);
                            String mobile = tenantDs.child("mobile").getValue(String.class);
                            data.add(new String[]{
                                    roomNo, roomType, "Family",
                                    name != null ? name : "—",
                                    mobile != null ? mobile : "—"
                            });
                        }
                    }
                    if (tenantTypeMap.containsKey("Student")) {
                        for (DataSnapshot tenantDs : tenantTypeMap.get("Student")) {
                            String name = tenantDs.child("name").getValue(String.class);
                            String mobile = tenantDs.child("mobile").getValue(String.class);
                            data.add(new String[]{
                                    roomNo, roomType, "Student",
                                    name != null ? name : "—",
                                    mobile != null ? mobile : "—"
                            });
                        }
                    }
                }

                createAndOpenPdf("All_Tenants_Report", data);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }




    private void generateRoomOccupancyReport() {
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String[]> bookedRoomsData = new ArrayList<>();
                List<String[]> availableRoomsData = new ArrayList<>();
                String[] headers = new String[]{"Room No", "Room Type", "Usage", "Status", "Occupants"};
                bookedRoomsData.add(headers);
                availableRoomsData.add(headers);

                DataSnapshot roomsSnapshot = snapshot.child("rooms");
                DataSnapshot tenantsSnapshot = snapshot.child("tenants");

                Map<String, List<String>> studentNamesByRoom = new HashMap<>();
                Map<String, String> familyNameByRoom = new HashMap<>();

                // Collect active tenants grouped by room
                for (DataSnapshot tenantDs : tenantsSnapshot.getChildren()) {
                    String status = tenantDs.child("status").getValue(String.class);
                    if (status != null && status.equalsIgnoreCase("Left")) continue; // Skip inactive tenants

                    String roomNo = tenantDs.child("roomNumber").getValue(String.class);
                    String tenantType = tenantDs.child("tenantType").getValue(String.class);
                    String name = tenantDs.child("name").getValue(String.class);

                    if (roomNo == null || tenantType == null) continue;

                    if (tenantType.equalsIgnoreCase("Student")) {
                        studentNamesByRoom.putIfAbsent(roomNo, new ArrayList<>());
                        if (name != null) studentNamesByRoom.get(roomNo).add(name);
                    } else if (tenantType.equalsIgnoreCase("Family")) {
                        // For family, only one tenant assumed
                        familyNameByRoom.put(roomNo, name != null ? name : "—");
                    }
                }

                // Process rooms: separate booked and available
                for (DataSnapshot roomDs : roomsSnapshot.getChildren()) {
                    String roomNo = roomDs.getKey();
                    String roomType = roomDs.child("type").getValue(String.class);
                    String usageType = roomDs.child("allowedFor").getValue(String.class); // "Family" or "Student"
                    Boolean isOccupied = roomDs.child("isOccupied").getValue(Boolean.class);

                    boolean occupied = (isOccupied != null && isOccupied);
                    String status = occupied ? "Occupied" : "Vacant";

                    String occupants = "";
                    if (occupied) {
                        if ("Family".equalsIgnoreCase(usageType)) {
                            occupants = familyNameByRoom.getOrDefault(roomNo, "Vacant");
                            if ("Vacant".equals(occupants)) occupants = "Vacant";
                        } else if ("Student".equalsIgnoreCase(usageType)) {
                            int count = studentNamesByRoom.get(roomNo) != null ? studentNamesByRoom.get(roomNo).size() : 0;
                            occupants = count > 0 ? count + " Students" : "Vacant";
                        } else {
                            occupants = "Vacant";
                        }
                    } else {
                        occupants = ""; // or "—" if you prefer
                    }

                    String[] row = new String[]{
                            roomNo != null ? roomNo : "—",
                            roomType != null ? roomType : "—",
                            usageType != null ? usageType : "—",
                            status,
                            occupants
                    };

                    if (occupied) {
                        bookedRoomsData.add(row);
                    } else {
                        availableRoomsData.add(row);
                    }
                }

                // For demo: generate 2 separate PDFs, in practice can merge with sections
                createAndOpenPdf("Booked_Rooms_Report", bookedRoomsData);
                createAndOpenPdf("Available_Rooms_Report", availableRoomsData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportsActivity.this, "Failed to load rooms report data.", Toast.LENGTH_SHORT).show();
            }
        });
    }





    private void createAndOpenPdf(String reportTitle, List<String[]> data) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = reportTitle + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(downloadsDir, fileName);

            PdfGenerator pdfGenerator = new PdfGenerator(this);
            pdfGenerator.generatePdf(reportTitle, data, file);

            openPdf(file);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void generatePdfFromView(String reportName) {
        Bitmap bitmap = Bitmap.createBitmap(reportContentLayout.getWidth(), reportContentLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        reportContentLayout.draw(canvas);

        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = reportName + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                android.graphics.pdf.PdfDocument document = new android.graphics.pdf.PdfDocument();
                android.graphics.pdf.PdfDocument.PageInfo pageInfo = new android.graphics.pdf.PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
                android.graphics.pdf.PdfDocument.Page page = document.startPage(pageInfo);
                page.getCanvas().drawBitmap(bitmap, 0, 0, null);
                document.finishPage(page);
                document.writeTo(fos);
                document.close();
            }

            Toast.makeText(this, "PDF saved to Downloads folder.", Toast.LENGTH_LONG).show();
            openPdf(file);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPdf(File file) {
        Uri fileUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No application available to view PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDateFilterSpinner() {
        String[] filterOptions = {"This Month", "Last 3 Months", "Last 6 Months", "All Time"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateFilterSpinner.setAdapter(adapter);

        dateFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadFinancialData(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadFinancialData(int filterIndex) {
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                processAllData(snapshot, filterIndex);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportsActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processAllData(DataSnapshot snapshot, int filterIndex) {
        Calendar startCal = getStartDate(filterIndex);
        Calendar endCal = Calendar.getInstance();

        double totalIncome = 0;
        double totalExpenses = 0;
        Map<String, Float> expenseCategoryMap = new HashMap<>();
        Map<String, Double> monthlyIncome = new TreeMap<>();
        Map<String, Double> monthlyExpenses = new TreeMap<>();

        for (DataSnapshot tenantNode : snapshot.child("rentPayments").getChildren()) {
            for (DataSnapshot paymentNode : tenantNode.getChildren()) {
                RentPayment payment = paymentNode.getValue(RentPayment.class);
                try {
                    Date date = sdf.parse(payment.paymentDate);
                    if (isDateInRange(date, startCal, endCal)) {
                        totalIncome += payment.amountPaid;
                        String monthYear = monthYearSdf.format(date);
                        monthlyIncome.put(monthYear, monthlyIncome.getOrDefault(monthYear, 0.0) + payment.amountPaid);
                    }
                } catch (Exception e) {}
            }
        }

        for (DataSnapshot expenseNode : snapshot.child("expenses").getChildren()) {
            Expense expense = expenseNode.getValue(Expense.class);
            try {
                Date date = sdf.parse(expense.date);
                if (isDateInRange(date, startCal, endCal)) {
                    totalExpenses += expense.amount;
                    String monthYear = monthYearSdf.format(date);
                    monthlyExpenses.put(monthYear, monthlyExpenses.getOrDefault(monthYear, 0.0) + expense.amount);
                    expenseCategoryMap.put(expense.category, expenseCategoryMap.getOrDefault(expense.category, 0f) + (float)expense.amount);
                }
            } catch (Exception e) {}
        }

        updateUI(totalIncome, totalExpenses, expenseCategoryMap, monthlyIncome, monthlyExpenses, filterIndex);
    }

    private void updateUI(double income, double expenses, Map<String, Float> categoryMap, Map<String, Double> monthlyIncome, Map<String, Double> monthlyExpenses, int filterIndex) {
        tvTotalIncome.setText(String.format(Locale.getDefault(), "₹%,.2f", income));
        tvTotalExpenses.setText(String.format(Locale.getDefault(), "₹%,.2f", expenses));

        double netProfit = income - expenses;
        tvNetProfit.setText(String.format(Locale.getDefault(), "₹%,.2f", netProfit));
        tvNetProfit.setTextColor(netProfit >= 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        setupPieChart(categoryMap);
        setupBarChart(monthlyIncome, monthlyExpenses, filterIndex);
    }

    private void setupPieChart(Map<String, Float> categoryMap) {
        List<PieEntry> entries = new ArrayList<>();
        if (categoryMap.isEmpty() || categoryMap.values().stream().allMatch(v -> v == 0)) {
            pieChart.clear();
            pieChart.setNoDataText("No Expense Data for this Period");
            pieChart.invalidate();
            return;
        }

        for (Map.Entry<String, Float> entry : categoryMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Breakdown");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Expenses");
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void setupBarChart(Map<String, Double> incomeMap, Map<String, Double> expenseMap, int filterIndex) {
        int numMonthsToShow = getNumberOfMonths(filterIndex, incomeMap, expenseMap);

        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < numMonthsToShow; i++) {
            String monthYearKey = monthYearSdf.format(cal.getTime());
            labels.add(monthYearKey.substring(0, 3));

            float incomeVal = incomeMap.getOrDefault(monthYearKey, 0.0).floatValue();
            float expenseVal = expenseMap.getOrDefault(monthYearKey, 0.0).floatValue();

            incomeEntries.add(new BarEntry(i, incomeVal));
            expenseEntries.add(new BarEntry(i, expenseVal));

            cal.add(Calendar.MONTH, -1);
        }

        Collections.reverse(labels);
        Collections.reverse(incomeEntries);
        Collections.reverse(expenseEntries);

        BarDataSet incomeDataSet = new BarDataSet(incomeEntries, "Income");
        incomeDataSet.setColor(Color.parseColor("#4CAF50"));

        BarDataSet expenseDataSet = new BarDataSet(expenseEntries, "Expenses");
        expenseDataSet.setColor(Color.parseColor("#F44336"));

        BarData barData = new BarData(incomeDataSet, expenseDataSet);
        barChart.setData(barData);

        float groupSpace = 0.1f;
        float barSpace = 0.05f;
        float barWidth = 0.4f;
        barData.setBarWidth(barWidth);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(labels.size());

        barChart.groupBars(0, groupSpace, barSpace);
        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private Calendar getStartDate(int filterIndex) {
        Calendar cal = Calendar.getInstance();
        if (filterIndex == 0) cal.set(Calendar.DAY_OF_MONTH, 1);
        else if (filterIndex == 1) cal.add(Calendar.MONTH, -2);
        else if (filterIndex == 2) cal.add(Calendar.MONTH, -5);
        else cal.setTimeInMillis(0);

        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
        return cal;
    }

    private boolean isDateInRange(Date date, Calendar start, Calendar end) {
        return !date.before(start.getTime()) && !date.after(end.getTime());
    }

    private int getNumberOfMonths(int filterIndex, Map<String, Double> income, Map<String, Double> expense) {
        if (filterIndex == 0) return 1;
        if (filterIndex == 1) return 3;
        if (filterIndex == 2) return 6;
        return Math.min(12, Math.max(income.size(), expense.size()));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
