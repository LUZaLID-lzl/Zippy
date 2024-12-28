package com.luza.zippy.ui.views;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class LiquidBackgroundView extends View {
    private static final String TAG = "LiquidBackgroundView";
    private Paint paint;
    private Path path;
    private float phase = 0;
    private ValueAnimator waveAnimator;
    private ValueAnimator colorAnimator;
    private int[] currentColors = new int[4];
    private int[] startColors = {
            Color.parseColor("#1A1A1A"),
            Color.parseColor("#2D2D2D"),
            Color.parseColor("#404040"),
            Color.parseColor("#333333")
    };
    private int[] endColors = {
            Color.parseColor("#2D2D2D"),
            Color.parseColor("#404040"),
            Color.parseColor("#1A1A1A"),
            Color.parseColor("#404040")
    };
    private boolean isFirstDraw = true;

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
        System.arraycopy(startColors, 0, currentColors, 0, 4);

        // 波浪动画
        waveAnimator = ValueAnimator.ofFloat(0, 1);
        waveAnimator.setDuration(15000);
        waveAnimator.setRepeatCount(ValueAnimator.INFINITE);
        waveAnimator.setRepeatMode(ValueAnimator.REVERSE);
        waveAnimator.setInterpolator(new LinearInterpolator());
        waveAnimator.addUpdateListener(animation -> {
            phase = (float) animation.getAnimatedValue();
            invalidate();
        });

        // 颜色动画
        colorAnimator = ValueAnimator.ofFloat(0, 1);
        colorAnimator.setDuration(10000);
        colorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            ArgbEvaluator evaluator = new ArgbEvaluator();
            
            for (int i = 0; i < 4; i++) {
                currentColors[i] = (int) evaluator.evaluate(fraction, startColors[i], endColors[i]);
            }
            
            updateGradient();
            logCurrentColors();
            invalidate();
        });
    }

    /**
     * 将颜色整数值转换为6位十六进制颜色代码
     * @param color 颜色整数值
     * @return 6位十六进制颜色代码（不包含alpha通道）
     */
    private String colorToHex(int color) {
        // 提取RGB分量
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        // 转换为6位十六进制格式
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /**
     * 获取当前顶部颜色值
     * @return 6位十六进制颜色代码
     */
    public String getTopColor() {
        return colorToHex(currentColors[0]);
    }

    /**
     * 获取当前底部颜色值
     * @return 6位十六进制颜色代码
     */
    public String getBottomColor() {
        return colorToHex(currentColors[currentColors.length - 1]);
    }

    /**
     * 获取当前所有颜色值
     * @return 包含所有当前颜色的数组
     */
    public String[] getAllColors() {
        String[] colorStrings = new String[currentColors.length];
        for (int i = 0; i < currentColors.length; i++) {
            colorStrings[i] = colorToHex(currentColors[i]);
        }
        return colorStrings;
    }

    private void updateGradient() {
        if (getWidth() > 0 && getHeight() > 0) {
            RadialGradient gradient = new RadialGradient(
                    getWidth()/2f, getHeight()/2f, 
                    Math.max(getWidth(), getHeight())/1.5f,
                    currentColors,
                    null,
                    Shader.TileMode.CLAMP);
            paint.setShader(gradient);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateGradient();
    }

    /**
     * 获取颜色的ARGB信息
     * @param color 颜色整数值
     * @return 包含颜色信息的字符串
     */
    private String getColorInfo(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format("#%02X%02X%02X%02X (A:%d, R:%d, G:%d, B:%d)",
                alpha, red, green, blue, alpha, red, green, blue);
    }

    /**
     * 打印当前颜色值
     */
    private void logCurrentColors() {
        Log.d(TAG, "Current colors:");
        Log.d(TAG, "Top color: " + getTopColor() + " " + getColorInfo(currentColors[0]));
        Log.d(TAG, "Bottom color: " + getBottomColor() + " " + getColorInfo(currentColors[currentColors.length - 1]));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 首次绘制时打印颜色信息
        if (isFirstDraw) {
            isFirstDraw = false;
            Log.d(TAG, "Initial colors:");
            Log.d(TAG, "Top color: " + getTopColor() + " " + getColorInfo(currentColors[0]));
            Log.d(TAG, "Bottom color: " + getBottomColor() + " " + getColorInfo(currentColors[currentColors.length - 1]));
            Log.d(TAG, "All colors:");
            for (int i = 0; i < currentColors.length; i++) {
                Log.d(TAG, "Color " + (i + 1) + ": " + colorToHex(currentColors[i]) + " " + getColorInfo(currentColors[i]));
            }
        }

        int width = getWidth();
        int height = getHeight();

        path.reset();
        path.moveTo(0, 0);
        
        float amplitude = height * 0.05f;
        float wavelength = width * 1.5f;
        
        // 第一层波浪
        for (float x = 0; x <= width + wavelength; x += 5) {
            double y = amplitude * Math.sin(2 * Math.PI * (x / wavelength + phase));
            y += amplitude * Math.cos(2 * Math.PI * (x / (wavelength * 0.7) + phase * 1.3));
            path.lineTo(x, height/2f + (float)y);
        }
        
        // 第二层波浪
        for (float x = width; x >= -wavelength; x -= 5) {
            double y = amplitude * Math.cos(2 * Math.PI * (x / wavelength + phase * 0.8));
            y += amplitude * Math.sin(2 * Math.PI * (x / (wavelength * 0.9) + phase * 1.1));
            path.lineTo(x, height/2f + (float)y + amplitude);
        }

        path.lineTo(0, height);
        path.close();

        canvas.drawPath(path, paint);
        
        // 渐变叠加层
        Paint overlayPaint = new Paint();
        overlayPaint.setShader(new LinearGradient(
                0, 0, width, height,
                new int[]{
                        Color.argb(50, 0, 0, 0),
                        Color.argb(30, 0, 0, 0),
                        Color.argb(50, 0, 0, 0)
                },
                null,
                Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, width, height, overlayPaint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        waveAnimator.start();
        colorAnimator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        waveAnimator.cancel();
        colorAnimator.cancel();
    }

    public void setColors(int[] newStartColors, int[] newEndColors) {
        if (newStartColors != null && newStartColors.length == 4 &&
            newEndColors != null && newEndColors.length == 4) {
            System.arraycopy(newStartColors, 0, startColors, 0, 4);
            System.arraycopy(newEndColors, 0, endColors, 0, 4);
            System.arraycopy(startColors, 0, currentColors, 0, 4);
            updateGradient();
        }
    }
} 