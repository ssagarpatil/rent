//package com.ss.rentmangment;
//
//import android.animation.ValueAnimator;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.Calendar;
//
//public class HomeFragment extends Fragment {
//
//    private TextView tvTotalRooms, tvOccupiedRooms, tvTotalTenants, tvPendingRent;
//    private TextView tvStudentsCount, tvFamiliesCount;
//    private Button btnCollectRent;
//
//    private DatabaseReference roomsRef, tenantsRef, paymentsRef;
//    private String adminId;
//    private double totalExpected = 0;
//
//    public HomeFragment() {}
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_home, container, false);
//
//        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
//        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
//        tvTotalTenants = view.findViewById(R.id.tvTotalTenants);
//        tvPendingRent = view.findViewById(R.id.tvPendingRent);
//        tvStudentsCount = view.findViewById(R.id.tvStudentsCount);
//        tvFamiliesCount = view.findViewById(R.id.tvFamiliesCount);
//        btnCollectRent = view.findViewById(R.id.btnCollectRent);
//
//        Context context = getContext();
//        if (context != null) {
//            adminId = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//                    .getString("mobile", "default_admin");
//        } else {
//            adminId = "default_admin";
//        }
//
//        roomsRef = FirebaseDatabase.getInstance()
//                .getReference("users").child(adminId).child("rooms");
//        tenantsRef = FirebaseDatabase.getInstance()
//                .getReference("users").child(adminId).child("tenants");
//        paymentsRef = FirebaseDatabase.getInstance()
//                .getReference("users").child(adminId).child("rentPayments");
//
//        loadDashboardStats();
//
//        btnCollectRent.setOnClickListener(v ->
//                startActivity(new Intent(getContext(), CollectRentActivity.class))
//        );
//
//        return view;
//    }
//
//    private void loadDashboardStats() {
//        roomsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                int totalRooms = 0;
//                int occupiedRooms = 0;
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    RoomModel room = ds.getValue(RoomModel.class);
//                    if (room != null) {
//                        totalRooms++;
//                        if (room.getOccupied() > 0) {
//                            occupiedRooms++;
//                        }
//                    }
//                }
//                animateTextView(tvTotalRooms, totalRooms);
//                animateTextView(tvOccupiedRooms, occupiedRooms);
//            }
//            @Override public void onCancelled(@NonNull DatabaseError error) {}
//        });
//
//        tenantsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
//                int totalTenants = 0;
//                int students = 0;
//                int families = 0;
//                totalExpected = 0;
//
//                for (DataSnapshot ds : tenantSnapshot.getChildren()) {
//                    Tenant tenant = ds.getValue(Tenant.class);
//                    if (tenant != null && tenant.rentAmount > 0) {
//                        totalTenants++;
//                        totalExpected += tenant.rentAmount;
//                        if ("Students".equalsIgnoreCase(tenant.tenantType)) {
//                            students++;
//                        } else if ("Family".equalsIgnoreCase(tenant.tenantType)) {
//                            families++;
//                        }
//                    }
//                }
//
//                animateTextView(tvTotalTenants, totalTenants);
//                animateTextView(tvStudentsCount, students);
//                animateTextView(tvFamiliesCount, families);
//
//                paymentsRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
//                        double totalPaid = 0;
//                        String currentMonth = getCurrentMonthYear();
//                        for (DataSnapshot tenantPayments : paymentSnapshot.getChildren()) {
//                            for (DataSnapshot pay : tenantPayments.getChildren()) {
//                                RentPayment rp = pay.getValue(RentPayment.class);
//                                if (rp != null && rp.paymentDate != null &&
//                                        rp.paymentDate.contains(currentMonth)) {
//                                    totalPaid += rp.amountPaid;
//                                }
//                            }
//                        }
//                        double pending = totalExpected - totalPaid;
//                        if (pending < 0) pending = 0;
//                        animateTextViewCurrency(tvPendingRent, (int) pending);
//                    }
//                    @Override public void onCancelled(@NonNull DatabaseError error) {}
//                });
//            }
//            @Override public void onCancelled(@NonNull DatabaseError error) {}
//        });
//    }
//
//    // Animate integer count-up with fade+scale on TextView
//    private void animateTextView(TextView textView, int endValue) {
//        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
//        animator.setDuration(1000);
//        animator.addUpdateListener(animation -> {
//            int animatedValue = (int) animation.getAnimatedValue();
//            textView.setText(String.valueOf(animatedValue));
//        });
//        animator.start();
//
//        // Fade and scale animation on the TextView
//        textView.setAlpha(0f);
//        textView.setScaleX(0.5f);
//        textView.setScaleY(0.5f);
//        textView.animate()
//                .alpha(1f)
//                .scaleX(1f)
//                .scaleY(1f)
//                .setDuration(600)
//                .start();
//    }
//
//    // Animate currency count-up with fade+scale on TextView
//    private void animateTextViewCurrency(TextView textView, int endValue) {
//        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
//        animator.setDuration(1000);
//        animator.addUpdateListener(animation -> {
//            int animatedValue = (int) animation.getAnimatedValue();
//            textView.setText("₹" + animatedValue);
//        });
//        animator.start();
//
//        // Fade and scale animation on the TextView
//        textView.setAlpha(0f);
//        textView.setScaleX(0.5f);
//        textView.setScaleY(0.5f);
//        textView.animate()
//                .alpha(1f)
//                .scaleX(1f)
//                .scaleY(1f)
//                .setDuration(600)
//                .start();
//    }
//
//    private String getCurrentMonthYear() {
//        Calendar cal = Calendar.getInstance();
//        int month = cal.get(Calendar.MONTH) + 1;
//        int year = cal.get(Calendar.YEAR);
//        return month + "/" + year;
//    }
//}

