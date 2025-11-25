package com.luza.zippy.ui.views;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CenterScaleLayoutManager extends LinearLayoutManager {
    private final float mShrinkAmount = 0.15f;
    private final float mShrinkDistance = 0.9f;

    public CenterScaleLayoutManager(Context context) {
        super(context);
        setOrientation(LinearLayoutManager.HORIZONTAL);
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        scaleChildren();
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
        if (scrolled != 0) {
            scaleChildren();
        }
        return scrolled;
    }

    private void scaleChildren() {
        float midpoint = getWidth() / 2.0f;
        float d0 = 0.f;
        float d1 = mShrinkDistance * midpoint;
        float s0 = 1.f;
        float s1 = 1.f - mShrinkAmount;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                float childMidpoint = (getDecoratedRight(child) + getDecoratedLeft(child)) / 2.f;
                float d = Math.min(d1, Math.abs(midpoint - childMidpoint));
                float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                
                float z = Math.max(0.5f, scale);
                child.setTranslationZ(z * 8);
                
                child.setScaleX(scale);
                child.setScaleY(scale);
                
                child.setAlpha(0.5f + 0.5f * scale);
            }
        }
    }
} 