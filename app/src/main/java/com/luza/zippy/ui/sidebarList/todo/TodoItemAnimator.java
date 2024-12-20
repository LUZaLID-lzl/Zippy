package com.luza.zippy.ui.sidebarList.todo;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class TodoItemAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(-20f);
        holder.itemView.setScaleX(1.05f);
        holder.itemView.setScaleY(1.05f);

        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)
            .setInterpolator(new DecelerateInterpolator())
            .start();

        return true;
    }

    @Override
    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        holder.itemView.animate()
            .alpha(0f)
            .translationX(holder.itemView.getWidth())
            .setDuration(300)
            .setInterpolator(new AccelerateInterpolator())
            .start();

        return true;
    }
} 