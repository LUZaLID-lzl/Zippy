package com.luza.zippy.ui.sidebarList.timer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimerRecordAdapter extends RecyclerView.Adapter<TimerRecordAdapter.ViewHolder> {
    private List<TimerRecord> records = new ArrayList<>();
    private OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(TimerRecord record);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

    public void setRecords(List<TimerRecord> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timer_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimerRecord record = records.get(position);
        
        // 设置类型图标
        if (record.isOption1()) {
            holder.typeImage.setImageResource(R.drawable.ic_spark);
        } else if (record.isOption2()) {
            holder.typeImage.setImageResource(R.drawable.ic_leaf);
        } else if (record.isOption3()) {
            holder.typeImage.setImageResource(R.drawable.ic_peach);
        }

        // 格式化时长
        long millis = record.getDuration();
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (millis % (1000 * 60)) / 1000;
        long milliseconds = millis % 1000;

        StringBuilder timeBuilder = new StringBuilder();
        if (hours > 0) {
            timeBuilder.append(hours).append("小时");
        }
        if (minutes > 0 || hours > 0) {
            timeBuilder.append(minutes).append("分");
        }
        if (seconds > 0 || minutes > 0 || hours > 0) {
            timeBuilder.append(seconds).append("秒");
        }
        timeBuilder.append(String.format(Locale.getDefault(), "%03d", milliseconds)).append("毫秒");
        
        holder.durationText.setText(timeBuilder.toString());

        // 格式化时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        holder.timestampText.setText(sdf.format(record.getTimestamp()));

        // 设置删除按钮点击事件
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(record);
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView typeImage;
        TextView durationText;
        TextView timestampText;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);
            typeImage = itemView.findViewById(R.id.image_type);
            durationText = itemView.findViewById(R.id.text_duration);
            timestampText = itemView.findViewById(R.id.text_timestamp);
            deleteButton = itemView.findViewById(R.id.btn_delete);
        }
    }
} 