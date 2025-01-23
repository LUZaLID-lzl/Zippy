package com.luza.zippy.ui.sidebarList.scrummage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;

import java.util.List;

public class ScrummageTypeAdapter extends RecyclerView.Adapter<ScrummageTypeAdapter.ViewHolder> {
    private List<String> types;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, String type);
        void onItemLongClick(int position, String type);
    }

    public ScrummageTypeAdapter(List<String> types) {
        this.types = types;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scrummage_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String type = types.get(position);
        holder.typeText.setText(type);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, type);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(position, type);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView typeText;

        ViewHolder(View itemView) {
            super(itemView);
            typeText = itemView.findViewById(R.id.text_type);
        }
    }
} 