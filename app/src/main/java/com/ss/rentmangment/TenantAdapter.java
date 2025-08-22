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
package com.ss.rentmangment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TenantAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View type constants
    private static final int TYPE_ROOM_HEADER = 0;
    private static final int TYPE_TENANT = 1;

    private Context context;
    private List<Object> displayItemsList; // Contains both headers and tenants
    private String adminMobile;
    private boolean isStudentTab = false; // Only group for students

    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;

        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            this.adminMobile = sharedPreferences.getString("mobile", "");
        }

        updateList(tenantList);
        Log.d("TenantAdapter", "Created with " + (tenantList != null ? tenantList.size() : 0) + " tenants");
    }

    /**
     * Set whether this adapter is for students tab (to enable room grouping)
     */
    public void setStudentMode(boolean isStudentTab) {
        this.isStudentTab = isStudentTab;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < displayItemsList.size()) {
            Object item = displayItemsList.get(position);
            if (item instanceof RoomHeader) {
                return TYPE_ROOM_HEADER;
            }
        }
        return TYPE_TENANT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ROOM_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_room_header, parent, false);
            return new RoomHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
            return new TenantViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= displayItemsList.size()) {
            Log.e("TenantAdapter", "Position out of bounds: " + position);
            return;
        }

        Object item = displayItemsList.get(position);

        if (holder instanceof RoomHeaderViewHolder && item instanceof RoomHeader) {
            ((RoomHeaderViewHolder) holder).bind((RoomHeader) item);
        } else if (holder instanceof TenantViewHolder && item instanceof Tenant) {
            ((TenantViewHolder) holder).bind((Tenant) item, position);
        }
    }

    @Override
    public int getItemCount() {
        return displayItemsList.size();
    }

    /**
     * Update the list with new data
     */
    public void updateList(List<Tenant> newList) {
        Log.d("TenantAdapter", "updateList called with " + (newList != null ? newList.size() : 0) + " tenants, isStudentTab: " + isStudentTab);

        displayItemsList = new ArrayList<>();

        if (newList == null || newList.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        if (isStudentTab) {
            // Group by room for students only
            createGroupedList(newList);
        } else {
            // Simple list for families (no grouping)
            displayItemsList.addAll(newList);
        }

        notifyDataSetChanged();
        Log.d("TenantAdapter", "Final display list size: " + displayItemsList.size());
    }

    /**
     * Create grouped list by room numbers (ONLY for students)
     */
    private void createGroupedList(List<Tenant> tenants) {
        // Group tenants by room
        Map<String, List<Tenant>> roomGroups = new LinkedHashMap<>();

        for (Tenant tenant : tenants) {
            String roomKey = getRoomKey(tenant);

            if (!roomGroups.containsKey(roomKey)) {
                roomGroups.put(roomKey, new ArrayList<>());
            }
            roomGroups.get(roomKey).add(tenant);
        }

        // Sort room keys naturally (Room 1, Room 2, etc.)
        List<String> sortedRoomKeys = new ArrayList<>(roomGroups.keySet());
        Collections.sort(sortedRoomKeys, new RoomComparator());

        // Build display list with headers and tenants
        for (String roomKey : sortedRoomKeys) {
            List<Tenant> roomTenants = roomGroups.get(roomKey);

            // Add room header
            RoomHeader header = new RoomHeader(roomKey, roomTenants.size());
            displayItemsList.add(header);

            // Sort tenants within room by name
            Collections.sort(roomTenants, (t1, t2) -> {
                String name1 = t1.name != null ? t1.name : "";
                String name2 = t2.name != null ? t2.name : "";
                return name1.compareToIgnoreCase(name2);
            });

            // Add tenants
            displayItemsList.addAll(roomTenants);

            Log.d("TenantAdapter", "Room: " + roomKey + " has " + roomTenants.size() + " students");
        }
    }

    /**
     * Get room key for grouping
     */
    private String getRoomKey(Tenant tenant) {
        if (tenant.assignedRoomName != null && !tenant.assignedRoomName.trim().isEmpty()) {
            return tenant.assignedRoomName.trim();
        } else if (tenant.roomNumber != null && !tenant.roomNumber.trim().isEmpty()) {
            return tenant.roomNumber.trim();
        } else {
            return "Unassigned Room";
        }
    }

    /**
     * Room header class
     */
    public static class RoomHeader {
        public String roomName;
        public int tenantCount;

        public RoomHeader(String roomName, int tenantCount) {
            this.roomName = roomName;
            this.tenantCount = tenantCount;
        }
    }

    /**
     * Custom comparator for room sorting
     */
    private static class RoomComparator implements Comparator<String> {
        @Override
        public int compare(String room1, String room2) {
            // Handle "Unassigned Room" case
            if (room1.equals("Unassigned Room") && !room2.equals("Unassigned Room")) {
                return 1; // Unassigned comes last
            }
            if (room2.equals("Unassigned Room") && !room1.equals("Unassigned Room")) {
                return -1; // Unassigned comes last
            }
            if (room1.equals("Unassigned Room") && room2.equals("Unassigned Room")) {
                return 0;
            }

            // Extract numbers from room names for natural sorting
            int num1 = extractRoomNumber(room1);
            int num2 = extractRoomNumber(room2);

            if (num1 != -1 && num2 != -1) {
                return Integer.compare(num1, num2);
            }

            // Fallback to string comparison
            return room1.compareToIgnoreCase(room2);
        }

        private int extractRoomNumber(String roomName) {
            try {
                // Extract numbers from room name (e.g., "Room 1" -> 1, "R-5" -> 5)
                String numbers = roomName.replaceAll("[^0-9]", "");
                if (!numbers.isEmpty()) {
                    return Integer.parseInt(numbers);
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
            return -1;
        }
    }

    /**
     * Room Header ViewHolder (ONLY used for students)
     */
    public static class RoomHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRoomName;
        private TextView tvTenantCount;

        public RoomHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTenantCount = itemView.findViewById(R.id.tvTenantCount);
        }

        public void bind(RoomHeader header) {
            tvRoomName.setText(header.roomName);
            tvTenantCount.setText(header.tenantCount + " student" + (header.tenantCount == 1 ? "" : "s"));
        }
    }

    /**
     * Tenant ViewHolder (works for both students and families)
     */
    public class TenantViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRoom, tvPhone;
        ImageView ivPhoto;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTenantName);
            tvRoom = itemView.findViewById(R.id.tvTenantRoom);
            tvPhone = itemView.findViewById(R.id.tvTenantPhone);
            ivPhoto = itemView.findViewById(R.id.ivTenantPhoto);
        }

        public void bind(Tenant tenant, int position) {
            // Set tenant name
            tvName.setText(tenant.name != null ? tenant.name : "Unknown");

            // Determine tenant type
            boolean isFamily = isTenantFamily(tenant);

            // Build room text based on mode and tenant type
            String roomText;
            if (isStudentTab && !isFamily) {
                // For students in grouped view, simplified display
                roomText = "🎓 Student";
            } else {
                // For families or ungrouped view, show full room info
                roomText = "Room: ";
                if (tenant.assignedRoomName != null && !tenant.assignedRoomName.isEmpty()) {
                    roomText += tenant.assignedRoomName;
                } else if (tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
                    roomText += tenant.roomNumber;
                } else {
                    roomText += "Not Assigned";
                }

                if (isFamily) {
                    roomText += " (🏠 Family)";
                } else {
                    roomText += " (🎓 Student)";
                }
            }

            tvRoom.setText(roomText);
            tvPhone.setText(tenant.mobile != null ? tenant.mobile : "No mobile");

            // Set styling based on tenant type
            if (isFamily) {
                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            } else {
                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            }

            // Set click listeners
            itemView.setOnClickListener(v -> showOptionsMenu(v, tenant, position));
            itemView.setOnLongClickListener(v -> {
                showOptionsMenu(v, tenant, position);
                return true;
            });
        }
    }

    /**
     * Determines if a tenant is a family (UNCHANGED)
     */
    private boolean isTenantFamily(Tenant tenant) {
        if (tenant == null) return false;

        if (tenant.tenantType != null && !tenant.tenantType.trim().isEmpty()) {
            String type = tenant.tenantType.trim();
            return "Family".equalsIgnoreCase(type);
        }

        // Fallback logic
        boolean hasEmergencyContact = tenant.emergencyContactName != null &&
                !tenant.emergencyContactName.trim().isEmpty();
        boolean hasHighDeposit = tenant.securityDeposit > 0 && tenant.rentAmount > 0 &&
                tenant.securityDeposit >= (tenant.rentAmount * 2);

        return hasEmergencyContact || hasHighDeposit;
    }

    // All existing methods remain UNCHANGED
    private void showOptionsMenu(View view, Tenant tenant, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenu().add(0, 1, 0, "Edit");
        popupMenu.getMenu().add(0, 2, 0, "Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    editTenant(tenant);
                    return true;
                case 2:
                    deleteTenant(tenant, position);
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void editTenant(Tenant tenant) {
        Intent intent = new Intent(context, AddTenantActivity.class);
        intent.putExtra("tenant_mobile", tenant.mobile);
        intent.putExtra("edit_mode", true);
        context.startActivity(intent);
    }

    private void deleteTenant(Tenant tenant, int position) {
        String tenantTypeDisplay = isTenantFamily(tenant) ? "🏠 Family" : "🎓 Student";

        new AlertDialog.Builder(context)
                .setTitle("Delete Tenant")
                .setMessage("Are you sure you want to delete this tenant?\n\n" +
                        "Name: " + tenant.name + "\n" +
                        "Mobile: " + tenant.mobile + "\n" +
                        "Type: " + tenantTypeDisplay + "\n" +
                        "Room: " + (tenant.assignedRoomName != null ? tenant.assignedRoomName : "Not Assigned"))
                .setPositiveButton("Delete", (dialog, which) -> performDelete(tenant))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(Tenant tenant) {
        if (adminMobile == null || adminMobile.isEmpty()) {
            Toast.makeText(context, "Error: Admin mobile not found", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog progressDialog = new AlertDialog.Builder(context)
                .setTitle("Deleting Tenant")
                .setMessage("Please wait...")
                .setCancelable(false)
                .create();
        progressDialog.show();

        DatabaseReference tenantRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(adminMobile)
                .child("tenants")
                .child(tenant.mobile);

        tenantRef.removeValue().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(context, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
                // Parent fragment handles refresh
            } else {
                Toast.makeText(context, "Error deleting tenant", Toast.LENGTH_SHORT).show();
            }
        });
    }
}