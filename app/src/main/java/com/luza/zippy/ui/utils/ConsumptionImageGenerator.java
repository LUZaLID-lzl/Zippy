package com.luza.zippy.ui.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.luza.zippy.data.entity.ConsumptionRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ConsumptionImageGenerator {
    private static final int PADDING = 60;
    private static final int HEADER_HEIGHT = 180;
    private static final int ITEM_HEIGHT = 80;
    private static final int DATE_HEADER_HEIGHT = 50;
    private static final int TEXT_SIZE_SMALL = 28;
    private static final int TEXT_SIZE_NORMAL = 32;
    private static final int TEXT_SIZE_LARGE = 40;
    private static final int TEXT_SIZE_TITLE = 48;
    private static final int TEXT_SIZE_SUBTITLE = 36;
    
    public static void generateAndSaveImage(Context context, List<ConsumptionRecord> pendingRecords, double totalAmount) {
        try {
            android.util.Log.d("ImageGenerator", "开始生成图片");
            
            // 按日期分组记录
            Map<String, List<ConsumptionRecord>> groupedRecords = groupRecordsByDate(pendingRecords);
            
            int width = 1200;  // 固定宽度
            int recordCount = pendingRecords.size();
            int groupCount = groupedRecords.size();
            int height = HEADER_HEIGHT + (recordCount * ITEM_HEIGHT) + (groupCount * DATE_HEADER_HEIGHT) + (PADDING * 3);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            
            // 设置背景
            Paint backgroundPaint = new Paint();
            backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
            canvas.drawRect(0, 0, width, height, backgroundPaint);
            
            // 创建画笔
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            
            // 绘制主卡片背景
            Paint cardPaint = new Paint();
            cardPaint.setColor(Color.WHITE);
            cardPaint.setShadowLayer(8, 0, 4, Color.parseColor("#20000000"));
            canvas.drawRect(PADDING - 10, PADDING - 10, width - PADDING + 10, height - PADDING + 10, cardPaint);
            
            // 绘制标题区域背景
            Paint titleBgPaint = new Paint();
            titleBgPaint.setColor(Color.parseColor("#1976D2"));
            canvas.drawRect(PADDING, PADDING, width - PADDING, PADDING + HEADER_HEIGHT, titleBgPaint);
            
            // 绘制标题
            paint.setTextSize(TEXT_SIZE_TITLE);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            String title = "待归还消费清单";
            canvas.drawText(title, width / 2f, PADDING + 60, paint);
            
            // 绘制总金额
            paint.setTextSize(TEXT_SIZE_SUBTITLE);
            String totalText = String.format(Locale.getDefault(), "总金额: ¥%.2f", totalAmount);
            canvas.drawText(totalText, width / 2f, PADDING + 100, paint);
            
            // 绘制生成时间
            paint.setTextSize(TEXT_SIZE_SMALL);
            paint.setColor(Color.parseColor("#E3F2FD"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
            String dateText = "生成时间: " + dateFormat.format(new Date());
            canvas.drawText(dateText, width / 2f, PADDING + 130, paint);
            
            // 绘制记录数量
            String recordCountText = String.format(Locale.getDefault(), "共 %d 条记录", recordCount);
            canvas.drawText(recordCountText, width / 2f, PADDING + 155, paint);
            
            // 绘制内容区域背景
            Paint contentBgPaint = new Paint();
            contentBgPaint.setColor(Color.WHITE);
            canvas.drawRect(PADDING, PADDING + HEADER_HEIGHT, width - PADDING, height - PADDING, contentBgPaint);
            
            // 绘制表头
            int y = PADDING + HEADER_HEIGHT + 40;
            
            // 绘制表头背景
            Paint headerBgPaint = new Paint();
            headerBgPaint.setColor(Color.parseColor("#F8F9FA"));
            canvas.drawRect(PADDING, y - 30, width - PADDING, y + 10, headerBgPaint);
            
            // 绘制表头文字
            paint.setTextSize(TEXT_SIZE_NORMAL);
            paint.setColor(Color.parseColor("#424242"));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStyle(Paint.Style.FILL);
            
            canvas.drawText("时间", PADDING + 20, y, paint);
            canvas.drawText("用途", PADDING + 200, y, paint);
            
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("金额", width - PADDING - 20, y, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            
            // 绘制表头分割线
            Paint linePaint = new Paint();
            linePaint.setColor(Color.parseColor("#E0E0E0"));
            linePaint.setStrokeWidth(2);
            canvas.drawLine(PADDING, y + 15, width - PADDING, y + 15, linePaint);
            
            y += 50;
            
            // 绘制记录
            paint.setTextSize(TEXT_SIZE_NORMAL);
            int recordIndex = 0;
            
            for (Map.Entry<String, List<ConsumptionRecord>> entry : groupedRecords.entrySet()) {
                // 绘制日期标题背景
                Paint dateBgPaint = new Paint();
                dateBgPaint.setColor(Color.parseColor("#E3F2FD"));
                canvas.drawRect(PADDING, y - 5, width - PADDING, y + 35, dateBgPaint);
                
                // 绘制日期标题
                paint.setColor(Color.parseColor("#1976D2"));
                paint.setTextSize(TEXT_SIZE_LARGE);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawText(entry.getKey(), PADDING + 20, y + 20, paint);
                y += DATE_HEADER_HEIGHT;
                
                // 绘制该日期下的记录
                paint.setColor(Color.parseColor("#424242"));
                paint.setTextSize(TEXT_SIZE_NORMAL);
                for (ConsumptionRecord record : entry.getValue()) {
                    // 绘制行背景（交替颜色）
                    if (recordIndex % 2 == 0) {
                        Paint rowBgPaint = new Paint();
                        rowBgPaint.setColor(Color.parseColor("#FAFAFA"));
                        canvas.drawRect(PADDING, y - 5, width - PADDING, y + 35, rowBgPaint);
                    }
                    
                    String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(new Date(record.getTimestamp()));
                    String amount = String.format(Locale.getDefault(), "¥%.2f", record.getAmount());
                    
                    // 绘制时间
                    canvas.drawText(time, PADDING + 20, y + 20, paint);
                    
                    // 绘制用途
                    canvas.drawText(record.getPurpose(), PADDING + 200, y + 20, paint);
                    
                    // 绘制金额
                    paint.setTextAlign(Paint.Align.RIGHT);
                    paint.setColor(Color.parseColor("#D32F2F"));
                    paint.setTextSize(TEXT_SIZE_LARGE);
                    canvas.drawText(amount, width - PADDING - 20, y + 20, paint);
                    paint.setTextAlign(Paint.Align.LEFT);
                    paint.setColor(Color.parseColor("#424242"));
                    paint.setTextSize(TEXT_SIZE_NORMAL);
                    
                    y += ITEM_HEIGHT;
                    recordIndex++;
                }
                
                // 绘制日期组之间的分割线
                if (y < height - PADDING - 50) {
                    linePaint.setColor(Color.parseColor("#E0E0E0"));
                    linePaint.setStrokeWidth(1);
                    canvas.drawLine(PADDING + 20, y - 10, width - PADDING - 20, y - 10, linePaint);
                }
            }
            
            // 绘制底部信息
            paint.setTextSize(TEXT_SIZE_SMALL);
            paint.setColor(Color.parseColor("#757575"));
            paint.setTextAlign(Paint.Align.CENTER);
            String footerText = "由 Zippy 应用生成";
            canvas.drawText(footerText, width / 2f, height - 30, paint);
            
            // 保存图片
            String fileName = "consumption_payback_" + System.currentTimeMillis() + ".png";
            boolean saveSuccess = false;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore API
                saveSuccess = saveImageWithMediaStore(context, bitmap, fileName);
            } else {
                // Android 9 及以下使用传统文件系统
                saveSuccess = saveImageToFile(context, bitmap, fileName);
            }
            
            if (saveSuccess) {
                android.util.Log.d("ImageGenerator", "图片保存成功");
            } else {
                android.util.Log.e("ImageGenerator", "图片保存失败");
            }
            
            bitmap.recycle();
            android.util.Log.d("ImageGenerator", "图片生成完成");
        } catch (Exception e) {
            android.util.Log.e("ImageGenerator", "生成图片时出错", e);
            e.printStackTrace();
            throw e;
        }
    }
    
    private static boolean saveImageWithMediaStore(Context context, Bitmap bitmap, String fileName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Zippy");
            
            Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            android.util.Log.d("ImageGenerator", "获取到URI: " + imageUri);
            
            if (imageUri != null) {
                try (OutputStream os = context.getContentResolver().openOutputStream(imageUri)) {
                    boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    android.util.Log.d("ImageGenerator", "图片保存" + (success ? "成功" : "失败"));
                    return success;
                } catch (IOException e) {
                    android.util.Log.e("ImageGenerator", "保存图片时出错", e);
                    e.printStackTrace();
                    return false;
                }
            } else {
                android.util.Log.e("ImageGenerator", "无法获取图片URI");
                return false;
            }
        } catch (Exception e) {
            android.util.Log.e("ImageGenerator", "MediaStore保存失败", e);
            return false;
        }
    }
    
    private static boolean saveImageToFile(Context context, Bitmap bitmap, String fileName) {
        try {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File zippyDir = new File(picturesDir, "Zippy");
            if (!zippyDir.exists()) {
                zippyDir.mkdirs();
            }
            
            File imageFile = new File(zippyDir, fileName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            
            android.util.Log.d("ImageGenerator", "传统方式保存图片" + (success ? "成功" : "失败"));
            return success;
        } catch (Exception e) {
            android.util.Log.e("ImageGenerator", "传统方式保存失败", e);
            return false;
        }
    }
    
    private static Map<String, List<ConsumptionRecord>> groupRecordsByDate(List<ConsumptionRecord> records) {
        Map<String, List<ConsumptionRecord>> grouped = new TreeMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        
        for (ConsumptionRecord record : records) {
            String date = dateFormat.format(new Date(record.getTimestamp()));
            grouped.computeIfAbsent(date, k -> new java.util.ArrayList<>()).add(record);
        }
        
        return grouped;
    }
} 