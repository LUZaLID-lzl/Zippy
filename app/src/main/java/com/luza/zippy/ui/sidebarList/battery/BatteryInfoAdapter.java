package com.luza.zippy.ui.sidebarList.battery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.R;
import java.util.ArrayList;
import java.util.List;

public class BatteryInfoAdapter extends RecyclerView.Adapter<BatteryInfoAdapter.ViewHolder> {
    private List<String> data = new ArrayList<>();

    public void setData(List<String> newData) {
        this.data.clear();
        if (newData != null) {
            this.data.addAll(newData);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_battery_info, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.text_battery_info);
        }
    }
} 