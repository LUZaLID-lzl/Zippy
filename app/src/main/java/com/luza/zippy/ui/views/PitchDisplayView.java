package com.luza.zippy.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

public class PitchDisplayView extends View {
    private Paint linePaint;
    private Paint currentPitchPaint;
    private Paint waveformPaint;
    private Paint textPaint;
    private Path pitchPath;
    private float currentPitch = -1;
    private static final int WAVEFORM_SIZE = 200;
    private float[] waveformData = new float[WAVEFORM_SIZE];
    private int waveformIndex = 0;
    private float maxAmplitude = 1.0f;
    
    private Scroller scroller;
    private GestureDetector gestureDetector;
    private float scrollY = 0;
    private static final float SCROLL_RANGE = 2000; // 滚动范围
    private static final int VISIBLE_NOTES = 24; // 同时显示的音符数量
    
    private String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final String[] STANDARD_NOTES = {"C", "D", "E", "F", "G", "A", "B"};
    private static final int[] SHARP_POSITIONS = {1, 3, -1, 6, 8, 10, -1}; // -1表示没有升号
    private static final int NOTES_PER_OCTAVE = 12;
    private static final int START_OCTAVE = 2; // 从C2开始
    private static final int END_OCTAVE = 6;   // 到C6结束

    public PitchDisplayView(Context context) {
        super(context);
        init(context);
    }

    public PitchDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        linePaint.setStrokeWidth(2);
        linePaint.setStyle(Paint.Style.STROKE);

        currentPitchPaint = new Paint();
        currentPitchPaint.setColor(Color.RED);
        currentPitchPaint.setStrokeWidth(4);
        currentPitchPaint.setStyle(Paint.Style.STROKE);

        waveformPaint = new Paint();
        waveformPaint.setColor(Color.BLUE);
        waveformPaint.setStrokeWidth(2);
        waveformPaint.setStyle(Paint.Style.STROKE);
        
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.RIGHT);

        pitchPath = new Path();
        
        scroller = new Scroller(context);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                scrollY = Math.max(0, Math.min(scrollY + distanceY, SCROLL_RANGE));
                invalidate();
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                scroller.fling(0, (int)scrollY, 0, -(int)velocityY, 0, 0, 0, (int)SCROLL_RANGE);
                invalidate();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollY = scroller.getCurrY();
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float width = getWidth();
        float totalNotes = (END_OCTAVE - START_OCTAVE + 1) * 7; // 每个八度7个基本音符
        float lineSpacing = height / VISIBLE_NOTES;

        // 绘制背景
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#1A1A1A"));
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // 绘制音高线和音名
        for (int i = 0; i < VISIBLE_NOTES; i++) {
            float y = height - (i * lineSpacing);
            
            // 计算当前显示的音符
            int currentPosition = (int)(i + scrollY/lineSpacing);
            int octave = START_OCTAVE + currentPosition / 7;
            int noteIndex = currentPosition % 7;
            String noteName = STANDARD_NOTES[noteIndex] + octave;

            // 绘制音高线
            Paint linePaint = new Paint();
            linePaint.setColor(Color.parseColor("#333333"));
            linePaint.setStrokeWidth(1);
            canvas.drawLine(width * 0.1f, y, width * 0.9f, y, linePaint);

            // 绘制音符名称
            Paint textPaint = new Paint();
            textPaint.setColor(Color.parseColor("#CCCCCC"));
            textPaint.setTextSize(30);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(noteName, width * 0.08f, y + textPaint.getTextSize()/3, textPaint);
        }

        // 绘制中心线
        Paint centerLinePaint = new Paint();
        centerLinePaint.setColor(Color.parseColor("#666666"));
        centerLinePaint.setStrokeWidth(2);
        float centerY = height / 2;
        canvas.drawLine(0, centerY, width, centerY, centerLinePaint);

        // 绘制当前音高指示线
        if (currentPitch >= 0) {
            float y = height - ((currentPitch - scrollY/lineSpacing) * lineSpacing);
            if (y >= 0 && y <= height) {
                // 绘制橙色指示线
                Paint pitchPaint = new Paint();
                pitchPaint.setColor(Color.parseColor("#FF8C00"));
                pitchPaint.setStrokeWidth(3);
                canvas.drawLine(width * 0.1f, y, width * 0.9f, y, pitchPaint);
                
                // 绘制音高偏差指示器
                float deviation = 0; // 这里可以添加音高偏差计算
                drawPitchDeviation(canvas, y, width * 0.9f, deviation);
            }
        }

        // 修改波形绘制部分
        Path wavePath = new Path();
        float waveWidth = getWidth() * 0.8f;
        float waveHeight = getHeight() * 0.2f;
        float waveY = getHeight() * 0.9f;
        float xStep = waveWidth / WAVEFORM_SIZE;

        wavePath.moveTo(0, waveY);
        float lastY = waveY;
        for (int i = 0; i < WAVEFORM_SIZE; i++) {
            int index = (waveformIndex + i) % WAVEFORM_SIZE;
            float x = i * xStep;
            float y = waveY - (waveformData[index] / maxAmplitude) * waveHeight;
            // 使用二次贝塞尔曲线使波形更平滑
            if (i > 0) {
                float midX = x - xStep / 2;
                float midY = (lastY + y) / 2;
                wavePath.quadTo(midX, lastY, x, y);
            } else {
                wavePath.moveTo(x, y);
            }
            lastY = y;
        }
        canvas.drawPath(wavePath, waveformPaint);
    }

    private void drawPitchDeviation(Canvas canvas, float y, float x, float deviation) {
        Paint deviationPaint = new Paint();
        deviationPaint.setColor(Color.parseColor("#FF8C00"));
        float indicatorHeight = 40;
        float indicatorWidth = 20;
        
        Path path = new Path();
        path.moveTo(x, y - indicatorHeight/2);
        path.lineTo(x + indicatorWidth, y);
        path.lineTo(x, y + indicatorHeight/2);
        path.close();
        
        canvas.drawPath(path, deviationPaint);
    }

    public void updatePitch(float pitch) {
        this.currentPitch = pitch;
        invalidate();
    }

    public void updateWaveform(float[] audioData) {
        // 计算RMS值作为波形振幅
        float rms = 0;
        for (float sample : audioData) {
            rms += sample * sample;
        }
        rms = (float) Math.sqrt(rms / audioData.length);
        
        // 平滑处理
        float smoothingFactor = 0.2f;
        if (maxAmplitude == 1.0f) {
            maxAmplitude = rms;
        } else {
            maxAmplitude = maxAmplitude * (1 - smoothingFactor) + rms * smoothingFactor;
        }

        // 更新波形数据
        for (int i = 0; i < audioData.length && i < WAVEFORM_SIZE; i++) {
            float value = audioData[i];
            // 应用平滑处理
            if (i < waveformData.length) {
                value = waveformData[i] * (1 - smoothingFactor) + value * smoothingFactor;
            }
            waveformData[waveformIndex] = value;
            waveformIndex = (waveformIndex + 1) % WAVEFORM_SIZE;
        }
        invalidate();
    }

    public void setScrollPosition(int position) {
        // 调整滑动范围，使其更平滑
        float lineSpacing = getHeight() / VISIBLE_NOTES;
        scrollY = position * (SCROLL_RANGE / 88f); // 88键钢琴的音符数量
        invalidate();
    }
} 