package com.luza.zippy.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.luza.zippy.ui.sidebarList.minecraft.location.LocationModel;

import java.util.ArrayList;
import java.util.List;

public class CoordinateView extends View {
    private Paint axisPaint;      // 坐标轴画笔
    private Paint pointPaint;     // 坐标点画笔
    private Paint labelPaint;     // 标签画笔
    private Paint coordinatePaint; // 坐标文字画笔
    
    private int centerX = 0;      // 中心点X坐标
    private int centerY = 0;      // 中心点Y坐标
    private float scale = 1.0f;   // 缩放比例
    private List<LocationModel> locations = new ArrayList<>();
    private float translateX = 0f;
    private float translateY = 0f;
    private float lastTouchX;
    private float lastTouchY;
    private float minScale = 0.1f;
    private float maxScale = 5.0f;
    private ScaleGestureDetector scaleDetector;
    private static final float ZOOM_FOCUS_SCALE = 0.3f;  // 缩放焦点比例
    private static final float TEXT_SHOW_SCALE = 0.3f;   // 文字显示的最小缩放比例
    private float autoScale = 1.0f;  // 自动计算的基础缩放比例

    public CoordinateView(Context context) {
        super(context);
        init();
    }

    public CoordinateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 初始化坐标轴画笔
        axisPaint = new Paint();
        axisPaint.setColor(Color.LTGRAY);  // 使用浅灰色
        axisPaint.setStrokeWidth(1);       // 细线
        axisPaint.setStyle(Paint.Style.STROKE);

