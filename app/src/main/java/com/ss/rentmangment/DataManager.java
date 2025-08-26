package com.ss.rentmangment;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ss.rentmangment.RoomModel;
import com.ss.rentmangment.Tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;
    private final DatabaseReference roomsRef;
    private final DatabaseReference tenantsRef;
    private final String adminId;

    private final List<RoomModel> allRooms = new ArrayList<>();
    private final List<Tenant> allTenants = new ArrayList<>();

    private final List<DataUpdateListener> listeners = new ArrayList<>();

    public interface DataUpdateListener {
        void onDataUpdated();
        void onError(String message);
    }

    private DataManager(Context context) {
        SharedPreferences sp = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        adminId = sp.getString("mobile", "default_admin");

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        roomsRef = db.getReference("users").child(adminId).child("rooms");
        tenantsRef = db.getReference("users").child(adminId).child("tenants");

        startListeners();
    }

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    public void addListener(DataUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        if (!allRooms.isEmpty() || !allTenants.isEmpty()) {
            listener.onDataUpdated();
        }
    }

    public void removeListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (DataUpdateListener listener : listeners) {
            listener.onDataUpdated();
        }
    }

    private void notifyError(String message) {
        for (DataUpdateListener listener : listeners) {
            listener.onError(message);
        }
    }

    private void startListeners() {
        roomsRef.orderByChild("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allRooms.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    allRooms.add(ds.getValue(RoomModel.class));
                }
                recalculateOccupancyAndNotify();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                notifyError("Failed to load rooms.");
            }
        });

        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTenants.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    allTenants.add(ds.getValue(Tenant.class));
                }
                recalculateOccupancyAndNotify();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                notifyError("Failed to load tenants.");
            }
        });
    }

    private void recalculateOccupancyAndNotify() {
        for (RoomModel room : allRooms) {
            long activeTenantsInRoom = allTenants.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase("Active") &&
                            room.getRoomId().equals(t.assignedRoomKey))
                    .count();
            room.setOccupied((int) activeTenantsInRoom);
        }
        notifyListeners();
    }

    // --- Public Data Accessors ---

    public String getAdminId() {
        return adminId;
    }

    public DatabaseReference getRoomsDatabaseReference() {
        return roomsRef;
    }

    public List<RoomModel> getAllRooms() {
        return new ArrayList<>(allRooms);
    }

    public List<Tenant> getActiveTenants() {
        return allTenants.stream()
                .filter(t -> t.status != null && t.status.equalsIgnoreCase("Active"))
                .collect(Collectors.toList());
    }
}
