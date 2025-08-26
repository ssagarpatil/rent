package com.ss.rentmangment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class RentCollectionAdapter extends RecyclerView.Adapter<RentCollectionAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<TenantProfile> originalList;
    private List<TenantProfile> filteredList;
    private OnCollectButtonClickListener listener;

    public interface OnCollectButtonClickListener {
        void onCollectClick(TenantProfile tenantProfile);
    }

    public RentCollectionAdapter(Context context, List<TenantProfile> tenantList, OnCollectButtonClickListener listener) {
        this.context = context;
        this.originalList = new ArrayList<>(tenantList != null ? tenantList : new ArrayList<>());
        this.filteredList = new ArrayList<>(this.originalList);
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
        if (position >= filteredList.size()) return;

        TenantProfile profile = filteredList.get(position);
        if (profile == null || profile.getTenant() == null) return;

        Tenant tenant = profile.getTenant();

        // Set basic tenant information
        setTenantBasicInfo(holder, tenant);

        // Set payment status and details with FIXED visibility
        setPaymentDetailsWithProperVisibility(holder, profile);

        // Set progress bar for collection percentage
        setCollectionProgress(holder, profile);

        // Set professional click listener
        setClickListeners(holder, profile);
    }

    /**
     * **ENHANCED: Professional tenant information display**
     */
    private void setTenantBasicInfo(ViewHolder holder, Tenant tenant) {
        // Tenant name
        if (holder.tvTenantName != null && tenant.name != null) {
            holder.tvTenantName.setText(tenant.name);

            // Visual indicator for "Left" tenants
            if ("Left".equalsIgnoreCase(tenant.status)) {
                holder.tvTenantName.setAlpha(0.7f);
                holder.tvTenantName.setText(tenant.name + " (Left)");
            } else {
                holder.tvTenantName.setAlpha(1.0f);
            }
        }

        // Room information with emoji
        if (holder.tvRoomNumber != null && tenant.roomNumber != null) {
            holder.tvRoomNumber.setText("" + tenant.roomNumber);
        }

        // Monthly rent with professional formatting
        if (holder.tvMonthlyRent != null) {
            holder.tvMonthlyRent.setText("₹" + formatAmount(tenant.rentAmount));
        }

        // **ENHANCED: Professional tenant initial display**
        if (holder.tvTenantInitial != null && tenant.name != null && !tenant.name.isEmpty()) {
            String initial = generateTenantInitials(tenant.name);
            holder.tvTenantInitial.setText(initial);
        }
    }

    /**
     * **FIXED: Proper visibility and content for collected/remaining amounts**
     */
    private void setPaymentDetailsWithProperVisibility(ViewHolder holder, TenantProfile profile) {
        String paymentStatus = profile.getPaymentStatus();
        double amountPaid = profile.getAmountPaid();
        double remainingAmount = profile.getRemainingAmount();
        double monthlyRent = profile.getTenant().rentAmount;

        if ("FULL".equals(paymentStatus)) {
            // **FULLY PAID DISPLAY**
            setStatusBadge(holder, "✅ FULLY PAID", R.color.success_color, R.drawable.status_paid_bg);

            // Show collected amount, hide remaining
            showCollectedAmount(holder, amountPaid, R.color.success_color);
            hideRemainingAmount(holder);

            // Hide collect button for fully paid
            if (holder.btnCollect != null) {
                holder.btnCollect.setVisibility(View.GONE);
            }

        } else if ("PARTIAL".equals(paymentStatus)) {
            // **PARTIALLY PAID DISPLAY**
            setStatusBadge(holder, "⚠️ PARTIAL", R.color.black, R.drawable.status_partial_bg);

            // Show both collected and remaining amounts
            showCollectedAmount(holder, amountPaid, R.color.success_color);
            showRemainingAmount(holder, remainingAmount, R.color.error_color, "");

            // Show collect button for remaining payment
            setCollectButton(holder, "Complete Payment", R.color.purple_700);

        } else {
            // **PENDING PAYMENT DISPLAY**
            setStatusBadge(holder, "❌ PENDING", R.color.error_color, R.drawable.status_partial_bg);

            // Hide collected amount, show full amount as pending
            hideCollectedAmount(holder);
            showRemainingAmount(holder, monthlyRent, R.color.error_color, "");

            // Show collect button for full payment
            setCollectButton(holder, "Collect Rent", R.color.primary_color);
        }
    }

    /**
     * **HELPER: Show collected amount with proper visibility**
     */
    private void showCollectedAmount(ViewHolder holder, double amount, int colorRes) {
        if (holder.tvCollectedAmount != null) {
            holder.tvCollectedAmount.setText("₹ " + formatAmount(amount));
            holder.tvCollectedAmount.setTextColor(ContextCompat.getColor(context, colorRes));
            holder.tvCollectedAmount.setVisibility(View.VISIBLE);
        }

        // Show container if it exists
        if (holder.layoutCollectedAmount != null) {
            holder.layoutCollectedAmount.setVisibility(View.VISIBLE);
        }
    }

    /**
     * **HELPER: Hide collected amount properly**
     */
    private void hideCollectedAmount(ViewHolder holder) {
        if (holder.tvCollectedAmount != null) {
            holder.tvCollectedAmount.setVisibility(View.GONE);
        }
        if (holder.layoutCollectedAmount != null) {
            holder.layoutCollectedAmount.setVisibility(View.GONE);
        }
    }

    /**
     * **HELPER: Show remaining amount with proper visibility**
     */
    private void showRemainingAmount(ViewHolder holder, double amount, int colorRes, String label) {
        if (holder.tvRemainingAmount != null) {
            String emoji = "";
            if (label.equals("Amount Due")) emoji = "💰";

            holder.tvRemainingAmount.setText(emoji + " " + label + "₹ " + formatAmount(amount));
            holder.tvRemainingAmount.setTextColor(ContextCompat.getColor(context, colorRes));
            holder.tvRemainingAmount.setVisibility(View.VISIBLE);
        }

        // Show container if it exists
        if (holder.layoutRemainingAmount != null) {
            holder.layoutRemainingAmount.setVisibility(View.VISIBLE);
        }
    }

    /**
     * **HELPER: Hide remaining amount properly**
     */
    private void hideRemainingAmount(ViewHolder holder) {
        if (holder.tvRemainingAmount != null) {
            holder.tvRemainingAmount.setVisibility(View.GONE);
        }
        if (holder.layoutRemainingAmount != null) {
            holder.layoutRemainingAmount.setVisibility(View.GONE);
        }
    }

    /**
     * **HELPER: Set status badge with professional styling**
     */
    private void setStatusBadge(ViewHolder holder, String text, int textColorRes, int backgroundRes) {
        if (holder.tvPaymentStatus != null) {
            holder.tvPaymentStatus.setText(text);
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(context, textColorRes));
            holder.tvPaymentStatus.setBackgroundResource(backgroundRes);
        }
    }

    /**
     * **HELPER: Set collect button with professional styling**
     */
    private void setCollectButton(ViewHolder holder, String text, int colorRes) {
        if (holder.btnCollect != null) {
            holder.btnCollect.setText(text);
            holder.btnCollect.setVisibility(View.VISIBLE);
            holder.btnCollect.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)));
        }
    }

    /**
     * **ENHANCED: Professional collection progress display**
     */
    private void setCollectionProgress(ViewHolder holder, TenantProfile profile) {
        if (holder.progressCollection != null && holder.tvCollectionPercentage != null) {
            double percentage = profile.getCollectionPercentage();
            int progressInt = (int) Math.round(percentage);

            holder.progressCollection.setProgress(progressInt);
            holder.tvCollectionPercentage.setText(progressInt + "%");

            // **ENHANCED: Dynamic progress bar colors**
            int progressColor;
            if (progressInt == 100) {
                progressColor = R.color.success_color;
            } else if (progressInt > 0) {
                progressColor = R.color.success_color;
            } else {
                progressColor = R.color.purple_700;
            }

            holder.progressCollection.setProgressTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(context, progressColor)));
            holder.tvCollectionPercentage.setTextColor(
                    ContextCompat.getColor(context, progressColor));
        }
    }

    /**
     * **ENHANCED: Professional click listeners**
     */
    private void setClickListeners(ViewHolder holder, TenantProfile profile) {
        if (holder.btnCollect != null) {
            holder.btnCollect.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCollectClick(profile);
                }
            });
        }

        // **NEW: Card click for details**
        if (holder.cardView != null) {
            holder.cardView.setOnClickListener(v -> {
                // Optional: Add card click functionality
                // Could show tenant details or payment history
            });
        }
    }

    @Override
    public int getItemCount() {
        return filteredList != null ? filteredList.size() : 0;
    }

    /**
     * **ENHANCED: Professional list update with proper notification**
     */
    public void updateList(List<TenantProfile> newList) {
        if (newList != null) {
            this.originalList.clear();
            this.originalList.addAll(newList);
            this.filteredList.clear();
            this.filteredList.addAll(newList);
            notifyDataSetChanged();
        }
    }

    /**
     * **NEW: Update single item professionally**
     */
    public void updateItem(TenantProfile updatedProfile, int position) {
        if (position >= 0 && position < filteredList.size()) {
            filteredList.set(position, updatedProfile);

            // Also update in original list
            int originalPosition = originalList.indexOf(filteredList.get(position));
            if (originalPosition >= 0) {
                originalList.set(originalPosition, updatedProfile);
            }

            notifyItemChanged(position);
        }
    }

    /**
     * **ENHANCED: Professional filtering with better search**
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<TenantProfile> filtered = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filtered.addAll(originalList);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (TenantProfile profile : originalList) {
                        if (profile != null && profile.getTenant() != null) {
                            Tenant tenant = profile.getTenant();

                            // **ENHANCED: Multi-field search**
                            boolean matches = false;

                            // Search in name
                            if (tenant.name != null && tenant.name.toLowerCase().contains(filterPattern)) {
                                matches = true;
                            }
                            // Search in mobile
                            else if (tenant.mobile != null && tenant.mobile.contains(filterPattern)) {
                                matches = true;
                            }
                            // Search in room number
                            else if (tenant.roomNumber != null && tenant.roomNumber.toLowerCase().contains(filterPattern)) {
                                matches = true;
                            }
                            // Search by payment status
                            else if (profile.getPaymentStatus() != null &&
                                    profile.getPaymentStatus().toLowerCase().contains(filterPattern)) {
                                matches = true;
                            }

                            if (matches) {
                                filtered.add(profile);
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filtered;
                results.count = filtered.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredList.clear();
                if (results.values != null) {
                    filteredList.addAll((List<TenantProfile>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    /**
     * **HELPER: Professional amount formatting**
     */
    private String formatAmount(double amount) {
        return String.format("%.0f", amount);
    }

    /**
     * **HELPER: Generate professional tenant initials**
     */
    private String generateTenantInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "T";

        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (int i = 0; i < Math.min(parts.length, 2); i++) {
            if (!parts[i].isEmpty()) {
                initials.append(parts[i].charAt(0));
            }
        }

        String result = initials.toString().toUpperCase();
        return result.isEmpty() ? "T" : result;
    }

    /**
     * **ENHANCED: Professional ViewHolder with all original IDs**
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // **ALL ORIGINAL IDs PRESERVED**
        MaterialCardView cardView;
        TextView tvTenantInitial, tvTenantName, tvRoomNumber, tvMonthlyRent;
        TextView tvPaymentStatus, tvCollectedAmount, tvRemainingAmount, tvCollectionPercentage;
        ProgressBar progressCollection;
        MaterialButton btnCollect;

        // **NEW: Optional container layouts for better visibility control**
        LinearLayout layoutCollectedAmount, layoutRemainingAmount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize all original views
            cardView = itemView.findViewById(R.id.cardView);
            tvTenantInitial = itemView.findViewById(R.id.tvTenantInitial);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            tvRoomNumber = itemView.findViewById(R.id.tvRoomNumber);
            tvMonthlyRent = itemView.findViewById(R.id.tvMonthlyRent);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvCollectedAmount = itemView.findViewById(R.id.tvCollectedAmount);
            tvRemainingAmount = itemView.findViewById(R.id.tvRemainingAmount);
            tvCollectionPercentage = itemView.findViewById(R.id.tvCollectionPercentage);
            progressCollection = itemView.findViewById(R.id.progressCollection);
            btnCollect = itemView.findViewById(R.id.btnCollect);

            // Initialize optional container layouts (may be null if not in XML)
            layoutCollectedAmount = itemView.findViewById(R.id.layoutCollectedAmount);
            layoutRemainingAmount = itemView.findViewById(R.id.layoutRemainingAmount);
        }
    }
}