        // 初始化坐标点画笔
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.FILL);

        // 初始化标签画笔
        labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(14);
        labelPaint.setTextAlign(Paint.Align.LEFT);

        // 添加坐标文字画笔
        coordinatePaint = new Paint();
        coordinatePaint.setColor(Color.WHITE);  // 改为白色，与标签文字一致
        coordinatePaint.setTextSize(14);        // 与标签文字大小一致
        coordinatePaint.setTextAlign(Paint.Align.LEFT);
        
        // 修改缩放检测器
        scaleDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            private float focusX, focusY;

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                focusX = detector.getFocusX();
                focusY = detector.getFocusY();
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float oldScale = scale;
                float newScale = scale * detector.getScaleFactor();
                scale = Math.max(minScale, Math.min(maxScale, newScale));
                
                // 根据缩放焦点调整平移
                if (scale != oldScale) {
                    float scaleFactor = scale / oldScale;
                    float dx = focusX - (focusX - translateX) * scaleFactor;
                    float dy = focusY - (focusY - translateY) * scaleFactor;
                    translateX += (dx - translateX) * ZOOM_FOCUS_SCALE;
                    translateY += (dy - translateY) * ZOOM_FOCUS_SCALE;
                }
                
                invalidate();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress()) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    translateX += dx;
                    translateY += dy;
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    invalidate();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        canvas.translate(translateX, translateY);
        
        // 应用自动缩放和用户缩放的组合
        float finalScale = scale * autoScale;
        
        int width = getWidth();
        int height = getHeight();
        int viewCenterX = width / 2;
        int viewCenterY = height / 2;

        // 绘制贯穿整个界面的坐标轴
        canvas.drawLine(-width, viewCenterY, width * 2, viewCenterY, axisPaint);
        canvas.drawLine(viewCenterX, -height, viewCenterX, height * 2, axisPaint);

        // 绘制中心点
        Paint centerPointPaint = new Paint();
        centerPointPaint.setColor(Color.BLUE);  // 蓝色
        centerPointPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(viewCenterX, viewCenterY, 8 * finalScale, centerPointPaint);  // 稍微大一点的点

        // 绘制坐标点和标签
        for (LocationModel location : locations) {
            float x = viewCenterX + (location.getX() - centerX) * finalScale;
            float y = viewCenterY - (location.getY() - centerY) * finalScale;
            
            // 绘制连接线
            drawConnectionLines(canvas, location, x, y, viewCenterX, viewCenterY, finalScale);
            
            // 绘制点
            canvas.drawCircle(x, y, 6 * finalScale, pointPaint);
            
            // 准备标签文本
            String label = location.getName();
            String coordinates = String.format(" (%d,%d)", location.getX(), location.getY());
            String fullText = label + coordinates;
            
            float textWidth = labelPaint.measureText(fullText);
            float textHeight = labelPaint.descent() - labelPaint.ascent();
            float padding = 4 * finalScale;

            // 根据点的位置决定标签的位置
            float labelX, labelY;
            float boxLeft, boxTop, boxRight, boxBottom;
            
            // 判断点在哪个象限并设置标签位置
            boolean isRightSide = x > viewCenterX;
            boolean isTopSide = y < viewCenterY;

            // 第一象限（右上）
            if (isRightSide && isTopSide) {
                boxLeft = x + 12 * finalScale;
                boxRight = x + textWidth + 20 * finalScale;
                boxTop = y - textHeight - padding * 3;
                boxBottom = y - padding;
            }
            // 第二象限（左上）
            else if (!isRightSide && isTopSide) {
                boxLeft = x - textWidth - 20 * finalScale;
                boxRight = x - 12 * finalScale;
                boxTop = y - textHeight - padding * 3;
                boxBottom = y - padding;
            }
            // 第三象限（左下）
            else if (!isRightSide && !isTopSide) {
                boxLeft = x - textWidth - 20 * finalScale;
                boxRight = x - 12 * finalScale;
                boxTop = y + padding;
                boxBottom = y + textHeight + padding * 3;
            }
            // 第四象限（右下）
            else {
                boxLeft = x + 12 * finalScale;
                boxRight = x + textWidth + 20 * finalScale;
                boxTop = y + padding;
                boxBottom = y + textHeight + padding * 3;
            }

            // 绘制标签背景
            Paint.Style originalStyle = pointPaint.getStyle();
            pointPaint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(
                boxLeft,
                boxTop,
                boxRight,
                boxBottom,
                8 * finalScale,
                8 * finalScale,
                pointPaint
            );
            pointPaint.setStyle(originalStyle);
            
            // 绘制文本
            if (finalScale > TEXT_SHOW_SCALE) {
                float textX = isRightSide ? boxLeft + padding : boxLeft + padding;
                float textY = boxTop + textHeight;
                canvas.drawText(label, textX, textY, labelPaint);
                canvas.drawText(coordinates, textX + labelPaint.measureText(label), textY, coordinatePaint);
            }
        }
        
        canvas.restore();
    }

    private void drawConnectionLines(Canvas canvas, LocationModel location, float x, float y, 
                                   int viewCenterX, int viewCenterY, float finalScale) {
        Paint connectionPaint = new Paint();
        connectionPaint.setColor(Color.parseColor("#4CAF50")); // 使用绿色
        connectionPaint.setStrokeWidth(1 * finalScale);
        connectionPaint.setStyle(Paint.Style.STROKE);
        connectionPaint.setPathEffect(new DashPathEffect(new float[]{5 * finalScale, 5 * finalScale}, 0));

        switch (location.getConnectionType()) {
            case "X轴连接":
                // 只绘制连接线，不绘制红点
                canvas.drawLine(x, y, x, viewCenterY, connectionPaint);
                break;
                
            case "Y轴连接":
                // 只绘制连接线，不绘制红点
                canvas.drawLine(x, y, viewCenterX, y, connectionPaint);
                break;
                
            case "XY都连接":
                // 只绘制连接线，不绘制红点
                canvas.drawLine(x, y, x, viewCenterY, connectionPaint);
                canvas.drawLine(x, y, viewCenterX, y, connectionPaint);
                break;
        }
    }

    public void setCenter(int x, int y) {
        this.centerX = x;
        this.centerY = y;
        invalidate();
    }

    public void setLocations(List<LocationModel> locations) {
        this.locations = locations;
        calculateAutoScale();  // 设置坐标时重新计算缩放比例
        invalidate();
    }

    public void setScale(float scale) {
        this.scale = scale;
        invalidate();
    }

    private void calculateAutoScale() {
        if (locations == null || locations.isEmpty()) {
            autoScale = 1.0f;
            return;
        }

        // 找出坐标的最大范围
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (LocationModel location : locations) {
            int x = location.getX();
            int y = location.getY();
            
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        // 考虑中心点的位置
        minX = Math.min(minX, centerX);
        maxX = Math.max(maxX, centerX);
        minY = Math.min(minY, centerY);
        maxY = Math.max(maxY, centerY);

        // 计算坐标范围
        int rangeX = Math.abs(maxX - minX);
        int rangeY = Math.abs(maxY - minY);

        // 获取视图尺寸
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0) {
            return;  // 视图尚未测量完成
        }

        // 计算合适的缩放比例
        float scaleX = (viewWidth * 0.8f) / (rangeX > 0 ? rangeX : 1);
        float scaleY = (viewHeight * 0.8f) / (rangeY > 0 ? rangeY : 1);

        // 使用较小的缩放比例，确保所有点都在视图内
        autoScale = Math.min(scaleX, scaleY);

        // 限制缩放范围
        autoScale = Math.max(minScale, Math.min(maxScale, autoScale));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateAutoScale();  // 视图大小改变时重新计算缩放比例
    }

    public void resetPosition() {
        // 重置平移
        translateX = 0;
        translateY = 0;
        
        // 重置用户缩放
        scale = 1.0f;
        
        // 重新计算自动缩放
        calculateAutoScale();
        
        // 刷新视图
        invalidate();
    }
} 