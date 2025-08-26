package com.ss.rentmangment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Correct and single import for AlertDialog
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomsFragment extends Fragment {

    private RecyclerView rvRooms;
    private FloatingActionButton fabAdd;
    private RoomsAdapter adapter;
    private List<RoomModel> roomList = new ArrayList<>();

    private DatabaseReference roomsRef;
    private DatabaseReference tenantsRef;
    private String adminId;

    private boolean isTenantsDataLoaded = false;

    public RoomsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);

        rvRooms = view.findViewById(R.id.rvRooms);
        fabAdd = view.findViewById(R.id.fab_add_room);

        setupRecyclerView();
        setupFirebase();

        loadRoomsData();
        listenForTenantDataChanges();

        fabAdd.setOnClickListener(v -> showAddEditDialog(null));

        return view;
    }

    private void setupRecyclerView() {
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
    }

    private void setupFirebase() {
        adminId = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("mobile", "default_admin");

        DatabaseReference userRootRef = FirebaseDatabase.getInstance().getReference("users").child(adminId);
        roomsRef = userRootRef.child("rooms");
        tenantsRef = userRootRef.child("tenants");
    }

    private void loadRoomsData() {
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
                if (isTenantsDataLoaded) {
                    recalculateAndSyncOccupancy();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load rooms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void listenForTenantDataChanges() {
        tenantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                isTenantsDataLoaded = true;
                recalculateAndSyncOccupancy();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to listen for tenant changes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recalculateAndSyncOccupancy() {
        tenantsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot tenantsSnapshot) {
                Map<String, Integer> occupancyMap = new HashMap<>();

                for (DataSnapshot tenantDs : tenantsSnapshot.getChildren()) {
                    Tenant tenant = tenantDs.getValue(Tenant.class);
                    if (tenant != null && "Active".equalsIgnoreCase(tenant.status) && tenant.assignedRoomKey != null) {
                        occupancyMap.put(tenant.assignedRoomKey, occupancyMap.getOrDefault(tenant.assignedRoomKey, 0) + 1);
                    }
                }

                for (RoomModel room : roomList) {
                    int calculatedOccupancy = occupancyMap.getOrDefault(room.getRoomId(), 0);
                    if (room.getOccupied() != calculatedOccupancy) {
                        roomsRef.child(room.getRoomId()).child("occupied").setValue(calculatedOccupancy);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to sync occupancy", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddEditDialog(final RoomModel editRoom) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_room, null);
        builder.setView(dialogView);
        // Use the correct AlertDialog class
        final AlertDialog dialog = builder.create();

        final EditText etName = dialogView.findViewById(R.id.etRoomName);
        final AutoCompleteTextView spinnerType = dialogView.findViewById(R.id.spinnerType);
        final CheckBox cbFamily = dialogView.findViewById(R.id.cbFamily);
        final CheckBox cbStudents = dialogView.findViewById(R.id.cbStudents);
        final EditText etCapacity = dialogView.findViewById(R.id.etCapacity);
        final EditText etRent = dialogView.findViewById(R.id.etRent);
        final EditText etDeposit = dialogView.findViewById(R.id.etDeposit);
        final EditText etMaintenance = dialogView.findViewById(R.id.etMaintenance);
        final EditText etNotes = dialogView.findViewById(R.id.etNotes);
        final Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String[] types = {"1BHK", "2BHK", "1RK", "1R", "Dormitory"};
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        spinnerType.setAdapter(spAdapter);

        final String originalRoomName = (editRoom != null) ? editRoom.getName() : null;

        if (editRoom != null) {
            etName.setText(editRoom.getName());
            spinnerType.setText(editRoom.getType(), false);
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
            cbFamily.setChecked(true);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            String name = etName.getText().toString().trim();
            String type = spinnerType.getText().toString().trim();
            boolean family = cbFamily.isChecked();
            boolean students = cbStudents.isChecked();
            String sCapacity = etCapacity.getText().toString().trim();
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(type) || TextUtils.isEmpty(sCapacity) || (!family && !students)) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
                btnSave.setText("Save");
                return;
            }

            boolean isNameChanged = editRoom == null || !name.equals(originalRoomName);
            if (isNameChanged) {
                roomsRef.child(name).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            etName.setError("Room name already exists");
                            btnSave.setEnabled(true);
                            btnSave.setText("Save");
                        } else {
                            saveRoomData(dialog, btnSave, editRoom);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking room name.", Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText("Save");
                    }
                });
            } else {
                saveRoomData(dialog, btnSave, editRoom);
            }
        });

        dialog.show();
    }

    // This is the single, correct version of the saveRoomData method
    private void saveRoomData(AlertDialog dialog, Button btnSave, RoomModel editRoom) {
        View dialogView = Objects.requireNonNull(dialog.getWindow()).getDecorView();
        String name = ((EditText) dialogView.findViewById(R.id.etRoomName)).getText().toString().trim();
        String type = ((AutoCompleteTextView) dialogView.findViewById(R.id.spinnerType)).getText().toString().trim();
        boolean family = ((CheckBox) dialogView.findViewById(R.id.cbFamily)).isChecked();
        boolean students = ((CheckBox) dialogView.findViewById(R.id.cbStudents)).isChecked();
        String sCapacity = ((EditText) dialogView.findViewById(R.id.etCapacity)).getText().toString().trim();
        String sRent = ((EditText) dialogView.findViewById(R.id.etRent)).getText().toString().trim();
        String sDeposit = ((EditText) dialogView.findViewById(R.id.etDeposit)).getText().toString().trim();
        String sMaintenance = ((EditText) dialogView.findViewById(R.id.etMaintenance)).getText().toString().trim();
        String notes = ((EditText) dialogView.findViewById(R.id.etNotes)).getText().toString().trim();

        try {
            int capacity = Integer.parseInt(sCapacity);
            if (capacity <= 0) {
                Toast.makeText(getContext(), "Capacity must be > 0", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true); btnSave.setText("Save"); return;
            }

            int rent = TextUtils.isEmpty(sRent) ? 0 : Integer.parseInt(sRent);
            int deposit = TextUtils.isEmpty(sDeposit) ? 0 : Integer.parseInt(sDeposit);
            int maintenance = TextUtils.isEmpty(sMaintenance) ? 0 : Integer.parseInt(sMaintenance);
            String allowedFor = family && students ? "Family,Students" : (family ? "Family" : "Students");
            int occupied = (editRoom != null) ? editRoom.getOccupied() : 0;
            String originalRoomName = (editRoom != null) ? editRoom.getName() : null;

            RoomModel room = new RoomModel(name, name, type, allowedFor, capacity, occupied, rent, deposit, maintenance, notes, System.currentTimeMillis());

            roomsRef.child(name).setValue(room).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (editRoom != null && !Objects.equals(name, originalRoomName)) {
                        roomsRef.child(originalRoomName).removeValue();
                        updateTenantsRoomKey(originalRoomName, name, null);
                    }
                    Toast.makeText(getContext(), "Room saved successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to save room.", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true); btnSave.setText("Save");
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
            btnSave.setEnabled(true); btnSave.setText("Save");
        }
    }

    private void updateTenantsRoomKey(String oldRoomKey, String newRoomKey, Runnable onComplete) {
        tenantsRef.orderByChild("assignedRoomKey").equalTo(oldRoomKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) { if (onComplete != null) onComplete.run(); return; }
                Map<String, Object> updates = new HashMap<>();
                for (DataSnapshot tenantSnapshot : snapshot.getChildren()) {
                    updates.put(tenantSnapshot.getKey() + "/assignedRoomKey", newRoomKey);
                    updates.put(tenantSnapshot.getKey() + "/roomNumber", newRoomKey);
                }
                tenantsRef.updateChildren(updates).addOnCompleteListener(task -> { if (onComplete != null) onComplete.run(); });
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { /* Handle error */ }
        });
    }

    private void confirmDelete(RoomModel room) {
        tenantsRef.orderByChild("assignedRoomKey").equalTo(room.getRoomId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasActiveTenants = false;
                for (DataSnapshot tenantSnapshot : snapshot.getChildren()) {
                    Tenant tenant = tenantSnapshot.getValue(Tenant.class);
                    if (tenant != null && "Active".equalsIgnoreCase(tenant.status)) {
                        hasActiveTenants = true;
                        break;
                    }
                }

                if (hasActiveTenants) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Cannot Delete Room")
                            .setMessage("This room has active tenants. Please move them or mark them as 'Left' first.")
                            .setPositiveButton("OK", null)
                            .show();
                } else {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Room")
                            .setMessage("Are you sure you want to permanently delete '" + room.getName() + "'? This cannot be undone.")
                            .setPositiveButton("Delete", (d, w) -> roomsRef.child(room.getRoomId()).removeValue()
                                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Room deleted", Toast.LENGTH_SHORT).show()))
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
