package com.luza.zippy.ui.sidebarList.scrummage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.R;
import com.luza.zippy.ui.sidebarList.scrummage.data.entity.ScrummageRecord;
import java.util.Locale;

public class ScrummageAdapter extends ListAdapter<ScrummageRecord, ScrummageAdapter.ViewHolder> {
    private OnItemClickListener listener;

    public ScrummageAdapter() {
        super(new DiffUtil.ItemCallback<ScrummageRecord>() {
            @Override
            public boolean areItemsTheSame(@NonNull ScrummageRecord oldItem, @NonNull ScrummageRecord newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull ScrummageRecord oldItem, @NonNull ScrummageRecord newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scrummage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScrummageRecord record = getItem(position);
        holder.bind(record);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView amountText;
        private final TextView dateText;
        private final TextView payerText;

        ViewHolder(View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_title);
            amountText = itemView.findViewById(R.id.text_amount);
            dateText = itemView.findViewById(R.id.text_date);
            payerText = itemView.findViewById(R.id.text_payer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        void bind(ScrummageRecord record) {
            titleText.setText(record.getTitle());
            amountText.setText(String.format(Locale.getDefault(), "¥%.2f", record.getAmount()));
            dateText.setText(record.getDate());
            payerText.setText(record.getPayer());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ScrummageRecord record);
        void onItemLongClick(ScrummageRecord record);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
} 