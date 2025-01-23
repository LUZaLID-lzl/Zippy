package com.luza.zippy.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import androidx.core.content.ContextCompat;
import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.fragments.HomeFragment;

import android.animation.ValueAnimator;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SparkView extends androidx.appcompat.widget.AppCompatImageView {
    private final Random random = new Random();
    private final int screenWidth;
    private final int screenHeight;
    private final int centerX;
    private final int centerY;
    private final Paint trailPaint;
    private final List<Point> trailPoints;
    private static final int TRAIL_LENGTH = 10;
    private final float rotation;

    private ShardPerfenceSetting shardPerfenceSetting;

    public int[] capooSpark = {
            R.drawable.ic_hamburg,
            R.drawable.ic_cola,
            R.drawable.ic_fries
    };

    private static class Point {
        float x, y;
        Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public SparkView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.centerX = screenWidth / 2;
        this.centerY = screenHeight / 4;
        // 设置Z轴层级高于按钮
        setZ(10f);
        
        // 设置随机旋转角度
        this.rotation = random.nextFloat() * 360;
        setRotation(rotation);

        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());
        switch (shardPerfenceSetting.getHomeTheme()){
            case "pikachu":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_spark));
                break;
            case "bulbasaur":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_water_drop));
                break;
            case "squirtle":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_leaf));
                break;
            case "mew":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_light_particle));
                break;
            case "karsa":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_peach));
                break;
            case "capoo":
                setImageDrawable(ContextCompat.getDrawable(context, HomeFragment.capoo));
                break;
            case "maple":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_maple));
                break;
            case "winter":
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_snowflake2));
                break;
            default:
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_spark));
                break;
        }


        setScaleX(3.0f);
        setScaleY(3.0f);

        // 初始化轨迹画笔
        trailPaint = new Paint();
        trailPaint.setColor(0xFFFFD700); // 金黄色
        trailPaint.setStyle(Paint.Style.STROKE);
        trailPaint.setStrokeWidth(4);
        trailPaint.setAlpha(100);
        trailPoints = new ArrayList<>();

        setLayerType(LAYER_TYPE_HARDWARE, null);
        setupSparkAnimation();
    }

    private void setupSparkAnimation() {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        // 决定闪电的起始位置
        int startX, startY;
        
        // 50%的概率从底部生成
        if (random.nextFloat() < 0.5f) {
            // 从底部开始，X坐标随机
            startX = random.nextInt(screenWidth);
            startY = screenHeight;
        } else {
            // 剩余30%概率从其他三个边生成
            if (random.nextBoolean()) {
                // 从左边或右边开始
                startX = random.nextBoolean() ? 0 : screenWidth;
                startY = random.nextInt(screenHeight);
            } else {
                // 从顶部开始
                startX = random.nextInt(screenWidth);
                startY = 0;
            }
        }

        setX(startX);
        setY(startY);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500 + random.nextInt(1000));  // 动画持续时间
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            
            // 计算当前位置
            float tx = startX + (centerX - startX) * fraction;
            float ty = startY + (centerY - startY) * fraction;
            
            // 添加随机抖动效果
            float wobble = (random.nextFloat() - 0.5f) * 20;
            tx += wobble * (1 - fraction);
            ty += wobble * (1 - fraction);
            
            setX(tx);
            setY(ty);

            // 检查是否在按钮区域内
            boolean inButtonArea = isInButtonArea(tx, ty);
            setVisibility(inButtonArea ? INVISIBLE : VISIBLE);
            
            // 添加轨迹点
            trailPoints.add(new Point(tx + getWidth() / 2, ty + getHeight() / 2));
            if (trailPoints.size() > TRAIL_LENGTH) {
                trailPoints.remove(0);
            }
            
            // 设置透明度和缩放
            trailPaint.setAlpha((int)(100 * (1 - fraction)));
            float scale = 2.0f * (1 - fraction * 0.7f);
            setScaleX(scale);
            setScaleY(scale);
            setAlpha(1 - fraction);

            // 添加旋转效果
            float currentRotation = rotation + fraction * 360 * (random.nextBoolean() ? 1 : -1);
            setRotation(currentRotation);
            
            invalidate();
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (getParent() != null) {
                    ((ViewGroup) getParent()).removeView(SparkView.this);
                }
            }
        });

        animator.start();
    }

    // 判断是否在按钮区域内
    private boolean isInButtonArea(float x, float y) {
        // 底部按钮区域
        if (y > screenHeight - 150) {  // 按钮区域的顶部边界
            return true;
        }
        
        // 左上角菜单按钮区域
        if (y < 100 && x < 100) {  // 菜单按钮的区域
            return true;
        }
        
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (trailPoints.size() > 1) {
            Path trailPath = new Path();
            Point first = trailPoints.get(0);
            trailPath.moveTo(first.x, first.y);
            
            for (int i = 1; i < trailPoints.size(); i++) {
                Point point = trailPoints.get(i);
                trailPath.lineTo(point.x, point.y);
            }
            
            canvas.drawPath(trailPath, trailPaint);
        }
    }
} 