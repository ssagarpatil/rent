package com.ss.rentmangment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.ss.rentmangment.R;
import com.ss.rentmangment.TenantProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RentCollectionAdapter extends RecyclerView.Adapter<RentCollectionAdapter.ViewHolder> implements Filterable {

    private final List<TenantProfile> tenantList;
    private List<TenantProfile> tenantListFiltered;
    private final Context context;
    private final OnCollectButtonClickListener listener;

    public interface OnCollectButtonClickListener {
        void onCollectClick(TenantProfile tenantProfile);
    }

    public RentCollectionAdapter(Context context, List<TenantProfile> tenantList, OnCollectButtonClickListener listener) {
        this.context = context;
        this.tenantList = tenantList;
        this.tenantListFiltered = new ArrayList<>(tenantList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_tenant_rent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TenantProfile profile = tenantListFiltered.get(position);
        holder.bind(profile, listener);
    }

    @Override
    public int getItemCount() {
        return tenantListFiltered.size();
    }

    public void updateList(List<TenantProfile> newList) {
        tenantList.clear();
        tenantList.addAll(newList);
        tenantListFiltered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase(Locale.getDefault());
                if (charString.isEmpty()) {
                    tenantListFiltered = new ArrayList<>(tenantList);
                } else {
                    List<TenantProfile> filteredList = new ArrayList<>();
                    for (TenantProfile profile : tenantList) {
                        if (profile.getTenant().name.toLowerCase(Locale.getDefault()).contains(charString) ||
                                profile.getTenant().mobile.contains(charString)) {
                            filteredList.add(profile);
                        }
                    }
                    tenantListFiltered = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = tenantListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, @NonNull FilterResults results) {
                tenantListFiltered = (ArrayList<TenantProfile>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenantName, tvRoomNumber, tvRentAmount, tvPaidStatus, tvPendingMonths;
        MaterialButton btnCollect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvRentAmount = itemView.findViewById(R.id.tvRentAmount);
            tvPaidStatus = itemView.findViewById(R.id.tvPaidStatus);
            btnCollect = itemView.findViewById(R.id.btnCollect);
            tvPendingMonths = itemView.findViewById(R.id.tvPendingMonths);
        }

        public void bind(final TenantProfile profile, final OnCollectButtonClickListener listener) {
            tvTenantName.setText(profile.getTenant().name);
            tvRoomNumber.setText("Room: " + profile.getTenant().roomNumber);
            tvRentAmount.setText("Rent: ₹" + (int) profile.getTenant().rentAmount);

            if (profile.isPaidForCurrentMonth()) {
                btnCollect.setVisibility(View.GONE);
                tvPaidStatus.setVisibility(View.VISIBLE);
                tvPendingMonths.setVisibility(View.GONE);
            } else {
                btnCollect.setVisibility(View.VISIBLE);
                tvPaidStatus.setVisibility(View.GONE);
                if (profile.getPendingMonths() > 0) {
                    tvPendingMonths.setText("Pending Months: " + profile.getPendingMonths());
                    tvPendingMonths.setVisibility(View.VISIBLE);
                } else {
                    tvPendingMonths.setVisibility(View.GONE);
                }
            }

            btnCollect.setOnClickListener(v -> listener.onCollectClick(profile));
        }
    }
}
