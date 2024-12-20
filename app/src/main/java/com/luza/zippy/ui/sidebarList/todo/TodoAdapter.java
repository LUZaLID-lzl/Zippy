package com.luza.zippy.ui.sidebarList.todo;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.luza.zippy.R;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {
    private final List<TodoItem> todoList;
    private final BiConsumer<Integer, TodoItem> onEditClick;
    private final Consumer<Integer> onDeleteClick;
    private final Consumer<TodoItem> onItemCompleted;

    public TodoAdapter(List<TodoItem> todoList,
                      BiConsumer<Integer, TodoItem> onEditClick,
                      Consumer<Integer> onDeleteClick,
                      Consumer<TodoItem> onItemCompleted) {
        this.todoList = todoList;
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
        this.onItemCompleted = onItemCompleted;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_todo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoItem item = todoList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView contentView;
        private final CheckBox checkBox;
        private float initialX;
        private static final float SWIPE_THRESHOLD = 100f;

        ViewHolder(View view) {
            super(view);
            titleView = view.findViewById(R.id.text_title);
            contentView = view.findViewById(R.id.text_content);
            checkBox = view.findViewById(R.id.checkbox);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = event.getX();
                        return true;
                    case MotionEvent.ACTION_UP:
                        float deltaX = event.getX() - initialX;
                        if (deltaX > SWIPE_THRESHOLD) {
                            TodoItem item = todoList.get(getAdapterPosition());
                            item.setCompleted(true);
                            onItemCompleted.accept(item);
                            notifyItemChanged(getAdapterPosition());
                        }
                        return true;
                }
                return false;
            });

            view.findViewById(R.id.btn_edit).setOnClickListener(v -> 
                onEditClick.accept(getAdapterPosition(), todoList.get(getAdapterPosition())));
            
            view.findViewById(R.id.btn_delete).setOnClickListener(v ->
                onDeleteClick.accept(getAdapterPosition()));
        }

        void bind(TodoItem item) {
            titleView.setText(item.getTitle());
            contentView.setText(item.getContent());
            checkBox.setChecked(item.isCompleted());
            
            float alpha = item.isCompleted() ? 0.5f : 1.0f;
            itemView.setAlpha(alpha);
            titleView.setPaintFlags(item.isCompleted() ? 
                titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG :
                titleView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
} 