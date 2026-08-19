package com.example.fieldreservation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TerrainAdapter extends RecyclerView.Adapter<TerrainAdapter.TerrainViewHolder> {
    private List<Terrain> terrains;
    private OnItemClickListener listener;
    private boolean managementEnabled = false;

    public interface OnItemClickListener {
        void onEditClick(Terrain terrain);
        void onDeleteClick(Terrain terrain);
    }

    public TerrainAdapter(List<Terrain> terrains, OnItemClickListener listener) {
        this.terrains = terrains;
        this.listener = listener;
    }

    public void setManagementEnabled(boolean enabled) {
        this.managementEnabled = enabled;
        notifyDataSetChanged();
    }

    public void updateData(List<Terrain> newTerrains) {
        this.terrains = newTerrains;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TerrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_terrain, parent, false);
        return new TerrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TerrainViewHolder holder, int position) {
        Terrain terrain = terrains.get(position);
        holder.bind(terrain, listener, managementEnabled);
    }

    @Override
    public int getItemCount() {
        return terrains.size();
    }

    static class TerrainViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvLocation, tvPrice;
        private Button btnEdit, btnDelete;

        public TerrainViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Terrain terrain, OnItemClickListener listener, boolean managementEnabled) {
            tvName.setText(terrain.getName());
            tvLocation.setText(terrain.getLocation());
            tvPrice.setText(String.format("$%.2f", terrain.getPrice()));

            if (managementEnabled) {
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                
                btnEdit.setOnClickListener(v -> listener.onEditClick(terrain));
                btnDelete.setOnClickListener(v -> listener.onDeleteClick(terrain));
            } else {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
            }
        }
    }
}

