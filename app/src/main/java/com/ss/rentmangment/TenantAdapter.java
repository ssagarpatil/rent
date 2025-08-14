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
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    private Context context;
    private List<Tenant> tenantsList; // The list we'll display
    private String adminMobile;

    public TenantAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantsList = tenantList != null ? new ArrayList<>(tenantList) : new ArrayList<>();

        if (context != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            this.adminMobile = sharedPreferences.getString("mobile", "");
        }

        Log.d("TenantAdapter", "Created with " + this.tenantsList.size() + " tenants");
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        if (position >= tenantsList.size()) {
            Log.e("TenantAdapter", "Position " + position + " is out of bounds for list size " + tenantsList.size());
            return;
        }

        Tenant tenant = tenantsList.get(position);
        Log.d("TenantAdapter", "Binding tenant: " + tenant.name + " at position " + position);

        holder.bind(tenant, position);
    }

    @Override
    public int getItemCount() {
        Log.d("TenantAdapter", "getItemCount called: " + tenantsList.size());
        return tenantsList.size();
    }

    /**
     * Update the list with new data
     */
    public void updateList(List<Tenant> newList) {
        Log.d("TenantAdapter", "updateList called with " + (newList != null ? newList.size() : 0) + " tenants");

        this.tenantsList = newList != null ? new ArrayList<>(newList) : new ArrayList<>();

        // Log each tenant being added
        for (int i = 0; i < tenantsList.size(); i++) {
            Tenant tenant = tenantsList.get(i);
            Log.d("TenantAdapter", "Tenant " + (i+1) + ": " + tenant.name +
                    " (Type: " + tenant.tenantType + ")");
        }

        notifyDataSetChanged();
        Log.d("TenantAdapter", "notifyDataSetChanged called. Final count: " + tenantsList.size());
    }

    /**
     * Determines if a tenant is a family
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

    /**
     * ViewHolder class
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

            // Build room text
            String roomText = "Room: ";
            if (tenant.assignedRoomName != null && !tenant.assignedRoomName.isEmpty()) {
                roomText += tenant.assignedRoomName;
            } else if (tenant.roomNumber != null && !tenant.roomNumber.isEmpty()) {
                roomText += tenant.roomNumber;
            } else {
                roomText += "Not Assigned";
            }

            // Determine tenant type and add indicator
            boolean isFamily = isTenantFamily(tenant);
            if (isFamily) {
                roomText += " (🏠 Family)";
                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_light));
            } else {
                roomText += " (🎓 Student)";
                ivPhoto.setBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_light));
            }

            tvRoom.setText(roomText);
            tvPhone.setText(tenant.mobile != null ? tenant.mobile : "No mobile");

            // Set click listeners
            itemView.setOnClickListener(v -> showOptionsMenu(v, tenant, position));
            itemView.setOnLongClickListener(v -> {
                showOptionsMenu(v, tenant, position);
                return true;
            });
        }
    }

    // Rest of the methods (showOptionsMenu, editTenant, deleteTenant, etc.)
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
                .setPositiveButton("Delete", (dialog, which) -> performDelete(tenant, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performDelete(Tenant tenant, int position) {
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
                tenantsList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tenantsList.size());
                Toast.makeText(context, "Tenant deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error deleting tenant", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
