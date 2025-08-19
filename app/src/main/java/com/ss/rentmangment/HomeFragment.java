package com.ss.rentmangment;

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
                tvTotalRooms.setText(String.valueOf(totalRooms));
                tvOccupiedRooms.setText(String.valueOf(occupiedRooms));
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

                tvTotalTenants.setText(String.valueOf(totalTenants));
                tvStudentsCount.setText(String.valueOf(students));
                tvFamiliesCount.setText(String.valueOf(families));

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
                        tvPendingRent.setText("₹" + (int) pending);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private String getCurrentMonthYear() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        return month + "/" + year;
    }
}