package com.ss.rentmangment;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    // --- UI Components ---
    private TextView tvTotalRooms, tvOccupiedRooms, tvTotalTenants, tvPendingRent;
    private TextView tvStudentsCount, tvFamiliesCount;
    private Button btnCollectRent;

    // --- Firebase References ---
    private DatabaseReference roomsRef, tenantsRef, paymentsRef;
    private String adminId;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        setupFirebase();

        // Load all dashboard statistics from Firebase
        loadDashboardStats();

        btnCollectRent.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CollectRentActivity.class))
        );

        return view;
    }

    private void initializeViews(View view) {
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvTotalTenants = view.findViewById(R.id.tvTotalTenants);
        tvPendingRent = view.findViewById(R.id.tvPendingRent);
        tvStudentsCount = view.findViewById(R.id.tvStudentsCount);
        tvFamiliesCount = view.findViewById(R.id.tvFamiliesCount);
        btnCollectRent = view.findViewById(R.id.btnCollectRent);
    }

    private void setupFirebase() {
        if (getContext() == null) return;
        adminId = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("mobile", "default_admin");

        DatabaseReference userRootRef = FirebaseDatabase.getInstance().getReference("users").child(adminId);
        roomsRef = userRootRef.child("rooms");
        tenantsRef = userRootRef.child("tenants");
        paymentsRef = userRootRef.child("rentPayments");
    }

    /**
     * This single method now orchestrates the loading of all dashboard data.
     * It uses separate listeners for clarity and efficiency.
     */
    private void loadDashboardStats() {
        // Listener for Room Statistics
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRooms = (int) snapshot.getChildrenCount();
                int occupiedRooms = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RoomModel room = ds.getValue(RoomModel.class);
                    if (room != null && room.getOccupied() > 0) {
                        occupiedRooms++;
                    }
                }
                animateTextView(tvTotalRooms, totalRooms);
                animateTextView(tvOccupiedRooms, occupiedRooms);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                // Optionally show a toast or log the error
            }
        });

        // Listener for Tenant Statistics and Rent Calculation
        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                int activeTenantsCount = 0;
                int students = 0;
                int families = 0;
                double totalExpectedRent = 0;

                for (DataSnapshot ds : tenantSnapshot.getChildren()) {
                    Tenant tenant = ds.getValue(Tenant.class);
                    // *** THIS IS THE CRITICAL FIX ***
                    // We only count tenants if their status is "Active".
                    if (tenant != null && "Active".equalsIgnoreCase(tenant.status)) {
                        activeTenantsCount++;
                        totalExpectedRent += tenant.rentAmount;

                        if ("Students".equalsIgnoreCase(tenant.tenantType)) {
                            students++;
                        } else if ("Family".equalsIgnoreCase(tenant.tenantType)) {
                            families++;
                        }
                    }
                }

                animateTextView(tvTotalTenants, activeTenantsCount);
                animateTextView(tvStudentsCount, students);
                animateTextView(tvFamiliesCount, families);

                // After getting the total expected rent, fetch payments to calculate what's pending.
                calculatePendingRent(totalExpectedRent);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /**
     * Calculates the pending rent for the current month.
     * This is called after the total expected rent from active tenants is known.
     * @param totalExpected The total rent amount expected from all active tenants.
     */
    private void calculatePendingRent(double totalExpected) {
        // Use a Single Value Event Listener here as we only need to check payments once per update.
        paymentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
                double totalPaidThisMonth = 0;
                String currentMonthYear = getCurrentMonthYear(); // Format like "MM/yyyy"

                for (DataSnapshot tenantPaymentsNode : paymentSnapshot.getChildren()) {
                    for (DataSnapshot paymentIdNode : tenantPaymentsNode.getChildren()) {
                        RentPayment payment = paymentIdNode.getValue(RentPayment.class);
                        if (payment != null && payment.paymentDate != null && payment.paymentDate.contains(currentMonthYear)) {
                            totalPaidThisMonth += payment.amountPaid;
                        }
                    }
                }

                double pendingRent = totalExpected - totalPaidThisMonth;
                // Ensure pending rent is not negative
                animateTextViewCurrency(tvPendingRent, (int) Math.max(0, pendingRent));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // --- ANIMATION & HELPER METHODS ---

    private void animateTextView(TextView textView, int endValue) {
        if(textView == null) return;
        // Get the current value to animate from, to avoid starting from 0 every time.
        int startValue = 0;
        try {
            startValue = Integer.parseInt(textView.getText().toString());
        } catch (NumberFormatException e) {
            // Ignore if the text is not a number (e.g., initial state)
        }

        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(800); // Slightly faster for a snappier feel
        animator.addUpdateListener(animation ->
                textView.setText(String.valueOf((int) animation.getAnimatedValue()))
        );
        animator.start();
    }

    private void animateTextViewCurrency(TextView textView, int endValue) {
        if(textView == null) return;
        int startValue = 0;
        try {
            startValue = Integer.parseInt(textView.getText().toString().replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            // Ignore if text is not a number
        }

        ValueAnimator animator = ValueAnimator.ofInt(startValue, endValue);
        animator.setDuration(800);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText("₹" + animatedValue);
        });
        animator.start();
    }

    /**
     * Returns the current month and year in a consistent format (e.g., "08/2025").
     */
    private String getCurrentMonthYear() {
        // Using SimpleDateFormat for a reliable format
        return new SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }
}
