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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView tvTotalRooms, tvOccupiedRooms, tvTotalTenants, tvPendingRent;
    private TextView tvStudentsCount, tvFamiliesCount;
    private Button btnCollectRent;

    private DatabaseReference roomsRef, tenantsRef, paymentsRef;
    private String adminId;
    private double totalExpected = 0;

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvTotalTenants = view.findViewById(R.id.tvTotalTenants);
        tvPendingRent = view.findViewById(R.id.tvPendingRent);
        tvStudentsCount = view.findViewById(R.id.tvStudentsCount);
        tvFamiliesCount = view.findViewById(R.id.tvFamiliesCount);
        btnCollectRent = view.findViewById(R.id.btnCollectRent);

        Context context = getContext();
        if (context != null) {
            adminId = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("mobile", "default_admin");
        } else {
            adminId = "default_admin";
        }

        roomsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(adminId).child("rooms");
        tenantsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(adminId).child("tenants");
        paymentsRef = FirebaseDatabase.getInstance()
                .getReference("users").child(adminId).child("rentPayments");

        loadDashboardStats();

        btnCollectRent.setOnClickListener(v ->
                startActivity(new Intent(getContext(), CollectRentActivity.class))
        );

        return view;
    }

    private void loadDashboardStats() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalRooms = 0;
                int occupiedRooms = 0;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RoomModel room = ds.getValue(RoomModel.class);
                    if (room != null) {
                        totalRooms++;
                        if (room.getOccupied() > 0) {
                            occupiedRooms++;
                        }
                    }
                }
                animateTextView(tvTotalRooms, totalRooms);
                animateTextView(tvOccupiedRooms, occupiedRooms);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });

        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantSnapshot) {
                int totalTenants = 0;
                int students = 0;
                int families = 0;
                totalExpected = 0;

                for (DataSnapshot ds : tenantSnapshot.getChildren()) {
                    Tenant tenant = ds.getValue(Tenant.class);
                    if (tenant != null && tenant.rentAmount > 0) {
                        totalTenants++;
                        totalExpected += tenant.rentAmount;
                        if ("Students".equalsIgnoreCase(tenant.tenantType)) {
                            students++;
                        } else if ("Family".equalsIgnoreCase(tenant.tenantType)) {
                            families++;
                        }
                    }
                }

                animateTextView(tvTotalTenants, totalTenants);
                animateTextView(tvStudentsCount, students);
                animateTextView(tvFamiliesCount, families);

                paymentsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot paymentSnapshot) {
                        double totalPaid = 0;
                        String currentMonth = getCurrentMonthYear();
                        for (DataSnapshot tenantPayments : paymentSnapshot.getChildren()) {
                            for (DataSnapshot pay : tenantPayments.getChildren()) {
                                RentPayment rp = pay.getValue(RentPayment.class);
                                if (rp != null && rp.paymentDate != null &&
                                        rp.paymentDate.contains(currentMonth)) {
                                    totalPaid += rp.amountPaid;
                                }
                            }
                        }
                        double pending = totalExpected - totalPaid;
                        if (pending < 0) pending = 0;
                        animateTextViewCurrency(tvPendingRent, (int) pending);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Animate integer count-up with fade+scale on TextView
    private void animateTextView(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(animatedValue));
        });
        animator.start();

        // Fade and scale animation on the TextView
        textView.setAlpha(0f);
        textView.setScaleX(0.5f);
        textView.setScaleY(0.5f);
        textView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .start();
    }

    // Animate currency count-up with fade+scale on TextView
    private void animateTextViewCurrency(TextView textView, int endValue) {
        ValueAnimator animator = ValueAnimator.ofInt(0, endValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText("₹" + animatedValue);
        });
        animator.start();

        // Fade and scale animation on the TextView
        textView.setAlpha(0f);
        textView.setScaleX(0.5f);
        textView.setScaleY(0.5f);
        textView.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .start();
    }

    private String getCurrentMonthYear() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        return month + "/" + year;
    }
}
