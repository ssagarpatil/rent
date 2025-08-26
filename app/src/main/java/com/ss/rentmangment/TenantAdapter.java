//package com.ss.rentmangment;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {
//
//    private Context context;
//    private List<Tenant> tenantsList; // The list we'll display
//    private String adminMobile;
//
//    public TenantAdapter(Context context, List<Tenant> tenantList) {
//        this.context = context;
//        this.tenantsList = tenantList != null ? new ArrayList<>(tenantList) : new ArrayList<>();
//
//        if (context != null) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//            this.adminMobile = sharedPreferences.getString("mobile", "");
//        }
//
//        Log.d("TenantAdapter", "Created with " + this.tenantsList.size() + " tenants");
//    }
//
//    @NonNull
//    @Override
//    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
//        return new TenantViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
//        if (position >= tenantsList.size()) {
//            Log.e("TenantAdapter", "Position " + position + " is out of bounds for list size " + tenantsList.size());
//            return;
//        }
//
//        Tenant tenant = tenantsList.get(position);
//        Log.d("TenantAdapter", "Binding tenant: " + tenant.name + " at position " + position);
//
//        holder.bind(tenant, position);
//    }
//
//    @Override
//    public int getItemCount() {
//        Log.d("TenantAdapter", "getItemCount called: " + tenantsList.size());
//        return tenantsList.size();
//    }
//
//    /**
//     * Update the list with new data
//     */
//    public void updateList(List<Tenant> newList) {
//        Log.d("TenantAdapter", "updateList called with " + (newList != null ? newList.size() : 0) + " tenants");
//
//        this.tenantsList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();
//
//        // Log each tenant being added
//        for (int i = 0; i < tenantsList.size(); i++) {
//            Tenant tenant = tenantsList.get(i);
//            Log.d("TenantAdapter", "Tenant " + (i+1) + ": " + tenant.name +
//                    " (Type: " + tenant.tenantType + ")");
//        }
//
//        notifyDataSetChanged();
//        Log.d("TenantAdapter", "notifyDataSetChanged called. Final count: " + tenantsList.size());
//    }
//
//    /**
//     * Determines if a tenant is a family
//     */
//    private boolean isTenantFamily(Tenant tenant) {
//        if (tenant == null) return false;
//
//        if (tenant.tenantType != null && !tenant.tenantType.trim().isEmpty()) {
//            String type = tenant.tenantType.trim();
//            return "Family".equalsIgnoreCase(type);
//        }
//
//        // Fallback logic
//        boolean hasEmergencyContact = tenant.emergencyContactName != null &&
//                !tenant.emergencyContactName.trim().isEmpty();
//        boolean hasHighDeposit = tenant.securityDeposit > 0 && tenant.rentAmount > 0 &&
//                tenant.securityDeposit >= (tenant.rentAmount * 2);
//
//        return hasEmergencyContact || hasHighDeposit;
//    }
//
//    /**
//     * ViewHolder class
//     */
//    public class TenantViewHolder extends RecyclerView.ViewHolder {
//        TextView tvName, tvRoom, tvPhone;
//        ImageView ivPhoto;
//
//        TenantViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tvTenantName);
//            tvRoom = itemView.findViewById(R.id.tvTenantRoom);
//            tvPhone = itemView.findViewById(R.id.tvTenantPhone);
//            ivPhoto = itemView.findViewById(R.id.ivTenantPhoto);
//        }
//
//        public void bind(Tenant tenant, int position) {
//            // Set tenant name
//            tvName.setText(tenant.name != null ? tenant.name : "Unknown");
//
//            // Build room text
//            String roomText = "Room: ";
//            if (tenant.assignedRoomName != null && !tenant.assignedRoomName.isEmpty()) {
//                roomText += tenant.assignedRoomName;
//            } else if (tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
//                roomText += tenant.roomNumber;
//            } else {
//                roomText += "Not Assigned";
//            }
//
//            // Determine tenant type and add indicator
//            boolean isFamily = isTenantFamily(tenant);
//            if (isFamily) {
//                roomText += " (🏠 Family)";
//                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
//            } else {
//                roomText += " (🎓 Student)";
//                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
//            }
//
//            tvRoom.setText(roomText);
//            tvPhone.setText(tenant.mobile != null ? tenant.mobile : "No mobile");
//
//            // Set click listeners
//            itemView.setOnClickListener(v -> showOptionsMenu(v, tenant, position));
//            itemView.setOnLongClickListener(v -> {
//                showOptionsMenu(v, tenant, position);
//                return true;
//            });
//        }
//    }
//
//    // Rest of the methods (showOptionsMenu, editTenant, deleteTenant, etc.)
//    private void showOptionsMenu(View view, Tenant tenant, int position) {
//        PopupMenu popupMenu = new PopupMenu(context, view);
//        popupMenu.getMenu().add(0, 1, 0, "Edit");
//        popupMenu.getMenu().add(0, 2, 0, "Delete");
//
//        popupMenu.setOnMenuItemClickListener(item -> {
//            switch (item.getItemId()) {
//                case 1:
//                    editTenant(tenant);
//                    return true;
//                case 2:
//                    deleteTenant(tenant, position);
//                    return true;
//                default:
//                    return false;
//            }
//        });
//        popupMenu.show();
//    }
//
//    private void editTenant(Tenant tenant) {
//        Intent intent = new Intent(context, AddTenantActivity.class);
//        intent.putExtra("tenant_mobile", tenant.mobile);
//        intent.putExtra("edit_mode", true);
//        context.startActivity(intent);
//    }
//
//    private void deleteTenant(Tenant tenant, int position) {
//        String tenantTypeDisplay = isTenantFamily(tenant) ? "🏠 Family" : "🎓 Student";
//
//        new AlertDialog.Builder(context)
//                .setTitle("Delete Tenant")
//                .setMessage("Are you sure you want to delete this tenant?\n\n" +
//                        "Name: " + tenant.name + "\n" +
//                        "Mobile: " + tenant.mobile + "\n" +
//                        "Type: " + tenantTypeDisplay + "\n" +
//                        "Room: " + (tenant.assignedRoomName != null ? tenant.assignedRoomName : "Not Assigned"))
//                .setPositiveButton("Delete", (dialog, which) -> performDelete(tenant, position))
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void performDelete(Tenant tenant, int position) {
//        if (adminMobile == null || adminMobile.isEmpty()) {
//            Toast.makeText(context, "Error: Admin mobile not found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        AlertDialog progressDialog = new AlertDialog.Builder(context)
//                .setTitle("Deleting Tenant")
//                .setMessage("Please wait...")
//                .setCancelable(false)
//                .create();
//        progressDialog.show();
//
//        DatabaseReference tenantRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(adminMobile)
//                .child("tenants")
//                .child(tenant.mobile);
//
//        tenantRef.removeValue().addOnCompleteListener(task -> {
//            progressDialog.dismiss();
//            if (task.isSuccessful()) {
//                tenantsList.remove(position);
//                notifyItemRemoved(position);
//                notifyItemRangeChanged(position, tenantsList.size());
//                Toast.makeText(context, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Error deleting tenant", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}
//package com.ss.rentmangment;
//
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.PopupMenu;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//public class TenantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    // View type constants
//    private static final int TYPE_ROOM_HEADER = 0;
//    private static final int TYPE_TENANT = 1;
//
//    private Context context;
//    private List<Object> displayItemsList; // Contains both headers and tenants
//    private String adminMobile;
//    private boolean isStudentTab = false; // Only group for students
//
//    public TenantAdapter(Context context, List<Tenant> tenantList) {
//        this.context = context;
//
//        if (context != null) {
//            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//            this.adminMobile = sharedPreferences.getString("mobile", "");
//        }
//
//        updateList(tenantList);
//        Log.d("TenantAdapter", "Created with " + (tenantList != null ? tenantList.size() : 0) + " tenants");
//    }
//
//    /**
//     * Set whether this adapter is for students tab (to enable room grouping)
//     */
//    public void setStudentMode(boolean isStudentTab) {
//        this.isStudentTab = isStudentTab;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        if (position < displayItemsList.size()) {
//            Object item = displayItemsList.get(position);
//            if (item instanceof RoomHeader) {
//                return TYPE_ROOM_HEADER;
//            }
//        }
//        return TYPE_TENANT;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        if (viewType == TYPE_ROOM_HEADER) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_room_header, parent, false);
//            return new RoomHeaderViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
//            return new TenantViewHolder(view);
//        }
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        if (position >= displayItemsList.size()) {
//            Log.e("TenantAdapter", "Position out of bounds: " + position);
//            return;
//        }
//
//        Object item = displayItemsList.get(position);
//
//        if (holder instanceof RoomHeaderViewHolder && item instanceof RoomHeader) {
//            ((RoomHeaderViewHolder) holder).bind((RoomHeader) item);
//        } else if (holder instanceof TenantViewHolder && item instanceof Tenant) {
//            ((TenantViewHolder) holder).bind((Tenant) item, position);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return displayItemsList.size();
//    }
//
//    /**
//     * Update the list with new data
//     */
//    public void updateList(List<Tenant> newList) {
//        Log.d("TenantAdapter", "updateList called with " + (newList != null ? newList.size() : 0) + " tenants, isStudentTab: " + isStudentTab);
//
//        displayItemsList = new ArrayList<>();
//
//        if (newList == null || newList.isEmpty()) {
//            notifyDataSetChanged();
//            return;
//        }
//
//        if (isStudentTab) {
//            // Group by room for students only
//            createGroupedList(newList);
//        } else {
//            // Simple list for families (no grouping)
//            displayItemsList.addAll(newList);
//        }
//
//        notifyDataSetChanged();
//        Log.d("TenantAdapter", "Final display list size: " + displayItemsList.size());
//    }
//
//    /**
//     * Create grouped list by room numbers (ONLY for students)
//     */
//    private void createGroupedList(List<Tenant> tenants) {
//        // Group tenants by room
//        Map<String, List<Tenant>> roomGroups = new LinkedHashMap<>();
//
//        for (Tenant tenant : tenants) {
//            String roomKey = getRoomKey(tenant);
//
//            if (!roomGroups.containsKey(roomKey)) {
//                roomGroups.put(roomKey, new ArrayList<>());
//            }
//            roomGroups.get(roomKey).add(tenant);
//        }
//
//        // Sort room keys naturally (Room 1, Room 2, etc.)
//        List<String> sortedRoomKeys = new ArrayList<>(roomGroups.keySet());
//        Collections.sort(sortedRoomKeys, new RoomComparator());
//
//        // Build display list with headers and tenants
//        for (String roomKey : sortedRoomKeys) {
//            List<Tenant> roomTenants = roomGroups.get(roomKey);
//
//            // Add room header
//            RoomHeader header = new RoomHeader(roomKey, roomTenants.size());
//            displayItemsList.add(header);
//
//            // Sort tenants within room by name
//            Collections.sort(roomTenants, (t1, t2) -> {
//                String name1 = t1.name != null ? t1.name : "";
//                String name2 = t2.name != null ? t2.name : "";
//                return name1.compareToIgnoreCase(name2);
//            });
//
//            // Add tenants
//            displayItemsList.addAll(roomTenants);
//
//            Log.d("TenantAdapter", "Room: " + roomKey + " has " + roomTenants.size() + " students");
//        }
//    }
//
//    /**
//     * Get room key for grouping
//     */
//    private String getRoomKey(Tenant tenant) {
//        if (tenant.assignedRoomName != null && !tenant.assignedRoomName.trim().isEmpty()) {
//            return tenant.assignedRoomName.trim();
//        } else if (tenant.roomNumber != null && !tenant.roomNumber.trim().isEmpty()) {
//            return tenant.roomNumber.trim();
//        } else {
//            return "Unassigned Room";
//        }
//    }
//
//    /**
//     * Room header class
//     */
//    public static class RoomHeader {
//        public String roomName;
//        public int tenantCount;
//
//        public RoomHeader(String roomName, int tenantCount) {
//            this.roomName = roomName;
//            this.tenantCount = tenantCount;
//        }
//    }
//
//    /**
//     * Custom comparator for room sorting
//     */
//    private static class RoomComparator implements Comparator<String> {
//        @Override
//        public int compare(String room1, String room2) {
//            // Handle "Unassigned Room" case
//            if (room1.equals("Unassigned Room") && !room2.equals("Unassigned Room")) {
//                return 1; // Unassigned comes last
//            }
//            if (room2.equals("Unassigned Room") && !room1.equals("Unassigned Room")) {
//                return -1; // Unassigned comes last
//            }
//            if (room1.equals("Unassigned Room") && room2.equals("Unassigned Room")) {
//                return 0;
//            }
//
//            // Extract numbers from room names for natural sorting
//            int num1 = extractRoomNumber(room1);
//            int num2 = extractRoomNumber(room2);
//
//            if (num1 != -1 && num2 != -1) {
//                return Integer.compare(num1, num2);
//            }
//
//            // Fallback to string comparison
//            return room1.compareToIgnoreCase(room2);
//        }
//
//        private int extractRoomNumber(String roomName) {
//            try {
//                // Extract numbers from room name (e.g., "Room 1" -> 1, "R-5" -> 5)
//                String numbers = roomName.replaceAll("[^0-9]", "");
//                if (!numbers.isEmpty()) {
//                    return Integer.parseInt(numbers);
//                }
//            } catch (NumberFormatException e) {
//                // Ignore
//            }
//            return -1;
//        }
//    }
//
//    /**
//     * Room Header ViewHolder (ONLY used for students)
//     */
//    public static class RoomHeaderViewHolder extends RecyclerView.ViewHolder {
//        private TextView tvRoomName;
//        private TextView tvTenantCount;
//
//        public RoomHeaderViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvRoomName = itemView.findViewById(R.id.tvRoomName);
//            tvTenantCount = itemView.findViewById(R.id.tvTenantCount);
//        }
//
//        public void bind(RoomHeader header) {
//            tvRoomName.setText(header.roomName);
//            tvTenantCount.setText(header.tenantCount + " student" + (header.tenantCount == 1 ? "" : "s"));
//        }
//    }
//
//    /**
//     * Tenant ViewHolder (works for both students and families)
//     */
//    public class TenantViewHolder extends RecyclerView.ViewHolder {
//        TextView tvName, tvRoom, tvPhone;
//        ImageView ivPhoto;
//
//        TenantViewHolder(@NonNull View itemView) {
//            super(itemView);
//            tvName = itemView.findViewById(R.id.tvTenantName);
//            tvRoom = itemView.findViewById(R.id.tvTenantRoom);
//            tvPhone = itemView.findViewById(R.id.tvTenantPhone);
//            ivPhoto = itemView.findViewById(R.id.ivTenantPhoto);
//        }
//
//        public void bind(Tenant tenant, int position) {
//            // Set tenant name
//            tvName.setText(tenant.name != null ? tenant.name : "Unknown");
//
//            // Determine tenant type
//            boolean isFamily = isTenantFamily(tenant);
//
//            // Build room text based on mode and tenant type
//            String roomText;
//            if (isStudentTab && !isFamily) {
//                // For students in grouped view, simplified display
//                roomText = "🎓 Student";
//            } else {
//                // For families or ungrouped view, show full room info
//                roomText = "Room: ";
//                if (tenant.assignedRoomName != null && !tenant.assignedRoomName.isEmpty()) {
//                    roomText += tenant.assignedRoomName;
//                } else if (tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
//                    roomText += tenant.roomNumber;
//                } else {
//                    roomText += "Not Assigned";
//                }
//
//                if (isFamily) {
//                    roomText += " (🏠 Family)";
//                } else {
//                    roomText += " (🎓 Student)";
//                }
//            }
//
//            tvRoom.setText(roomText);
//            tvPhone.setText(tenant.mobile != null ? tenant.mobile : "No mobile");
//
//            // Set styling based on tenant type
//            if (isFamily) {
//                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
//            } else {
//                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
//            }
//
//            // Set click listeners
//            itemView.setOnClickListener(v -> showOptionsMenu(v, tenant, position));
//            itemView.setOnLongClickListener(v -> {
//                showOptionsMenu(v, tenant, position);
//                return true;
//            });
//        }
//    }
//
//    /**
//     * Determines if a tenant is a family (UNCHANGED)
//     */
//    private boolean isTenantFamily(Tenant tenant) {
//        if (tenant == null) return false;
//
//        if (tenant.tenantType != null && !tenant.tenantType.trim().isEmpty()) {
//            String type = tenant.tenantType.trim();
//            return "Family".equalsIgnoreCase(type);
//        }
//
//        // Fallback logic
//        boolean hasEmergencyContact = tenant.emergencyContactName != null &&
//                !tenant.emergencyContactName.trim().isEmpty();
//        boolean hasHighDeposit = tenant.securityDeposit > 0 && tenant.rentAmount > 0 &&
//                tenant.securityDeposit >= (tenant.rentAmount * 2);
//
//        return hasEmergencyContact || hasHighDeposit;
//    }
//
//    // All existing methods remain UNCHANGED
//    private void showOptionsMenu(View view, Tenant tenant, int position) {
//        PopupMenu popupMenu = new PopupMenu(context, view);
//        popupMenu.getMenu().add(0, 1, 0, "Edit");
//        popupMenu.getMenu().add(0, 2, 0, "Delete");
//
//        popupMenu.setOnMenuItemClickListener(item -> {
//            switch (item.getItemId()) {
//                case 1:
//                    editTenant(tenant);
//                    return true;
//                case 2:
//                    deleteTenant(tenant, position);
//                    return true;
//                default:
//                    return false;
//            }
//        });
//        popupMenu.show();
//    }
//
//    private void editTenant(Tenant tenant) {
//        Intent intent = new Intent(context, AddTenantActivity.class);
//        intent.putExtra("tenant_mobile", tenant.mobile);
//        intent.putExtra("edit_mode", true);
//        context.startActivity(intent);
//    }
//
//    private void deleteTenant(Tenant tenant, int position) {
//        String tenantTypeDisplay = isTenantFamily(tenant) ? "🏠 Family" : "🎓 Student";
//
//        new AlertDialog.Builder(context)
//                .setTitle("Delete Tenant")
//                .setMessage("Are you sure you want to delete this tenant?\n\n" +
//                        "Name: " + tenant.name + "\n" +
//                        "Mobile: " + tenant.mobile + "\n" +
//                        "Type: " + tenantTypeDisplay + "\n" +
//                        "Room: " + (tenant.assignedRoomName != null ? tenant.assignedRoomName : "Not Assigned"))
//                .setPositiveButton("Delete", (dialog, which) -> performDelete(tenant))
//                .setNegativeButton("Cancel", null)
//                .show();
//    }
//
//    private void performDelete(Tenant tenant) {
//        if (adminMobile == null || adminMobile.isEmpty()) {
//            Toast.makeText(context, "Error: Admin mobile not found", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        AlertDialog progressDialog = new AlertDialog.Builder(context)
//                .setTitle("Deleting Tenant")
//                .setMessage("Please wait...")
//                .setCancelable(false)
//                .create();
//        progressDialog.show();
//
//        DatabaseReference tenantRef = FirebaseDatabase.getInstance()
//                .getReference("users")
//                .child(adminMobile)
//                .child("tenants")
//                .child(tenant.mobile);
//
//        tenantRef.removeValue().addOnCompleteListener(task -> {
//            progressDialog.dismiss();
//            if (task.isSuccessful()) {
//                Toast.makeText(context, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
//                // Parent fragment handles refresh
//            } else {
//                Toast.makeText(context, "Error deleting tenant", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//}

package com.ss.rentmangment;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TenantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_TENANT = 1;

    private final Context context;
    private final String adminMobile;
    private final boolean isStudentMode;
    private List<Object> displayList = new ArrayList<>();

    public TenantAdapter(Context context, List<Tenant> tenants, boolean isStudentMode) {
        this.context = context;
        this.isStudentMode = isStudentMode;
        this.adminMobile = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                .getString("mobile", "");
        setData(tenants);
    }

    public void setData(List<Tenant> tenants) {
        displayList.clear();
        if (isStudentMode) {
            Map<String, List<Tenant>> groupedByRoom = new LinkedHashMap<>();
            for (Tenant tenant : tenants) {
                if ("Active".equalsIgnoreCase(tenant.status)) { // Only show active tenants in student mode
                    String roomName = tenant.roomNumber != null ? "Room " + tenant.roomNumber : "Unassigned";
                    groupedByRoom.computeIfAbsent(roomName, k -> new ArrayList<>()).add(tenant);
                }
            }
            List<String> sortedRoomNames = new ArrayList<>(groupedByRoom.keySet());
            Collections.sort(sortedRoomNames, new RoomNameComparator());
            for (String roomName : sortedRoomNames) {
                List<Tenant> roomTenants = groupedByRoom.get(roomName);
                if (roomTenants != null) {
                    displayList.add(new RoomHeader(roomName, roomTenants.size()));
                    roomTenants.sort(Comparator.comparing(t -> t.name));
                    displayList.addAll(roomTenants);
                }
            }
        } else {
            // For non-student mode (e.g., family view), just add all tenants
            displayList.addAll(tenants);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return displayList.get(position) instanceof RoomHeader ? VIEW_TYPE_HEADER : VIEW_TYPE_TENANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_room_header, parent, false);
            return new RoomHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_tenant, parent, false);
            return new TenantViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            ((RoomHeaderViewHolder) holder).bind((RoomHeader) displayList.get(position));
        } else {
            ((TenantViewHolder) holder).bind((Tenant) displayList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDetails;
        ImageView ivIcon, ivOptions;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTenantName);
            tvDetails = itemView.findViewById(R.id.tvTenantDetails);
            ivIcon = itemView.findViewById(R.id.ivTenantIcon);
            ivOptions = itemView.findViewById(R.id.ivTenantOptions);
        }

        void bind(Tenant tenant) {
            tvName.setText(tenant.name);
            tvDetails.setText("Mobile: " + tenant.mobile);

            if ("Family".equalsIgnoreCase(tenant.tenantType)) {
                ivIcon.setImageResource(R.drawable.ic_family);
            } else {
                ivIcon.setImageResource(R.drawable.ic_student);
            }

            ivOptions.setOnClickListener(v -> showPopupMenu(v, tenant));
            itemView.setOnClickListener(v -> ivOptions.performClick());
        }
    }

    static class RoomHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvTenantCount;
        RoomHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTenantCount = itemView.findViewById(R.id.tvTenantCount);
        }
        void bind(RoomHeader header) {
            tvRoomName.setText(header.roomName);
            tvTenantCount.setText(header.count + (header.count == 1 ? " Student" : " Students"));
        }
    }

    private void showPopupMenu(View view, Tenant tenant) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.tenant_options_menu, popup.getMenu());

        // Hide "Mark as Left" if tenant is already not active
        if (!"Active".equalsIgnoreCase(tenant.status)) {
            popup.getMenu().findItem(R.id.menu_mark_left).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                editTenant(tenant);
                return true;
            } else if (itemId == R.id.menu_mark_left) {
                confirmMarkAsLeft(tenant);
                return true;
            } else if (itemId == R.id.menu_delete) {
                confirmDelete(tenant);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void editTenant(Tenant tenant) {
        Intent intent = new Intent(context, AddTenantActivity.class);
        intent.putExtra("tenant_mobile_key", tenant.mobile);
        context.startActivity(intent);
    }

    private void confirmMarkAsLeft(Tenant tenant) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Confirm Action")
                .setMessage("Mark '" + tenant.name + "' as left? This will make their spot in the room available and archive the tenant record.")
                .setPositiveButton("Mark as Left", (dialog, which) -> markTenantAsLeft(tenant))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // --- THIS IS THE CORRECTED AND FINAL METHOD ---
    private void markTenantAsLeft(Tenant tenant) {
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("users")
                .child(adminMobile).child("tenants").child(tenant.mobile);

        // Get the current date to store as the official leave date.
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.US);
        String leaveDate = sdf.format(new Date());

        // Create a Map to update both status and leaseEndDate atomically.
        // This is the correct way to update multiple fields at once.
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "Left");
        updates.put("leaseEndDate", leaveDate);

        // Perform the atomic update.
        tenantRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Now, safely update the room's occupancy count.
                updateRoomOccupancy(tenant.assignedRoomKey, false); // false = decrement
                Toast.makeText(context, tenant.name + " marked as left.", Toast.LENGTH_SHORT).show();
                // You will need to refresh the tenant list in your fragment/activity after this.
            } else {
                Toast.makeText(context, "Operation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("TenantAdapter", "Failed to mark tenant as left", task.getException());
            }
        });
    }


    private void confirmDelete(Tenant tenant) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Tenant Permanently?")
                .setMessage("This action is irreversible. Are you sure you want to delete all data for '" + tenant.name + "'?")
                .setPositiveButton("Delete Permanently", (dialog, which) -> deleteTenantPermanently(tenant))
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete_forever)
                .show();
    }

    private void deleteTenantPermanently(Tenant tenant) {
        DatabaseReference tenantRef = FirebaseDatabase.getInstance().getReference("users")
                .child(adminMobile).child("tenants").child(tenant.mobile);

        tenantRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update the room's occupancy if the tenant was active.
                if ("Active".equalsIgnoreCase(tenant.status)) {
                    updateRoomOccupancy(tenant.assignedRoomKey, false);
                }
                Toast.makeText(context, "Tenant deleted permanently.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Delete failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRoomOccupancy(String roomKey, boolean increment) {
        if (roomKey == null || roomKey.trim().isEmpty()) return;

        DatabaseReference roomOccupancyRef = FirebaseDatabase.getInstance().getReference("users")
                .child(adminMobile).child("rooms").child(roomKey).child("occupied");

        roomOccupancyRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Integer currentOccupancy = mutableData.getValue(Integer.class);
                if (currentOccupancy == null) {
                    return Transaction.success(mutableData);
                }
                if (increment) {
                    mutableData.setValue(currentOccupancy + 1);
                } else {
                    mutableData.setValue(Math.max(0, currentOccupancy - 1));
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e("TenantAdapter", "Room occupancy transaction failed.", error.toException());
                }
            }
        });
    }

    private static class RoomHeader {
        final String roomName;
        final int count;
        RoomHeader(String roomName, int count) { this.roomName = roomName; this.count = count; }
    }

    private static class RoomNameComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            try {
                int n1 = Integer.parseInt(s1.replaceAll("\\D", ""));
                int n2 = Integer.parseInt(s2.replaceAll("\\D", ""));
                return Integer.compare(n1, n2);
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        }
    }
}



