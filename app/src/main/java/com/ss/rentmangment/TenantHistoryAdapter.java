package com.ss.rentmangment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TenantHistoryAdapter extends RecyclerView.Adapter<TenantHistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<Tenant> tenantList;

    // Correctly defined date formats
    private final SimpleDateFormat dbDateFormat = new SimpleDateFormat("d/M/yyyy", Locale.US);
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    public TenantHistoryAdapter(Context context, List<Tenant> tenantList) {
        this.context = context;
        this.tenantList = tenantList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Tenant tenant = tenantList.get(position);
        holder.bind(tenant);
        holder.itemView.setOnClickListener(v -> showDetailsDialog(tenant));
    }

    @Override
    public int getItemCount() {
        return tenantList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMobile, tvRoom, tvStatus;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryTenantName);
            tvMobile = itemView.findViewById(R.id.tvHistoryTenantMobile);
            tvRoom = itemView.findViewById(R.id.tvHistoryRoom);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
        }

        public void bind(Tenant tenant) {
            tvName.setText(tenant.name);
            tvMobile.setText("Mobile: " + tenant.mobile);
            tvRoom.setText(tenant.assignedRoomName != null ? "Room: " + tenant.assignedRoomName : "Room: Unassigned");

            if ("Active".equalsIgnoreCase(tenant.status)) {
                tvStatus.setText("Active");
                tvStatus.setBackgroundResource(R.drawable.status_background_active);
            } else {
                tvStatus.setText("Left / Archived");
                tvStatus.setBackgroundResource(R.drawable.status_background_left);
            }
            tvStatus.setTextColor(Color.WHITE);
        }
    }

    private void showDetailsDialog(Tenant tenant) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_tenant_details);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextView tvDetailName = dialog.findViewById(R.id.tvDetailName);
        TextView tvDetailMobile = dialog.findViewById(R.id.tvDetailMobile);
        TextView tvDetailRoom = dialog.findViewById(R.id.tvDetailRoom);
        TextView tvDetailStatus = dialog.findViewById(R.id.tvDetailStatus);
        TextView tvDetailLeaseLabel = dialog.findViewById(R.id.tvDetailLeaseLabel);
        TextView tvDetailLease = dialog.findViewById(R.id.tvDetailLease);
        TextView tvDetailRent = dialog.findViewById(R.id.tvDetailRent);
        TextView tvDetailDeposit = dialog.findViewById(R.id.tvDetailDeposit);

        tvDetailName.setText(tenant.name);
        tvDetailMobile.setText(tenant.mobile);
        tvDetailRoom.setText(tenant.assignedRoomName != null ? tenant.assignedRoomName : "N/A");
        tvDetailStatus.setText(tenant.status);
        tvDetailRent.setText(String.format(Locale.getDefault(), "₹%.2f / month", tenant.rentAmount));
        tvDetailDeposit.setText(String.format(Locale.getDefault(), "₹%.2f", tenant.securityDeposit));

        // **THE DEFINITIVE FIX**
        String joinDate = formatDate(tenant.leaseStartDate);

        if ("Active".equalsIgnoreCase(tenant.status)) {
            // For ACTIVE tenants, only show the join date.
            tvDetailLeaseLabel.setText("Joined On");
            tvDetailLease.setText(joinDate);
        } else {
            // For LEFT tenants, show the full period.
            String leftDate = formatDate(tenant.leaseEndDate);
            tvDetailLeaseLabel.setText("Period");
            tvDetailLease.setText(String.format("From: %s To: %s", joinDate, leftDate));
        }

        if ("Active".equalsIgnoreCase(tenant.status)) {
            tvDetailStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvDetailStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
        }

        dialog.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return "N/A";
        }
        try {
            Date date = dbDateFormat.parse(dateString);
            return displayDateFormat.format(date);
        } catch (ParseException e) {
            Log.e("TenantHistoryAdapter", "Could not parse date: \"" + dateString + "\"", e);
            return dateString;
        }
    }
}
