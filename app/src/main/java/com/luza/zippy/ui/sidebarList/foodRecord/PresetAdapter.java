package com.luza.zippy.ui.sidebarList.foodRecord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodPreset;

import java.util.List;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.PresetViewHolder> {
    private List<FoodPreset> presets;
    private OnPresetClickListener listener;

    public interface OnPresetClickListener {
        void onPresetClick(FoodPreset preset);
    }

    public PresetAdapter(List<FoodPreset> presets, OnPresetClickListener listener) {
        this.presets = presets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_preset, parent, false);
        return new PresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        FoodPreset preset = presets.get(position);
        holder.bind(preset);
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    class PresetViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;

        PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_preset_name);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPresetClick(presets.get(position));
                }
            });
        }

        void bind(FoodPreset preset) {
            nameText.setText(preset.getFoodName());
        }
    }
} 