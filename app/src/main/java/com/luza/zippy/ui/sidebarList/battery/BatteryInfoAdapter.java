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
    private List<String> infoList = new ArrayList<>();

    public void setData(List<String> data) {
        infoList.clear();
        if (data != null) {
            infoList.addAll(data);
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
        holder.infoText.setText(infoList.get(position));
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView infoText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            infoText = itemView.findViewById(R.id.text_info);
        }
    }
} 