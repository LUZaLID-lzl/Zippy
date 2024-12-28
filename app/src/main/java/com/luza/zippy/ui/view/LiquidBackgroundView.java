package com.luza.zippy.ui.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LiquidBackgroundView extends View {
    private Paint paint;
    private Path path;
    private Matrix matrix;
    private float phase = 0;
    private ValueAnimator animator;
    private int[] colors = {
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4")
    };

    public LiquidBackgroundView(Context context) {
        super(context);
        init();
    }

    public LiquidBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        
        path = new Path();
        matrix = new Matrix();

        // 创建动画
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(10000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 创建渐变
        LinearGradient gradient = new LinearGradient(
                0, 0, w, h,
                colors,
                null,
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        // 清除路径
        path.reset();
        
        // 创建波浪路径
        path.moveTo(0, height/2f);
        
        // 第一层波浪
        float amplitude1 = height * 0.1f;
        float wavelength1 = width * 0.8f;
        for (float x = 0; x <= width + wavelength1; x += 5) {
            float y = (float) (amplitude1 * Math.sin(2 * Math.PI * (x / wavelength1 + phase)));
            path.lineTo(x, height/2f + y);
        }
        
        // 第二层波浪
        float amplitude2 = height * 0.15f;
        float wavelength2 = width * 1.2f;
        for (float x = width; x >= -wavelength2; x -= 5) {
            float y = (float) (amplitude2 * Math.cos(2 * Math.PI * (x / wavelength2 + phase * 1.2)));
            path.lineTo(x, height/2f + y + amplitude1);
        }

        // 闭合路径
        path.lineTo(0, height/2f);
        path.close();

        // 绘制路径
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        animator.cancel();
    }

    public void setColors(int[] newColors) {
        this.colors = newColors;
        invalidate();
    }
} 