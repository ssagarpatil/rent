//package com.ss.rentmangment;
//
//import android.app.AlertDialog;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.*;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.database.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class RoomsFragment extends Fragment {
//
//    RecyclerView rvRooms;
//    FloatingActionButton fabAdd;
//    RoomsAdapter adapter;
//    List<RoomModel> roomList = new ArrayList<>();
//
//    DatabaseReference roomsRef;
//    DatabaseReference tenantsRef;
//    String adminId;
//
//    public RoomsFragment() {}
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
//
//        rvRooms = view.findViewById(R.id.rvRooms);
//        fabAdd = view.findViewById(R.id.fab_add_room);
//
//        rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
//        adapter = new RoomsAdapter(roomList, new RoomsAdapter.OnRoomActionListener() {
//            @Override
//            public void onEdit(RoomModel room) {
//                showAddEditDialog(room);
//            }
//
//            @Override
//            public void onDelete(RoomModel room) {
//                confirmDelete(room);
//            }
//        }, requireContext());
//        rvRooms.setAdapter(adapter);
//
//        // Get adminId from SharedPreferences
//        if (getContext() != null) {
//            adminId = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
//                    .getString("mobile", "default_admin");
//        } else {
//            adminId = "default_admin";
//        }
//
//        // Set Firebase references
//        roomsRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(adminId)
//                .child("rooms");
//
//        tenantsRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(adminId)
//                .child("tenants");
//
//        loadRooms();
//        listenToTenantChanges();
//
//        fabAdd.setOnClickListener(v -> showAddEditDialog(null));
//
//        return view;
//    }
//
//    private void loadRooms() {
//        roomsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                roomList.clear();
//                for (DataSnapshot ds : snapshot.getChildren()) {
//                    RoomModel r = ds.getValue(RoomModel.class);
//                    if (r != null) {
//                        roomList.add(r);
//                    }
//                }
//                adapter.setList(roomList);
//                updateRoomOccupancyFromTenants();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void listenToTenantChanges() {
//        tenantsRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                updateRoomOccupancyFromTenants();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to load tenants", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void updateRoomOccupancyFromTenants() {
//        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot tenantsSnapshot) {
//                Map<String, Integer> roomStudentCount = new HashMap<>();
//                Map<String, Boolean> roomHasFamily = new HashMap<>();
//                Map<String, String> roomCurrentType = new HashMap<>();
//                Map<String, Integer> roomFinalOccupancy = new HashMap<>();
//
//                for (DataSnapshot tenantSnapshot : tenantsSnapshot.getChildren()) {
//                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
//                    if (tenant != null && tenant.assignedRoomKey != null && !tenant.assignedRoomKey.isEmpty()) {
//                        String roomId = tenant.assignedRoomKey;
//                        boolean isFamily = (tenant.emergencyContactName != null &&
//                                !tenant.emergencyContactName.isEmpty());
//
//                        if (isFamily) {
//                            roomHasFamily.put(roomId, true);
//                            roomCurrentType.put(roomId, "Family");
//                        } else {
//                            roomStudentCount.put(roomId, roomStudentCount.getOrDefault(roomId, 0) + 1);
//                            if (!roomHasFamily.getOrDefault(roomId, false)) {
//                                roomCurrentType.put(roomId, "Students");
//                            }
//                        }
//                    }
//                }
//
//                for (RoomModel room : roomList) {
//                    String roomId = room.getRoomId();
//                    String currentType = roomCurrentType.getOrDefault(roomId, "Empty");
//                    boolean hasFamily = roomHasFamily.getOrDefault(roomId, false);
//                    int studentCount = roomStudentCount.getOrDefault(roomId, 0);
//                    int finalOccupancy;
//
//                    if (hasFamily || "Family".equals(currentType)) {
//                        finalOccupancy = room.getCapacity();
//                    } else if ("Students".equals(currentType)) {
//                        finalOccupancy = studentCount;
//                    } else {
//                        finalOccupancy = 0;
//                    }
//
//                    roomFinalOccupancy.put(roomId, finalOccupancy);
//                }
//
//                for (RoomModel room : roomList) {
//                    String roomId = room.getRoomId();
//                    int newOccupancy = roomFinalOccupancy.getOrDefault(roomId, 0);
//                    if (room.getOccupied() != newOccupancy) {
//                        updateRoomOccupancy(roomId, newOccupancy, room);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Failed to update occupancy", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private final BroadcastReceiver roomUpdateReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            if ("ROOM_OCCUPANCY_UPDATED".equals(intent.getAction())) {
//                String roomId = intent.getStringExtra("roomId");
//                int newOccupancy = intent.getIntExtra("newOccupancy", 0);
//                for (RoomModel room : roomList) {
//                    if (room.getRoomId().equals(roomId)) {
//                        room.setOccupied(newOccupancy);
//                        if (newOccupancy >= room.getCapacity()) {
//                            room.setAllowedFor("Family");
//                        }
//                        adapter.notifyDataSetChanged();
//                        break;
//                    }
//                }
//            }
//        }
//    };
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        requireActivity().registerReceiver(roomUpdateReceiver,
//                new IntentFilter("ROOM_OCCUPANCY_UPDATED"),
//                Context.RECEIVER_NOT_EXPORTED);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        requireActivity().unregisterReceiver(roomUpdateReceiver);
//    }
//
//    private void updateRoomOccupancy(String roomId, int newOccupancy, RoomModel room) {
//        roomsRef.child(roomId).child("occupied").setValue(newOccupancy)
//                .addOnSuccessListener(aVoid -> {
//                    for (RoomModel r : roomList) {
//                        if (r.getRoomId().equals(roomId)) {
//                            r.setOccupied(newOccupancy);
//                            break;
//                        }
//                    }
//                    adapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
//                });
//    }
//
//    private void showAddEditDialog(RoomModel editRoom) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_room, null);
//        builder.setView(dialogView);
//        AlertDialog dialog = builder.create();
//
//        EditText etName = dialogView.findViewById(R.id.etRoomName);
//        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
//        CheckBox cbFamily = dialogView.findViewById(R.id.cbFamily);
//        CheckBox cbStudents = dialogView.findViewById(R.id.cbStudents);
//        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
//        EditText etRent = dialogView.findViewById(R.id.etRent);
//        EditText etDeposit = dialogView.findViewById(R.id.etDeposit);
//        EditText etMaintenance = dialogView.findViewById(R.id.etMaintenance);
//        EditText etNotes = dialogView.findViewById(R.id.etNotes);
//        Button btnSave = dialogView.findViewById(R.id.btnSave);
//        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
//
//        String[] types = {"1BHK", "2BHK", "1RK", "1R", "Dormitory"};
//        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(),
//                android.R.layout.simple_spinner_item, types);
//        spinnerType.setAdapter(spAdapter);
//
//        if (editRoom != null) {
//            etName.setText(editRoom.getName());
//            for (int i = 0; i < types.length; i++) {
//                if (types[i].equals(editRoom.getType())) {
//                    spinnerType.setSelection(i);
//                    break;
//                }
//            }
//            if (editRoom.getAllowedFor() != null) {
//                cbFamily.setChecked(editRoom.getAllowedFor().contains("Family"));
//                cbStudents.setChecked(editRoom.getAllowedFor().contains("Students"));
//            }
//            etCapacity.setText(String.valueOf(editRoom.getCapacity()));
//            etRent.setText(String.valueOf(editRoom.getRent()));
//            etDeposit.setText(String.valueOf(editRoom.getDeposit()));
//            etMaintenance.setText(String.valueOf(editRoom.getMaintenanceCharges()));
//            etNotes.setText(editRoom.getNotes());
//        }
//
//        btnCancel.setOnClickListener(v -> dialog.dismiss());
//
//        btnSave.setOnClickListener(v -> {
//            String name = etName.getText().toString().trim();
//            String type = spinnerType.getSelectedItem().toString();
//            boolean family = cbFamily.isChecked();
//            boolean students = cbStudents.isChecked();
//            String sCapacity = etCapacity.getText().toString().trim();
//            String sRent = etRent.getText().toString().trim();
//            String sDeposit = etDeposit.getText().toString().trim();
//            String sMaintenance = etMaintenance.getText().toString().trim();
//            String notes = etNotes.getText().toString().trim();
//
//            if (TextUtils.isEmpty(name)) {
//                etName.setError("Enter room name");
//                return;
//            }
//            if (TextUtils.isEmpty(sCapacity)) {
//                etCapacity.setError("Enter capacity");
//                return;
//            }
//            if (!family && !students) {
//                Toast.makeText(getContext(), "Select at least one occupant type", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            try {
//                int capacity = Integer.parseInt(sCapacity);
//                int rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
//                int deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
//                int maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);
//
//                if (capacity <= 0) {
//                    etCapacity.setError("Capacity must be > 0");
//                    return;
//                }
//
//                String allowedFor = family && students ? "Family,Students" :
//                        family ? "Family" : "Students";
//                String roomKey = name.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
//
//                if (editRoom != null) {
//                    // Edit existing room
//                    RoomModel updated = new RoomModel(roomKey, name, type, allowedFor,
//                            capacity, editRoom.getOccupied(), rent, deposit,
//                            maintenance, notes, System.currentTimeMillis());
//                    roomsRef.child(roomKey).setValue(updated)
//                            .addOnSuccessListener(aVoid -> {
//                                Toast.makeText(getContext(), "Room updated", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            });
//                } else {
//                    // Add new room
//                    RoomModel newRoom = new RoomModel(roomKey, name, type, allowedFor,
//                            capacity, 0, rent, deposit, maintenance, notes,
//                            System.currentTimeMillis());
//                    roomsRef.child(roomKey).setValue(newRoom)
//                            .addOnSuccessListener(aVoid -> {
//                                Toast.makeText(getContext(), "Room added", Toast.LENGTH_SHORT).show();
//                                dialog.dismiss();
//                            });
//                }
//            } catch (NumberFormatException e) {
//                Toast.makeText(getContext(), "Invalid number values", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        dialog.show();
//    }
//
//    private void confirmDelete(RoomModel room) {
//        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                boolean hasTenants = false;
//                for (DataSnapshot tenantSnapshot : snapshot.getChildren()) {
//                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
//                    if (tenant != null && room.getRoomId().equals(tenant.assignedRoomKey)) {
//                        hasTenants = true;
//                        break;
//                    }
//                }
//
//                if (hasTenants) {
//                    new AlertDialog.Builder(getContext())
//                            .setTitle("Cannot Delete")
//                            .setMessage("Room has tenants. Remove them first.")
//                            .setPositiveButton("OK", null)
//                            .show();
//                } else {
//                    new AlertDialog.Builder(getContext())
//                            .setTitle("Delete Room")
//                            .setMessage("Are you sure? This cannot be undone.")
//                            .setPositiveButton("Delete", (d, w) -> {
//                                roomsRef.child(room.getRoomId()).removeValue()
//                                        .addOnSuccessListener(aVoid -> {
//                                            Toast.makeText(getContext(), "Room deleted", Toast.LENGTH_SHORT).show();
//                                        });
//                            })
//                            .setNegativeButton("Cancel", null)
//                            .show();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Toast.makeText(getContext(), "Error checking tenants", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        // Clean up Firebase listeners
//        if (roomsRef != null) roomsRef.removeEventListener(roomsListener);
//        if (tenantsRef != null) tenantsRef.removeEventListener(tenantsListener);
//    }
//
//    private final ValueEventListener roomsListener = new ValueEventListener() {
//        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {}
//        @Override public void onCancelled(@NonNull DatabaseError error) {}
//    };
//
//    private final ValueEventListener tenantsListener = new ValueEventListener() {
//        @Override public void onDataChange(@NonNull DataSnapshot snapshot) {}
//        @Override public void onCancelled(@NonNull DatabaseError error) {}
//    };
//}




package com.ss.rentmangment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import androidx.core.content.ContextCompat;

import java.util.Map;

public class RoomsFragment extends Fragment {

    RecyclerView rvRooms;
    FloatingActionButton fabAdd;
    RoomsAdapter adapter;
    List<RoomModel> roomList = new ArrayList<>();

    DatabaseReference roomsRef;
    DatabaseReference tenantsRef;
    String adminId;

    public RoomsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        fabAdd = view.findViewById(R.id.fab_add_room);

        rvRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RoomsAdapter(roomList, new RoomsAdapter.OnRoomActionListener() {
            @Override
            public void onEdit(RoomModel room) {
                showAddEditDialog(room);
            }

            @Override
            public void onDelete(RoomModel room) {
                confirmDelete(room);
            }
        }, requireContext());
        rvRooms.setAdapter(adapter);

        // Get adminId from SharedPreferences
        if (getContext() != null) {
            adminId = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .getString("mobile", "default_admin");
        } else {
            adminId = "default_admin";
        }

        // Set Firebase references
        roomsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminId)
                .child("rooms");

        tenantsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminId)
                .child("tenants");

        loadRooms();
        listenToTenantChanges();

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void loadRooms() {
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RoomModel r = ds.getValue(RoomModel.class);
                    if (r != null) {
                        roomList.add(r);
                    }
                }
                adapter.setList(roomList);
                updateRoomOccupancyFromTenants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenToTenantChanges() {
        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                updateRoomOccupancyFromTenants();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load tenants", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRoomOccupancyFromTenants() {
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantsSnapshot) {
                Map<String, Integer> roomStudentCount = new HashMap<>();
                Map<String, Boolean> roomHasFamily = new HashMap<>();
                Map<String, String> roomCurrentType = new HashMap<>();
                Map<String, Integer> roomFinalOccupancy = new HashMap<>();

                for (DataSnapshot tenantSnapshot : tenantsSnapshot.getChildren()) {
                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
                    if (tenant != null && tenant.assignedRoomKey != null && !tenant.assignedRoomKey.isEmpty()) {
                        String roomId = tenant.assignedRoomKey;
                        boolean isFamily = (tenant.emergencyContactName != null &&
                                !tenant.emergencyContactName.isEmpty());

                        if (isFamily) {
                            roomHasFamily.put(roomId, true);
                            roomCurrentType.put(roomId, "Family");
                        } else {
                            roomStudentCount.put(roomId, roomStudentCount.getOrDefault(roomId, 0) + 1);
                            if (!roomHasFamily.getOrDefault(roomId, false)) {
                                roomCurrentType.put(roomId, "Students");
                            }
                        }
                    }
                }

                for (RoomModel room : roomList) {
                    String roomId = room.getRoomId();
                    String currentType = roomCurrentType.getOrDefault(roomId, "Empty");
                    boolean hasFamily = roomHasFamily.getOrDefault(roomId, false);
                    int studentCount = roomStudentCount.getOrDefault(roomId, 0);
                    int finalOccupancy;

                    if (hasFamily || "Family".equals(currentType)) {
                        finalOccupancy = room.getCapacity();
                    } else if ("Students".equals(currentType)) {
                        finalOccupancy = studentCount;
                    } else {
                        finalOccupancy = 0;
                    }

                    roomFinalOccupancy.put(roomId, finalOccupancy);
                }

                for (RoomModel room : roomList) {
                    String roomId = room.getRoomId();
                    int newOccupancy = roomFinalOccupancy.getOrDefault(roomId, 0);
                    if (room.getOccupied() != newOccupancy) {
                        updateRoomOccupancy(roomId, newOccupancy, room);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to update occupancy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final BroadcastReceiver roomUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ROOM_OCCUPANCY_UPDATED".equals(intent.getAction())) {
                String roomId = intent.getStringExtra("roomId");
                int newOccupancy = intent.getIntExtra("newOccupancy", 0);
                for (RoomModel room : roomList) {
                    if (room.getRoomId().equals(roomId)) {
                        room.setOccupied(newOccupancy);
                        if (newOccupancy >= room.getCapacity()) {
                            room.setAllowedFor("Family");
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("ROOM_OCCUPANCY_UPDATED");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Android 8.0 (API 26) and above
            requireActivity().registerReceiver(roomUpdateReceiver, filter,
                    Context.RECEIVER_NOT_EXPORTED);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(roomUpdateReceiver);
    }

    private void updateRoomOccupancy(String roomId, int newOccupancy, RoomModel room) {
        roomsRef.child(roomId).child("occupied").setValue(newOccupancy)
                .addOnSuccessListener(aVoid -> {
                    for (RoomModel r : roomList) {
                        if (r.getRoomId().equals(roomId)) {
                            r.setOccupied(newOccupancy);
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                });
    }

    // FIXED ADD/EDIT DIALOG METHOD
    private void showAddEditDialog(RoomModel editRoom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        EditText etName = dialogView.findViewById(R.id.etRoomName);
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        CheckBox cbFamily = dialogView.findViewById(R.id.cbFamily);
        CheckBox cbStudents = dialogView.findViewById(R.id.cbStudents);
        EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        EditText etRent = dialogView.findViewById(R.id.etRent);
        EditText etDeposit = dialogView.findViewById(R.id.etDeposit);
        EditText etMaintenance = dialogView.findViewById(R.id.etMaintenance);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String[] types = {"1BHK", "2BHK", "1RK", "1R", "Dormitory"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, types);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(spAdapter);

        // Pre-fill data for editing
        if (editRoom != null) {
            etName.setText(editRoom.getName());
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(editRoom.getType())) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
            if (editRoom.getAllowedFor() != null) {
                cbFamily.setChecked(editRoom.getAllowedFor().contains("Family"));
                cbStudents.setChecked(editRoom.getAllowedFor().contains("Students"));
            }
            etCapacity.setText(String.valueOf(editRoom.getCapacity()));
            etRent.setText(String.valueOf(editRoom.getRent()));
            etDeposit.setText(String.valueOf(editRoom.getDeposit()));
            etMaintenance.setText(String.valueOf(editRoom.getMaintenanceCharges()));
            etNotes.setText(editRoom.getNotes());
        } else {
            // Set default values for new room
            cbFamily.setChecked(true); // Default to allow families
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            // Show progress
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            String name = etName.getText().toString().trim();
            String type = spinnerType.getSelectedItem().toString();
            boolean family = cbFamily.isChecked();
            boolean students = cbStudents.isChecked();
            String sCapacity = etCapacity.getText().toString().trim();
            String sRent = etRent.getText().toString().trim();
            String sDeposit = etDeposit.getText().toString().trim();
            String sMaintenance = etMaintenance.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter room name");
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                return;
            }
            if (TextUtils.isEmpty(sCapacity)) {
                etCapacity.setError("Enter capacity");
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                return;
            }
            if (!family && !students) {
                Toast.makeText(getContext(), "Select at least one occupant type", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                return;
            }

            try {
                int capacity = Integer.parseInt(sCapacity);
                int rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
                int deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
                int maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);

                if (capacity <= 0) {
                    etCapacity.setError("Capacity must be > 0");
                    btnSave.setEnabled(true);
                    btnSave.setText("Save");
                    return;
                }

                String allowedFor = family && students ? "Family,Students" :
                        family ? "Family" : "Students";

                if (editRoom != null) {
                    // Edit existing room - use existing roomId
                    String roomKey = editRoom.getRoomId();
                    RoomModel updated = new RoomModel(roomKey, name, type, allowedFor,
                            capacity, editRoom.getOccupied(), rent, deposit,
                            maintenance, notes, System.currentTimeMillis());

                    roomsRef.child(roomKey).setValue(updated)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Room updated successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnSave.setEnabled(true);
                                btnSave.setText("Save");
                            });
                } else {
                    // Add new room - generate unique key
                    String roomKey = roomsRef.push().getKey();
                    if (roomKey == null) {
                        Toast.makeText(getContext(), "Failed to generate room ID", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                        return;
                    }

                    RoomModel newRoom = new RoomModel(roomKey, name, type, allowedFor,
                            capacity, 0, rent, deposit, maintenance, notes,
                            System.currentTimeMillis());

                    roomsRef.child(roomKey).setValue(newRoom)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Room added successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add room: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                btnSave.setEnabled(true);
                                btnSave.setText("Save");
                            });
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("Save");
            }
        });

        dialog.show();
    }

    private void confirmDelete(RoomModel room) {
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasTenants = false;
                for (DataSnapshot tenantSnapshot : snapshot.getChildren()) {
                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
                    if (tenant != null && room.getRoomId().equals(tenant.assignedRoomKey)) {
                        hasTenants = true;
                        break;
                    }
                }

                if (hasTenants) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Cannot Delete")
                            .setMessage("Room has tenants. Remove them first.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete Room")
                            .setMessage("Are you sure? This cannot be undone.")
                            .setPositiveButton("Delete", (d, w) -> {
                                roomsRef.child(room.getRoomId()).removeValue()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Room deleted", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error checking tenants", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
