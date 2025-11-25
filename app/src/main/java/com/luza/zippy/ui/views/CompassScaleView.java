package com.luza.zippy.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CompassScaleView extends View {
    
    private Paint paint;
    private RectF rectF;
    private float centerX, centerY, radius;
    
    public CompassScaleView(Context context) {
        super(context);
        init();
    }
    
    public CompassScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public CompassScaleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        rectF = new RectF();
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 20; // 留出20dp边距
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制外圈
        paint.setColor(Color.parseColor("#2196F3"));
        paint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius, paint);
        
        // 绘制刻度
        drawScales(canvas);
        
        // 绘制方向标记
        drawDirectionMarks(canvas);
    }
    
    private void drawScales(Canvas canvas) {
        paint.setColor(Color.parseColor("#2196F3"));
        
        // 每5度一个小刻度
        for (int i = 0; i < 360; i += 5) {
            float angle = (float) Math.toRadians(i);
            float startRadius = radius - 12;
            float endRadius = radius - 5;
            
            if (i % 30 == 0) {
                // 主刻度（每30度）- 更长
                startRadius = radius - 25;
                endRadius = radius - 5;
                paint.setStrokeWidth(3);
            } else if (i % 15 == 0) {
                // 中等刻度（每15度）
                startRadius = radius - 18;
                endRadius = radius - 5;
                paint.setStrokeWidth(2);
            } else {
                // 小刻度（每5度）
                paint.setStrokeWidth(1);
            }
            
            float startX = centerX + (float) Math.sin(angle) * startRadius;
            float startY = centerY - (float) Math.cos(angle) * startRadius;
            float endX = centerX + (float) Math.sin(angle) * endRadius;
            float endY = centerY - (float) Math.cos(angle) * endRadius;
            
            canvas.drawLine(startX, startY, endX, endY, paint);
        }
    }
    
    private void drawDirectionMarks(Canvas canvas) {
        paint.setColor(Color.parseColor("#2196F3"));
        paint.setStrokeWidth(4);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        
        // 绘制方向文字
        String[] directions = {"N", "E", "S", "W"};
        float[] angles = {0, 90, 180, 270};
        
        for (int i = 0; i < directions.length; i++) {
            float angle = (float) Math.toRadians(angles[i]);
            float textRadius = radius - 45;
            float x = centerX + (float) Math.sin(angle) * textRadius;
            float y = centerY - (float) Math.cos(angle) * textRadius;
            
            // 绘制文字背景
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(x, y, 15, paint);
            
            // 绘制文字
            paint.setColor(Color.parseColor("#2196F3"));
            canvas.drawText(directions[i], x, y + 6, paint);
        }
        
        // 绘制数字刻度（每30度）
        paint.setTextSize(16);
        for (int i = 0; i < 360; i += 30) {
            if (i == 0 || i == 90 || i == 180 || i == 270) continue; // 跳过方向文字位置
            
            float angle = (float) Math.toRadians(i);
            float textRadius = radius - 35;
            float x = centerX + (float) Math.sin(angle) * textRadius;
            float y = centerY - (float) Math.cos(angle) * textRadius;
            
            // 绘制数字
            paint.setColor(Color.parseColor("#2196F3"));
            canvas.drawText(String.valueOf(i), x, y + 5, paint);
        }
        
        paint.setStyle(Paint.Style.STROKE);
    }
} 