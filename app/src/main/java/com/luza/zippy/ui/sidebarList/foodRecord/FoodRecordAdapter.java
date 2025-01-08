package com.luza.zippy.ui.sidebarList.foodRecord;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.luza.zippy.R;
import com.luza.zippy.ui.sidebarList.foodRecord.data.entity.FoodRecord;

public class FoodRecordAdapter extends ListAdapter<FoodRecord, FoodRecordAdapter.FoodViewHolder> {

    private OnItemClickListener listener;

    protected FoodRecordAdapter(@NonNull DiffUtil.ItemCallback<FoodRecord> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_record, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodRecord current = getItem(position);
        holder.bind(current);
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        private final TextView foodNameText;
        private final TextView servingsText;
        private final ImageButton decreaseButton;
        private final ImageButton increaseButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodNameText = itemView.findViewById(R.id.text_food_name);
            servingsText = itemView.findViewById(R.id.text_servings);
            decreaseButton = itemView.findViewById(R.id.btn_decrease);
            increaseButton = itemView.findViewById(R.id.btn_increase);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            decreaseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FoodRecord foodRecord = getItem(position);
                    if (foodRecord.getServings() > 1) {
                        foodRecord.setServings(foodRecord.getServings() - 1);
                        servingsText.setText(String.format("%d份", foodRecord.getServings()));
                        if (listener != null) {
                            listener.onServingsChanged(foodRecord);
                        }
                    }
                }
            });

            increaseButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FoodRecord foodRecord = getItem(position);
                    foodRecord.setServings(foodRecord.getServings() + 1);
                    servingsText.setText(String.format("%d份", foodRecord.getServings()));
                    if (listener != null) {
                        listener.onServingsChanged(foodRecord);
                    }
                }
            });
        }

        public void bind(FoodRecord foodRecord) {
            foodNameText.setText(foodRecord.getFoodName());
            servingsText.setText(String.format("%d份", foodRecord.getServings()));
        }
    }

    public static class FoodRecordDiff extends DiffUtil.ItemCallback<FoodRecord> {
        @Override
        public boolean areItemsTheSame(@NonNull FoodRecord oldItem, @NonNull FoodRecord newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FoodRecord oldItem, @NonNull FoodRecord newItem) {
            return oldItem.getFoodName().equals(newItem.getFoodName()) &&
                    oldItem.getServings() == newItem.getServings();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(FoodRecord foodRecord);
        void onServingsChanged(FoodRecord foodRecord);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
} 