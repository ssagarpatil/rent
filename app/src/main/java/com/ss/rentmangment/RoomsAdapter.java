package com.ss.rentmangment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomVH> {

    public interface OnRoomActionListener {
        void onEdit(RoomModel room);
        void onDelete(RoomModel room);
    }

    private List<RoomModel> list;
    private OnRoomActionListener listener;
    private Context context;

    public RoomsAdapter(List<RoomModel> list, OnRoomActionListener listener, Context context) {
        this.list = list;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomVH holder, int position) {
        RoomModel room = list.get(position);
        holder.tvRoomName.setText(room.getName());
        holder.tvTypeCapacity.setText(room.getType() + " • Capacity: " + room.getCapacity());

        // Calculate availability
        int available = room.getCapacity() - room.getOccupied();

        // Set availability text based on room type and occupancy
        String allowedFor = room.getAllowedFor();

        if (allowedFor.equals("Family") && room.getOccupied() > 0) {
            // Family exclusive rooms become unavailable once occupied
            holder.tvOccupiedAvailable.setText("Family Exclusive - No vacancies");
            holder.tvOccupiedAvailable.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else if (available <= 0) {
            holder.tvOccupiedAvailable.setText("Fully Occupied");
            holder.tvOccupiedAvailable.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            String availabilityText = "Available: " + available + " of " + room.getCapacity();
            if (allowedFor.equals("Both")) {
                availabilityText += " (Students & Families)";
            } else if (allowedFor.equals("Student")) {
                availabilityText += " (Students only)";
            }
            holder.tvOccupiedAvailable.setText(availabilityText);
            holder.tvOccupiedAvailable.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        }

        // Set rent information
        if (room.getRent() > 0) {
            holder.tvRent.setText("₹" + room.getRent());
        } else {
            holder.tvRent.setText("Rent not set");
        }

        // Set click listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(room));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(room));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void setList(List<RoomModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class RoomVH extends RecyclerView.ViewHolder {
        TextView tvRoomName, tvTypeCapacity, tvOccupiedAvailable, tvRent;
        MaterialButton btnEdit, btnDelete; // Changed from ImageButton to MaterialButton

        public RoomVH(@NonNull View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvTypeCapacity = itemView.findViewById(R.id.tvTypeCapacity);
            tvOccupiedAvailable = itemView.findViewById(R.id.tvOccupiedAvailable);
            tvRent = itemView.findViewById(R.id.tvRent);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}