package com.luza.zippy.ui.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TurntableView extends View {
    private Paint paint;
    private RectF rectF;
    private float currentRotation = 0f;
    private List<String> options = new ArrayList<>();
    private List<Integer> optionColors = new ArrayList<>();
    private String currentPassingOption = "";
    private String previousPassingOption = "";
    private float textAlpha = 255f;  // 文字透明度
    private ValueAnimator textAnimator;  // 文字动画
    private static final int[] PASTEL_COLORS = {
        Color.parseColor("#FFB6C1"),  // 浅粉红
        Color.parseColor("#98FB98"),  // 浅绿色
        Color.parseColor("#87CEFA"),  // 浅蓝色
        Color.parseColor("#DDA0DD"),  // 梅红色
        Color.parseColor("#F0E68C"),  // 卡其色
        Color.parseColor("#E6E6FA"),  // 淡紫色
        Color.parseColor("#FFA07A"),  // 浅鲑鱼色
        Color.parseColor("#20B2AA"),  // 浅海洋绿
        Color.parseColor("#87CEEB"),  // 天蓝色
        Color.parseColor("#FFA500"),  // 橙色
        Color.parseColor("#98FF98"),  // 薄荷色
        Color.parseColor("#FFB6C1"),  // 粉红色
        Color.parseColor("#FFDAB9"),  // 桃色
        Color.parseColor("#B0E0E6"),  // 粉蓝色
        Color.parseColor("#FF69B4"),  // 热粉红
        Color.parseColor("#4169E1"),  // 皇家蓝
        Color.parseColor("#7B68EE"),  // 中紫色
        Color.parseColor("#00FA9A"),  // 春绿色
        Color.parseColor("#F08080"),  // 浅珊瑚色
        Color.parseColor("#BA55D3"),  // 中兰花紫
        Color.parseColor("#FFE4E1"),  // 薄雾玫瑰
        Color.parseColor("#E0FFFF"),  // 淡青色
        Color.parseColor("#FAFAD2"),  // 浅秋麒麟黄
        Color.parseColor("#D8BFD8"),  // 蓟色
        Color.parseColor("#DEB887"),  // 实木色
        Color.parseColor("#ADD8E6"),  // 亮蓝色
        Color.parseColor("#F0FFF0"),  // 蜜露色
        Color.parseColor("#FFF0F5"),  // 淡紫红
        Color.parseColor("#E6E6FA"),  // 薰衣草色
        Color.parseColor("#FFE4B5"),  // 莫卡辛色
        Color.parseColor("#F0FFFF"),  // 天青色
        Color.parseColor("#F5F5DC"),  // 米色
        Color.parseColor("#FFEFD5"),  // 蜜桃色
        Color.parseColor("#FDF5E6"),  // 老花色
        Color.parseColor("#F0F8FF"),  // 爱丽丝蓝
        Color.parseColor("#F8F8FF"),  // 幽灵白
        Color.parseColor("#F5F5F5"),  // 白烟色
        Color.parseColor("#FFF5EE"),  // 海贝壳色
        Color.parseColor("#F5FFFA"),  // 薄荷奶油
        Color.parseColor("#FFFAF0")   // 花白色
    };
    private ValueAnimator rotationAnimator;
    private OnRotationEndListener rotationEndListener;
    private int currentHighlightIndex = -1;  // 当前高亮的扇形索引
    private boolean isFinished = false;      // 转盘是否已停止
    private int selectedIndex = -1;          // 最终选中的扇形索引

    public interface OnRotationEndListener {
        void onRotationEnd(String selectedOption);
    }

    public TurntableView(Context context) {
        super(context);
        init();
    }

    public TurntableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        rectF = new RectF();
        
        // 添加点击事件监听
        setOnClickListener(v -> {
            if (!isRotating()) {
                startRotation();
            }
        });
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
        this.optionColors.clear();
        
        // 创建已使用颜色的列表
        List<Integer> usedColorIndices = new ArrayList<>();
        Random random = new Random();
        
        // 为每个选项分配不重复的颜色
        for (int i = 0; i < options.size(); i++) {
            int colorIndex;
            do {
                colorIndex = random.nextInt(PASTEL_COLORS.length);
            } while (usedColorIndices.contains(colorIndex) && usedColorIndices.size() < PASTEL_COLORS.length);
            
            usedColorIndices.add(colorIndex);
            this.optionColors.add(PASTEL_COLORS[colorIndex]);
        }
        
        invalidate();
    }

    public void setOnRotationEndListener(OnRotationEndListener listener) {
        this.rotationEndListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int minSize = Math.min(w, h - 100);  // 预留底部文本空间
        int centerX = w / 2;
        int centerY = (h - 100) / 2;  // 转盘中心上移
        int radius = minSize / 2;
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (options.isEmpty()) return;

        float sweepAngle = 360f / options.size();
        float textRadius = rectF.width() * 0.35f;

        // 绘制白色背景
        paint.setColor(Color.WHITE);
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2, paint);

        // 创建一个较小的矩形用于绘制扇形，留出外圈的空间
        float ringWidth = rectF.width() * 0.02f;
        RectF innerRectF = new RectF(
            rectF.left + ringWidth,
            rectF.top + ringWidth,
            rectF.right - ringWidth,
            rectF.bottom - ringWidth
        );

        canvas.save();
        canvas.rotate(currentRotation, rectF.centerX(), rectF.centerY());

        for (int i = 0; i < options.size(); i++) {
            // 绘制扇形
            paint.setColor(optionColors.get(i));
            paint.setStyle(Paint.Style.FILL);
            
            // 如果正在旋转或已停止，降低非高亮扇形的亮度
            if ((isRotating() || isFinished) && i != currentHighlightIndex && i != selectedIndex) {
                int color = optionColors.get(i);
                int r = Color.red(color);
                int g = Color.green(color);
                int b = Color.blue(color);
                // 降低亮度到50%
                r = (int)(r * 0.5f);
                g = (int)(g * 0.5f);
                b = (int)(b * 0.5f);
                paint.setColor(Color.rgb(r, g, b));
            }
            
            // 先绘制扇形填充
            canvas.drawArc(innerRectF, i * sweepAngle, sweepAngle, true, paint);
            
            // 如果是当前高亮的扇形，显著增加亮度并添加描边
            if (i == currentHighlightIndex || (isFinished && i == selectedIndex)) {
                // 绘制发光效果（外层描边）
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(ringWidth * 1.1f);
                paint.setColor(Color.WHITE);
                canvas.drawArc(innerRectF, i * sweepAngle, sweepAngle, true, paint);
                
                // 绘制内层描边
                //paint.setStrokeWidth(ringWidth * 0.8f);
                int color = optionColors.get(i);
                // 增加亮度到150%
                int r = Math.min(255, (int)(Color.red(color) * 1.5f));
                int g = Math.min(255, (int)(Color.green(color) * 1.5f));
                int b = Math.min(255, (int)(Color.blue(color) * 1.5f));
                // 添加白色混合以增加亮度
                r = Math.min(255, r + 40);
                g = Math.min(255, g + 40);
                b = Math.min(255, b + 40);
                paint.setColor(Color.rgb(r, g, b));
                canvas.drawArc(innerRectF, i * sweepAngle, sweepAngle, true, paint);
                
                paint.setStyle(Paint.Style.FILL);
            }

            // 绘制文字
            paint.setColor(Color.BLACK);
            float textSize = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                16f, // 基准文字大小为16sp
                getResources().getDisplayMetrics()
            );
            paint.setTextSize(textSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);

            // 计算每个扇形可用的最大宽度（弧长）
            float arcLength = (float) (2 * Math.PI * textRadius * sweepAngle / 360);
            String text = options.get(i);
            float textWidth = paint.measureText(text);

            // 如果文字宽度超过可用宽度，截断文字并添加省略号
            if (textWidth > arcLength * 0.8f) { // 留出20%的边距
                float ellipsisWidth = paint.measureText("...");
                float availableWidth = arcLength * 0.8f - ellipsisWidth;
                int length = text.length();
                while (length > 0 && paint.measureText(text.substring(0, length)) > availableWidth) {
                    length--;
                }
                text = text.substring(0, length) + "...";
            }

            float angle = (float) Math.toRadians((i * sweepAngle) + (sweepAngle / 2));
            float x = rectF.centerX() + (float) (textRadius * Math.cos(angle));
            float y = rectF.centerY() + (float) (textRadius * Math.sin(angle));

            canvas.save();
            float textRotation = i * sweepAngle + (sweepAngle / 2) + 90;
            canvas.rotate(textRotation, x, y);
            canvas.drawText(text, x, y, paint);
            canvas.restore();
        }

        canvas.restore();

        // 绘制外围白色圆环
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(ringWidth * 2);  // 圆环宽度
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2 - ringWidth, paint);
        paint.setStyle(Paint.Style.FILL);

        // 绘制中心圆形按钮
        float centerCircleRadius = rectF.width() * 0.15f;
        
        // 绘制指针三角形
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        Path trianglePath = new Path();
        float triangleHeight = centerCircleRadius * 0.2f;  // 三角形高度
        float triangleBase = centerCircleRadius * 0.2f;    // 三角形底边长度的一半
        
        // 计算三角形的三个顶点
        float topX = rectF.centerX();
        float topY = rectF.centerY() - centerCircleRadius - triangleHeight;  // 在中心圆上方
        float leftX = rectF.centerX() - triangleBase;
        float leftY = rectF.centerY() - centerCircleRadius + 5;  // 与中心圆相切
        float rightX = rectF.centerX() + triangleBase;
        float rightY = rectF.centerY() - centerCircleRadius + 5;
        
        // 绘制三角形
        trianglePath.moveTo(topX, topY);     // 顶点
        trianglePath.lineTo(leftX, leftY);   // 左下角
        trianglePath.lineTo(rightX, rightY); // 右下角
        trianglePath.close();
        canvas.drawPath(trianglePath, paint);
        
        // 绘制外圈白色圆
        paint.setColor(Color.WHITE);
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), centerCircleRadius, paint);
        
        // 绘制内圈橙色圆（始终显示）
        paint.setColor(Color.parseColor("#FFA07A"));
        canvas.drawCircle(rectF.centerX(), rectF.centerY(), centerCircleRadius * 0.9f, paint);

        // 只在非旋转状态显示"开始"文字
        if (!isRotating()) {
            paint.setColor(Color.WHITE);
            float textSize1 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                16f, // 基准文字大小为16sp
                getResources().getDisplayMetrics()
            );
            paint.setTextSize(textSize1);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setFakeBoldText(true);
            float textY = rectF.centerY() + paint.getTextSize() / 3;
            canvas.drawText("开始", rectF.centerX(), textY, paint);
        }

        // 在转盘底部绘制当前选项文本
        paint.setColor(Color.BLACK);
        float textSize2 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            32f, // 基准文字大小为16sp
            getResources().getDisplayMetrics()
        );
        paint.setTextSize(textSize2);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(true);
        float bottomTextY = rectF.bottom + 100;

        // 绘制渐出的上一个选项
        if (textAnimator != null && textAnimator.isRunning() && !previousPassingOption.isEmpty()) {
            paint.setAlpha(255 - (int)textAlpha);
            canvas.drawText(previousPassingOption, rectF.centerX(), bottomTextY, paint);
        }
        
        // 绘制渐入的当前选项
        paint.setAlpha((int)textAlpha);
        canvas.drawText(currentPassingOption, rectF.centerX(), bottomTextY, paint);
        paint.setAlpha(255);  // 恢复透明度
    }

    public void startRotation() {
        if (options.size() < 2 || isRotating()) return;

        isFinished = false;
        selectedIndex = -1;
        Random random = new Random();
        int targetIndex = random.nextInt(options.size());
        
        float baseRotation = 360f * (8 + random.nextInt(5));
        float extraAngle = random.nextFloat() * 360f;
        float sweepAngle = 360f / options.size();
        float targetRotation = baseRotation + extraAngle - (targetIndex * sweepAngle) - (sweepAngle / 2) + 270;

        rotationAnimator = ValueAnimator.ofFloat(0f, targetRotation);
        rotationAnimator.setDuration(6000 + random.nextInt(2000));
        
        rotationAnimator.setInterpolator(t -> {
            float x = t * 2.0f;
            if (x < 1f) {
                return 0.5f * x * x;
            }
            x--;
            return -0.5f * (x * (x - 2) - 1);
        });
        
        rotationAnimator.addUpdateListener(animation -> {
            currentRotation = (float) animation.getAnimatedValue();
            
            // 计算当前指向的选项
            float currentAngle = currentRotation % 360;
            if (currentAngle < 0) currentAngle += 360;
            int currentIndex = (int) (((270 - currentAngle + 360) % 360) / sweepAngle);
            currentIndex = currentIndex % options.size();
            
            // 更新高亮扇形
            currentHighlightIndex = currentIndex;
            updatePassingOption(options.get(currentIndex));
            
            invalidate();

            if (animation.getAnimatedFraction() == 1f) {
                isFinished = true;
                selectedIndex = currentIndex;
                if (rotationEndListener != null) {
                    rotationEndListener.onRotationEnd(currentPassingOption);
                }
            }
        });
        
        previousPassingOption = currentPassingOption;
        currentPassingOption = "";
        textAlpha = 255f;
        rotationAnimator.start();
    }

    public boolean isRotating() {
        return rotationAnimator != null && rotationAnimator.isRunning();
    }

    private void updatePassingOption(String newOption) {
        if (!newOption.equals(currentPassingOption)) {
            previousPassingOption = currentPassingOption;
            currentPassingOption = newOption;
            
            // 创建文字渐变动画
            if (textAnimator != null) {
                textAnimator.cancel();
            }
            
            textAnimator = ValueAnimator.ofFloat(0f, 1f);
            textAnimator.setDuration(200);  // 200ms的动画时长
            textAnimator.addUpdateListener(animation -> {
                textAlpha = (float) animation.getAnimatedValue() * 255f;
                invalidate();
            });
            textAnimator.start();
        }
    }
} 