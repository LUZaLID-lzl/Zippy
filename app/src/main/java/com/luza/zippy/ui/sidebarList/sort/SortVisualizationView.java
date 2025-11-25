package com.luza.zippy.ui.sidebarList.sort;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import com.luza.zippy.R;

public class SortVisualizationView extends View {
    
    private List<Integer> data = new ArrayList<>();
    private List<Integer> originalData = new ArrayList<>();
    private Paint paint;
    private Paint textPaint;
    private Paint highlightPaint;
    private Paint comparingPaint;
    
    private int comparingIndex1 = -1;
    private int comparingIndex2 = -1;
    private int sortedIndex = -1;
    
    private int maxValue = 100;
    private int barWidth;
    private int barSpacing;
    private int padding = 20;
    
    public SortVisualizationView(Context context) {
        super(context);
        init();
    }
    
    public SortVisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public SortVisualizationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#2196F3"));
        
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        highlightPaint = new Paint();
        highlightPaint.setAntiAlias(true);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setColor(Color.parseColor("#FF5722"));
        
        comparingPaint = new Paint();
        comparingPaint.setAntiAlias(true);
        comparingPaint.setStyle(Paint.Style.FILL);
        comparingPaint.setColor(Color.parseColor("#FFC107"));
    }
    
    public void setData(List<Integer> newData) {
        this.data = new ArrayList<>(newData);
        this.originalData = new ArrayList<>(newData);
        if (!data.isEmpty()) {
            maxValue = data.stream().mapToInt(Integer::intValue).max().orElse(100);
        }
        calculateBarDimensions();
        invalidate();
    }
    
    public void updateData(List<Integer> newData) {
        this.data = new ArrayList<>(newData);
        invalidate();
    }
    
    public void setComparingIndices(int index1, int index2) {
        this.comparingIndex1 = index1;
        this.comparingIndex2 = index2;
        invalidate();
    }
    
    public void setSortedIndex(int index) {
        this.sortedIndex = index;
        invalidate();
    }
    
    public void resetHighlights() {
        this.comparingIndex1 = -1;
        this.comparingIndex2 = -1;
        this.sortedIndex = -1;
        invalidate();
    }
    
    public void resetToOriginal() {
        this.data = new ArrayList<>(originalData);
        resetHighlights();
    }
    
    private void calculateBarDimensions() {
        if (data.isEmpty()) return;
        
        int availableWidth = getWidth() - 2 * padding;
        barWidth = availableWidth / data.size() - 4;
        barSpacing = 4;
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateBarDimensions();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (data.isEmpty()) {
            // 绘制空状态提示
            textPaint.setTextSize(32);
            textPaint.setColor(Color.GRAY);
            String hintText = getContext().getString(R.string.sort_generate_data_hint);
            canvas.drawText(hintText, getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }
        
        int availableHeight = getHeight() - 2 * padding - 40; // 预留顶部空间给数字
        float scale = (float) availableHeight / maxValue;
        
        for (int i = 0; i < data.size(); i++) {
            int value = data.get(i);
            int barHeight = (int) (value * scale);
            
            int left = padding + i * (barWidth + barSpacing);
            int top = getHeight() - padding - barHeight;
            int right = left + barWidth;
            int bottom = getHeight() - padding;
            
            // 选择绘制颜色
            Paint currentPaint = paint;
            if (i == comparingIndex1 || i == comparingIndex2) {
                currentPaint = comparingPaint;
            } else if (i == sortedIndex) {
                currentPaint = highlightPaint;
            }
            
            // 绘制柱状图
            canvas.drawRect(left, top, right, bottom, currentPaint);
            
            // 在柱子上显示数字
            drawTextOnBar(canvas, value, left, top, right, bottom, barHeight);
        }
    }
    
    private void drawTextOnBar(Canvas canvas, int value, int left, int top, int right, int bottom, int barHeight) {
        String text = String.valueOf(value);
        
        // 设置文字大小（根据柱子宽度自适应）
        float textSize = Math.min(barWidth * 0.6f, 24f);
        textSize = Math.max(textSize, 8f); // 最小字体大小
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        // 测量文字尺寸
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float textWidth = textPaint.measureText(text);
        
        float textX = (left + right) / 2f;
        // 数字始终显示在柱子顶部上方
        float textY = top - 12f;
        
        // 设置文字颜色为深色
        textPaint.setColor(Color.parseColor("#333333"));
        textPaint.setStyle(Paint.Style.FILL);
        
        // 绘制文字背景（白色圆角矩形带边框）
        Paint backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.FILL);
        
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.parseColor("#DDDDDD"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1f);
        
        float padding = 4f;
        float bgLeft = textX - textWidth / 2f - padding;
        float bgTop = textY + fontMetrics.ascent - padding;
        float bgRight = textX + textWidth / 2f + padding;
        float bgBottom = textY + fontMetrics.descent + padding;
        
        canvas.drawRoundRect(bgLeft, bgTop, bgRight, bgBottom, 6f, 6f, backgroundPaint);
        canvas.drawRoundRect(bgLeft, bgTop, bgRight, bgBottom, 6f, 6f, borderPaint);
        
        // 绘制文字
        canvas.drawText(text, textX, textY, textPaint);
    }
} 