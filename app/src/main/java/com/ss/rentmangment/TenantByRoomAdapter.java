package com.ss.rentmangment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class TenantByRoomAdapter extends RecyclerView.Adapter<TenantByRoomAdapter.RoomViewHolder> {

    private Context context;
    private List<String> roomKeys;
    private Map<String, List<Tenant>> roomTenantMap;
    private Map<String, String> roomNameMap;

    public TenantByRoomAdapter(Context context, List<String> roomKeys,
                               Map<String, List<Tenant>> roomTenantMap,
                               Map<String, String> roomNameMap) {
        this.context = context;
        this.roomKeys = roomKeys;
        this.roomTenantMap = roomTenantMap;
        this.roomNameMap = roomNameMap;
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rooom, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        String roomKey = roomKeys.get(position);
        List<Tenant> tenants = roomTenantMap.get(roomKey);
        String roomName = roomNameMap.get(roomKey);

        if (roomName == null) roomName = "Room " + (position + 1);

        // Set room header
        holder.tvRoomName.setText(roomName);
        holder.tvTenantCount.setText(tenants.size() + " tenant(s)");

        // Setup nested RecyclerView for tenants
        TenantAdapter tenantAdapter = new TenantAdapter(context, tenants);
        holder.recyclerTenants.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerTenants.setAdapter(tenantAdapter);
        holder.recyclerTenants.setNestedScrollingEnabled(false);

        // Determine tenant type for display
        String tenantType = "Mixed";
        if (!tenants.isEmpty()) {
            Tenant firstTenant = tenants.get(0);
            // Check if it's a family (has emergency contact or high deposit)
            boolean isFamily = (firstTenant.emergencyContactName != null &&
                    !firstTenant.emergencyContactName.isEmpty()) ||
                    (firstTenant.securityDeposit >= firstTenant.rentAmount * 2);

            if (isFamily && tenants.size() == 1) {
                tenantType = "🏠 Family Room";
            } else if (!isFamily) {
                tenantType = "🎓 Student Room";
            }
        }

        holder.tvRoomType.setText(tenantType);
    }

    @Override
    public int getItemCount() {
        return roomKeys.size();
    }

    public void updateData(List<String> roomKeys, Map<String, List<Tenant>> roomTenantMap,
                           Map<String, String> roomNameMap) {
        this.roomKeys = roomKeys;
        this.roomTenantMap = roomTenantMap;
        this.roomNameMap = roomNameMap;
        notifyDataSetChanged();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvTenantCount, tvRoomType;
        RecyclerView recyclerTenants;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTenantCount = itemView.findViewById(R.id.tvTenantCount);
            tvRoomType = itemView.findViewById(R.id.tvRoomType);
            recyclerTenants = itemView.findViewById(R.id.recyclerTenants);
        }
    }
}


