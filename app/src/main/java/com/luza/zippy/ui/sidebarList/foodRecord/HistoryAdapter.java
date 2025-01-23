package com.luza.zippy.ui.sidebarList.foodRecord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luza.zippy.R;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecordHistory;

import java.lang.reflect.Type;
import java.util.List;

public class HistoryAdapter extends ListAdapter<FoodRecordHistory, HistoryAdapter.HistoryViewHolder> {

    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onHistoryLongClick(FoodRecordHistory history);
        void onHistoryClick(FoodRecordHistory history);
    }

    protected HistoryAdapter() {
        super(new HistoryDiff());
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        FoodRecordHistory history = getItem(position);
        holder.bind(history);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView nameText;
        private TextView dateText;
        private TextView summaryText;
        private final OnHistoryClickListener listener;
        private FoodRecordHistory currentHistory;

        HistoryViewHolder(@NonNull View itemView, OnHistoryClickListener listener) {
            super(itemView);
            this.listener = listener;
            nameText = itemView.findViewById(R.id.text_history_name);
            dateText = itemView.findViewById(R.id.text_history_date);
            summaryText = itemView.findViewById(R.id.text_history_summary);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION && currentHistory != null) {
                    listener.onHistoryClick(currentHistory);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && currentHistory != null) {
                    listener.onHistoryLongClick(currentHistory);
                    return true;
                }
                return false;
            });
        }

        void bind(FoodRecordHistory history) {
            this.currentHistory = history;
            nameText.setText(history.getName());
            dateText.setText(history.getRecordDate());

            // 解析JSON并生成摘要
            Gson gson = new Gson();
            Type listType = new TypeToken<List<FoodRecord>>(){}.getType();
            List<FoodRecord> records = gson.fromJson(history.getFoodRecords(), listType);

            StringBuilder summary = new StringBuilder();
            for (int i = 0; i < Math.min(records.size(), 3); i++) {
                FoodRecord record = records.get(i);
                if (i > 0) summary.append("、");
                summary.append(record.getFoodName())
                       .append(" x")
                       .append(record.getServings());
            }
            if (records.size() > 3) {
                summary.append(" 等");
            }
            summaryText.setText(summary);
        }
    }

    static class HistoryDiff extends DiffUtil.ItemCallback<FoodRecordHistory> {
        @Override
        public boolean areItemsTheSame(@NonNull FoodRecordHistory oldItem, @NonNull FoodRecordHistory newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodRecordHistory oldItem, @NonNull FoodRecordHistory newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getRecordDate().equals(newItem.getRecordDate()) &&
                   oldItem.getFoodRecords().equals(newItem.getFoodRecords());
        }
    }
} 