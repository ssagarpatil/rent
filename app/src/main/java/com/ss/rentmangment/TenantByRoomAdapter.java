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

    private final Context context;
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

        if (tenants == null || tenants.isEmpty()) {
            // Hide the item if there are no tenants for this room
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        }

        holder.itemView.setVisibility(View.VISIBLE);
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (roomName == null) roomName = "Room " + (position + 1);

        holder.tvRoomName.setText(roomName);
        holder.tvTenantCount.setText(tenants.size() + (tenants.size() == 1 ? " Tenant" : " Tenants"));

        // *** FIX IS HERE ***
        // Pass 'false' for the isStudentMode parameter because we don't want nested grouping.
        TenantAdapter tenantAdapter = new TenantAdapter(context, tenants, false);

        holder.recyclerTenants.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerTenants.setAdapter(tenantAdapter);
        holder.recyclerTenants.setNestedScrollingEnabled(false);

        // Simplified logic to determine room type
        String tenantType = tenants.get(0).tenantType;
        if ("Family".equalsIgnoreCase(tenantType)) {
            holder.tvRoomType.setText("🏠 Family Room");
        } else {
            holder.tvRoomType.setText("🎓 Student Room");
        }
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
