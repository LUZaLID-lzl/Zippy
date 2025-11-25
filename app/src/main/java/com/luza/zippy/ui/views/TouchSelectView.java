package com.luza.zippy.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TouchSelectView extends View {
    private static final long PREPARATION_TIME = 5000; // 5秒准备时间
    private static final long HIGHLIGHT_TIME = 5000; // 5秒高亮时间
    private static final float HIGHLIGHT_RATIO = 0.9375f; // HIGHLIGHT_RADIUS / BASE_TOUCH_RADIUS
    private static final int RIPPLE_COUNT = 4; // 减少涟漪圈数
    private static final float RIPPLE_SPACING_RATIO = 0.15f; // RIPPLE_SPACING / BASE_TOUCH_RADIUS
    private static final float PULSE_SCALE = 1.2f;
    private static final int MAX_TOUCH_POINTS = 100;

    private float baseTouchRadius; // 基础触摸半径
    private float highlightRadius; // 高亮半径
    private float rippleSpacing; // 涟漪间距

    private Paint backgroundPaint;
    private Paint touchPaint;
    private Paint highlightPaint;
    private Paint ripplePaint;
    private Paint glowPaint;
    private Paint finalHighlightPaint;
    private List<PointF> touchPoints;
    private PointF selectedPoint;
    private boolean isPreparing;
    private boolean isSelecting;
    private long startTime;
    private Handler handler;
    private Random random;
    private TouchSelectListener listener;
    private float pulseProgress = 0f;
    private float glowAlpha = 0f;
    private ValueAnimator pulseAnimator;
    private ValueAnimator glowAnimator;
    private OnMaxTouchReachedListener maxTouchReachedListener;
    private boolean isFinalSelected = false;

    public interface TouchSelectListener {
        void onPreparationStart();
        void onSelectionStart();
        void onSelectionComplete(int selectedIndex);
        void onReset();
    }

    public interface OnMaxTouchReachedListener {
        void onMaxTouchReached();
    }

    public TouchSelectView(Context context) {
        super(context);
        initTouchRadius();
        init();
    }

    public TouchSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTouchRadius();
        init();
    }

    private void initTouchRadius() {
        // 获取屏幕宽度
        android.util.DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        
        // 设置基础触摸半径为屏幕宽度的1/10
        baseTouchRadius = screenWidth / 10f;
        
        // 计算其他相关尺寸
        highlightRadius = baseTouchRadius * HIGHLIGHT_RATIO;
        rippleSpacing = baseTouchRadius * RIPPLE_SPACING_RATIO;
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(150, 0, 0, 0));
        backgroundPaint.setStyle(Paint.Style.FILL);

        touchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        touchPaint.setColor(Color.argb(200, 187, 222, 251)); // blue_100
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeWidth(2f); // 减小描边宽度

        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.argb(255, 33, 150, 243)); // blue_500
        highlightPaint.setStyle(Paint.Style.FILL);

        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setStyle(Paint.Style.STROKE);
        ripplePaint.setStrokeWidth(2f); // 减小描边宽度
        ripplePaint.setColor(Color.argb(150, 33, 150, 243)); // blue_500

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);

        finalHighlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        finalHighlightPaint.setColor(Color.argb(150, 94, 194, 165)); // red_500
        finalHighlightPaint.setStyle(Paint.Style.FILL);

        touchPoints = new ArrayList<>();
        handler = new Handler(Looper.getMainLooper());
        random = new Random();

        setupPulseAnimation();
    }

    private void setupPulseAnimation() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
        pulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    private void setupGlowAnimation() {
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }
        glowAnimator = ValueAnimator.ofFloat(0f, 1f);
        glowAnimator.setDuration(500);
        glowAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowAnimator.setRepeatMode(ValueAnimator.REVERSE);
        glowAnimator.addUpdateListener(animation -> {
            glowAlpha = (float) animation.getAnimatedValue();
            invalidate();
        });
        glowAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (glowAnimator != null) {
            glowAnimator.cancel();
        }
    }

    private void drawTouchPoint(Canvas canvas, PointF point, boolean isSelected) {
        float currentRadius = baseTouchRadius;  // 使用计算后的半径
        if (isSelected) {
            currentRadius = highlightRadius;
        }

        // 绘制涟漪效果
        float maxRippleRadius = currentRadius + (RIPPLE_COUNT * rippleSpacing);
        for (int i = 0; i < RIPPLE_COUNT; i++) {
            float rippleRadius = currentRadius + (i * rippleSpacing) + (pulseProgress * rippleSpacing);
            if (rippleRadius <= maxRippleRadius) {
                float alpha = isSelected ? 200 : 150;
                ripplePaint.setAlpha((int) (alpha * (1 - rippleRadius / maxRippleRadius)));
                canvas.drawCircle(point.x, point.y, rippleRadius, ripplePaint);
            }
        }

        // 绘制发光效果（仅在选中时）
        if (isSelected && isSelecting) {
            int glowColor = isFinalSelected ? Color.rgb(255, 87, 34) : Color.rgb(255, 215, 0);
            RadialGradient gradient = new RadialGradient(
                point.x, point.y, currentRadius * 1.5f,
                new int[]{Color.argb((int) (200 * glowAlpha), Color.red(glowColor), 
                    Color.green(glowColor), Color.blue(glowColor)),
                         Color.argb(0, Color.red(glowColor), 
                    Color.green(glowColor), Color.blue(glowColor))},
                null, Shader.TileMode.CLAMP);
            glowPaint.setShader(gradient);
            canvas.drawCircle(point.x, point.y, currentRadius * 1.5f, glowPaint);
            
            // 选中状态绘制实心圆
            float scale = 1f + (pulseProgress * (PULSE_SCALE - 1f));
            canvas.drawCircle(point.x, point.y, currentRadius * scale, 
                isFinalSelected ? finalHighlightPaint : highlightPaint);
        } else {
            // 非选中状态只绘制一个基础圆环
            canvas.drawCircle(point.x, point.y, currentRadius, touchPaint);
        }

        // 绘制序号
        int index = touchPoints.indexOf(point);
        if (index != -1) {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(isSelected ? 
                (isFinalSelected ? Color.WHITE : Color.WHITE) : 
                Color.rgb(33, 150, 243));
            textPaint.setTextSize(currentRadius * 0.7f); // 减小文字大小比例
            textPaint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float textHeight = fontMetrics.bottom - fontMetrics.top;
            float textBaseline = point.y + (textHeight / 2) - fontMetrics.bottom;
            canvas.drawText(String.valueOf(index + 1), point.x, textBaseline, textPaint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isPreparing || isSelecting) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        }

        for (PointF point : touchPoints) {
            drawTouchPoint(canvas, point, selectedPoint != null && 
                point.x == selectedPoint.x && point.y == selectedPoint.y);
        }
    }

    public void setTouchSelectListener(TouchSelectListener listener) {
        this.listener = listener;
    }

    public void setOnMaxTouchReachedListener(OnMaxTouchReachedListener listener) {
        this.maxTouchReachedListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isSelecting) return false;

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                int index = event.getActionIndex();
                float x = event.getX(index);
                float y = event.getY(index);
                
                // 检查是否已经存在相近的点
                boolean tooClose = false;
                for (PointF point : touchPoints) {
                    if (Math.abs(point.x - x) < baseTouchRadius * 1.5 && 
                        Math.abs(point.y - y) < baseTouchRadius * 1.5) {
                        tooClose = true;
                        break;
                    }
                }
                
                if (!tooClose) {
                    if (touchPoints.size() < MAX_TOUCH_POINTS) {
                        touchPoints.add(new PointF(x, y));
                        if (!isPreparing && touchPoints.size() == 1) {
                            startPreparation();
                        }
                    } else if (maxTouchReachedListener != null) {
                        maxTouchReachedListener.onMaxTouchReached();
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                index = event.getActionIndex();
                removePointAtIndex(event.getX(index), event.getY(index));
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                updateTouchPoints(event);
                invalidate();
                break;
        }
        return true;
    }

    private void removePointAtIndex(float x, float y) {
        for (int i = touchPoints.size() - 1; i >= 0; i--) {
            PointF point = touchPoints.get(i);
            if (Math.abs(point.x - x) < baseTouchRadius && 
                Math.abs(point.y - y) < baseTouchRadius) {
                touchPoints.remove(i);
                break;
            }
        }
    }

    private void updateTouchPoints(MotionEvent event) {
        List<PointF> newPoints = new ArrayList<>();
        for (int i = 0; i < Math.min(event.getPointerCount(), MAX_TOUCH_POINTS); i++) {
            float x = event.getX(i);
            float y = event.getY(i);
            
            boolean tooClose = false;
            for (PointF existingPoint : newPoints) {
                if (Math.abs(existingPoint.x - x) < baseTouchRadius * 2 && 
                    Math.abs(existingPoint.y - y) < baseTouchRadius * 2) {
                    tooClose = true;
                    break;
                }
            }
            
            if (!tooClose) {
                newPoints.add(new PointF(x, y));
            }
        }
        touchPoints = newPoints;
    }

    private void startPreparation() {
        isPreparing = true;
        startTime = System.currentTimeMillis();
        if (listener != null) {
            listener.onPreparationStart();
        }

        handler.postDelayed(this::startSelection, PREPARATION_TIME);
        invalidate();
    }

    private void startSelection() {
        if (touchPoints.isEmpty()) {
            reset();
            return;
        }

        isPreparing = false;
        isSelecting = true;
        isFinalSelected = false;
        setupGlowAnimation();
        
        if (listener != null) {
            listener.onSelectionStart();
        }

        // 随机选择过程动画
        final int[] currentIndex = {0};
        final int totalSteps = 20; // 增加切换次数到20次
        final long stepDuration = 150; // 每次切换间隔改为150毫秒
        final long[] durations = new long[totalSteps];
        
        // 生成渐变的时间间隔，开始快，结束慢
        for (int i = 0; i < totalSteps; i++) {
            durations[i] = stepDuration + (i * 25); // 每次多增加25毫秒
        }

        Handler animationHandler = new Handler();
        Runnable selectionAnimation = new Runnable() {
            @Override
            public void run() {
                if (currentIndex[0] < totalSteps) {
                    // 随机选择一个新位置
                    int randomIndex = random.nextInt(touchPoints.size());
                    selectedPoint = new PointF(
                        touchPoints.get(randomIndex).x,
                        touchPoints.get(randomIndex).y
                    );
                    invalidate();
                    currentIndex[0]++;
                    animationHandler.postDelayed(this, durations[currentIndex[0] - 1]);
                } else {
                    // 最终选择
                    int finalIndex = random.nextInt(touchPoints.size());
                    selectedPoint = new PointF(
                        touchPoints.get(finalIndex).x,
                        touchPoints.get(finalIndex).y
                    );
                    isFinalSelected = true;
                    if (listener != null) {
                        listener.onSelectionComplete(finalIndex);
                    }
                    handler.postDelayed(() -> {
                        if (glowAnimator != null) {
                            glowAnimator.cancel();
                        }
                        reset();
                    }, HIGHLIGHT_TIME);
                    invalidate();
                }
            }
        };

        animationHandler.post(selectionAnimation);
    }

    public void reset() {
        isPreparing = false;
        isSelecting = false;
        isFinalSelected = false;
        selectedPoint = null;
        touchPoints.clear();
        if (listener != null) {
            listener.onReset();
        }
        invalidate();
    }
} 