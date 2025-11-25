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
    
    // 颜色变化监听器接口
    public interface ColorChangeListener {
        void onColorChanged(String topColor, String bottomColor, String actualTopColor, String actualBottomColor);
    }
    
    private Paint paint;
    private Path path;
    private float phase = 0;
    private ValueAnimator waveAnimator;
    private ValueAnimator colorAnimator;
    private int[] currentColors = new int[4];
    private ColorChangeListener colorChangeListener;
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
            
            // 通知颜色变化
            if (colorChangeListener != null) {
                colorChangeListener.onColorChanged(
                    getTopColor(), 
                    getBottomColor(),
                    getActualDisplayedTopColor(),
                    getActualDisplayedBottomColor()
                );
            }
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
     * 获取实际显示的底部颜色值（考虑动画插值和叠加效果）
     * @return 6位十六进制颜色代码
     */
    public String getActualDisplayedBottomColor() {
        // 获取当前动画进度
        float fraction = 0;
        if (colorAnimator != null && colorAnimator.isRunning()) {
            fraction = colorAnimator.getAnimatedFraction();
        }
        
        // 计算实际显示的颜色（考虑动画插值）
        ArgbEvaluator evaluator = new ArgbEvaluator();
        int actualColor = (int) evaluator.evaluate(fraction, startColors[startColors.length - 1], endColors[endColors.length - 1]);
        
        // 考虑白色叠加效果（约30%透明度）
        int red = Color.red(actualColor);
        int green = Color.green(actualColor);
        int blue = Color.blue(actualColor);
        
        // 添加白色叠加效果（约30%透明度）
        red = Math.min(255, red + (int)((255 - red) * 0.3));
        green = Math.min(255, green + (int)((255 - green) * 0.3));
        blue = Math.min(255, blue + (int)((255 - blue) * 0.3));
        
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /**
     * 获取实际显示的顶部颜色值（考虑动画插值和叠加效果）
     * @return 6位十六进制颜色代码
     */
    public String getActualDisplayedTopColor() {
        // 获取当前动画进度
        float fraction = 0;
        if (colorAnimator != null && colorAnimator.isRunning()) {
            fraction = colorAnimator.getAnimatedFraction();
        }
        
        // 计算实际显示的颜色（考虑动画插值）
        ArgbEvaluator evaluator = new ArgbEvaluator();
        int actualColor = (int) evaluator.evaluate(fraction, startColors[0], endColors[0]);
        
        // 考虑白色叠加效果（约30%透明度）
        int red = Color.red(actualColor);
        int green = Color.green(actualColor);
        int blue = Color.blue(actualColor);
        
        // 添加白色叠加效果（约30%透明度）
        red = Math.min(255, red + (int)((255 - red) * 0.3));
        green = Math.min(255, green + (int)((255 - green) * 0.3));
        blue = Math.min(255, blue + (int)((255 - blue) * 0.3));
        
        return String.format("#%02X%02X%02X", red, green, blue);
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

    /**
     * 设置颜色变化监听器
     * @param listener 颜色变化监听器
     */
    public void setColorChangeListener(ColorChangeListener listener) {
        this.colorChangeListener = listener;
    }

    private void updateGradient() {
        if (currentColors == null || currentColors.length < 2) {
            Log.e(TAG, "Invalid colors array");
            return;
        }
        
        int width = getWidth();
        int height = getHeight();
        
        if (width <= 0 || height <= 0) {
            Log.e(TAG, "Invalid dimensions: width=" + width + ", height=" + height);
            return;
        }
        
        // 使用径向渐变
        RadialGradient gradient = new RadialGradient(
            width/2f, height/2f,
            Math.max(width, height)/1.5f,
            currentColors,
            null,
            Shader.TileMode.CLAMP
        );
        
        paint.setShader(gradient);
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
        
        int width = getWidth();
        int height = getHeight();

        // 确保画笔和渐变是正确设置的
        if (paint == null) {
            paint = new Paint();
            paint.setAntiAlias(true);
        }
        
        // 更新渐变
        updateGradient();

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

        // 使用渐变色绘制路径
        canvas.drawPath(path, paint);
        
        // 渐变叠加层
        Paint overlayPaint = new Paint();
        overlayPaint.setShader(new LinearGradient(
            0, 0, width, height,
            new int[]{
                Color.argb(50, 255, 255, 255),
                Color.argb(30, 255, 255, 255),
                Color.argb(50, 255, 255, 255)
            },
            null,
            Shader.TileMode.CLAMP
        ));
        canvas.drawRect(0, 0, width, height, overlayPaint);
        
        // 打印当前颜色信息
        logCurrentColors();
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
        Log.d(TAG, "setColors called");
        if (newStartColors != null && newStartColors.length == 4 &&
            newEndColors != null && newEndColors.length == 4) {

            System.arraycopy(newStartColors, 0, startColors, 0, 4);
            System.arraycopy(newEndColors, 0, endColors, 0, 4);
            System.arraycopy(startColors, 0, currentColors, 0, 4);

            // 打印新的颜色值
            Log.d(TAG, "Setting new colors:");
            Log.d(TAG, "Start colors:");
            for (int i = 0; i < newStartColors.length; i++) {
                Log.d(TAG, "Color " + i + ": " + colorToHex(newStartColors[i]));
            }
            Log.d(TAG, "End colors:");
            for (int i = 0; i < newEndColors.length; i++) {
                Log.d(TAG, "Color " + i + ": " + colorToHex(newEndColors[i]));
            }

            // 更新渐变
            updateGradient();
            
            // 重启动画
            if (colorAnimator != null) {
                colorAnimator.cancel();
                colorAnimator.start();
            }
            if (waveAnimator != null) {
                waveAnimator.cancel();
                waveAnimator.start();
            }
            
            // 强制重绘
            invalidate();
        } else {
            Log.e(TAG, "Invalid color arrays provided");
            if (newStartColors == null || newEndColors == null) {
                Log.e(TAG, "Color arrays are null");
            } else {
                Log.e(TAG, "Color arrays length: start=" + newStartColors.length + ", end=" + newEndColors.length);
            }
        }
    }
} 