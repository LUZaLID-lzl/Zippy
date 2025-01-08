package com.luza.zippy.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

public class ImageProcess {
    private static final String TAG = "ImageProcess";
    private static int maxWidthNum = 16;
    private static int maxHeightNum = 7;
    
    /**
     * 创建默认的空白图片
     */
    private static Bitmap createDefaultBitmap() {
        return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    }

    /**
     * 创建默认的空白图片数组
     */
    private static Bitmap[] createDefaultBitmapArray(int size) {
        Bitmap[] defaults = new Bitmap[Math.max(1, size)];
        for (int i = 0; i < defaults.length; i++) {
            defaults[i] = createDefaultBitmap();
        }
        return defaults;
    }

    /**
     * 将图片按网格切割，返回指定行的图片数组
     * @param context 上下文
     * @param source 源图片
     * @param rowIndex 需要返回的行索引（从0开始）
     * @param limit 需要返回的图片数量，如果大于实际切割数量则返回全部
     * @return 指定行的Bitmap数组
     */
    public static Bitmap[] splitImageByRow(Context context, Bitmap source, int rowIndex, int limit) {
        if (source == null) {
            Log.e(TAG, "Source bitmap is null");
            return createDefaultBitmapArray(1);
        }

        int width = source.getWidth();
        int height = source.getHeight();
        Log.d(TAG, "Original image size: " + width + "x" + height);

        // 计算每个网格的实际大小
        int gridWidth = width / maxWidthNum;
        int gridHeight = height / maxHeightNum;
        Log.d(TAG, "Grid size: " + gridWidth + "x" + gridHeight);

        // 检查行索引是否有效
        if (rowIndex < 0 || rowIndex >= maxHeightNum) {
            Log.e(TAG, "Invalid row index: " + rowIndex);
            return createDefaultBitmapArray(1);
        }

        // 确定实际要返回的图片数量
        int actualLimit = (limit <= 0 || limit > maxWidthNum) ? maxWidthNum : limit;
        Bitmap[] results = new Bitmap[actualLimit];

        try {
            // 计算当前行的起始Y坐标
            int startY = rowIndex * gridHeight;
            
            // 对当前行进行切割
            for (int col = 0; col < actualLimit; col++) {
                int startX = col * gridWidth;
                
                // 创建新的Bitmap，保持原始大小
                Bitmap bitmap = Bitmap.createBitmap(gridWidth, gridHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                
                // 定义源矩形和目标矩形
                Rect srcRect = new Rect(startX, startY, startX + gridWidth, startY + gridHeight);
                Rect dstRect = new Rect(0, 0, gridWidth, gridHeight);
                
                // 将对应部分绘制到新的Bitmap上
                canvas.drawBitmap(source, srcRect, dstRect, null);
                results[col] = bitmap;
                
                Log.d(TAG, String.format("Created grid at (%d,%d) with size %dx%d", 
                    startX, startY, gridWidth, gridHeight));
            }
            
            // 检查结果数组是否包含空值
            boolean hasEmptyBitmap = false;
            for (Bitmap bitmap : results) {
                if (bitmap == null) {
                    hasEmptyBitmap = true;
                    break;
                }
            }
            
            if (hasEmptyBitmap) {
                Log.e(TAG, "Some bitmaps in result array are null");
                recycleBitmaps(results);
                return createDefaultBitmapArray(1);
            }
            
            return results;
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing row " + rowIndex, e);
            return createDefaultBitmapArray(1);
        }
    }

    /**
     * 将图片资源按网格切割，返回指定行的图片数组
     */
    public static Bitmap[] splitImageByRow(Context context, int resourceId, int rowIndex, int limit) {
        try {
            Log.d(TAG, "Decoding resource ID: " + resourceId);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap source = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            if (source == null) {
                Log.e(TAG, "Failed to decode resource ID: " + resourceId);
                return createDefaultBitmapArray(1);
            }
            Log.d(TAG, "Successfully decoded resource");
            return splitImageByRow(context, source, rowIndex, limit);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding resource", e);
            return createDefaultBitmapArray(1);
        }
    }

    // 为了保持向后兼容，保留原有的方法
    public static Bitmap[] splitImageByRow(Context context, Bitmap source, int rowIndex) {
        return splitImageByRow(context, source, rowIndex, 0);
    }

    public static Bitmap[] splitImageByRow(Context context, int resourceId, int rowIndex) {
        return splitImageByRow(context, resourceId, rowIndex, 0);
    }

    /**
     * 释放Bitmap数组中的所有Bitmap
     */
    public static void recycleBitmaps(Bitmap[] bitmaps) {
        if (bitmaps == null) return;
        
        for (Bitmap bitmap : bitmaps) {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    public static Bitmap[] splitImage(Context context, int resourceId, int parts) {
        try {
            Bitmap source = BitmapFactory.decodeResource(context.getResources(), resourceId);
            return splitImage(source, parts);
        } catch (Exception e) {
            Log.e("ImageProcess", "Error splitting image from resource: " + e.getMessage());
            return new Bitmap[0];
        }
    }

    public static Bitmap[] splitImage(Bitmap source, int parts) {
        if (source == null) {
            Log.e("ImageProcess", "Source bitmap is null");
            return new Bitmap[0];
        }

        // 设置边缘缩进像素
        final int PADDING = 2;
        
        int width = source.getWidth();
        int height = source.getHeight();
        
        Log.d("ImageProcess", "Source image size: " + width + "x" + height);
        
        // 计算实际裁剪区域（去除边缘）
        int effectiveWidth = width - (PADDING * 2);
        int effectiveHeight = height - (PADDING * 2);
        
        // 确保parts至少为1
        parts = Math.max(1, parts);
        
        // 计算每个部分的宽度
        int partWidth = effectiveWidth / parts;
        
        Log.d("ImageProcess", "Splitting into " + parts + " parts, each part width: " + partWidth);
        
        Bitmap[] results = new Bitmap[parts];
        
        try {
            for (int i = 0; i < parts; i++) {
                // 计算裁剪区域（加上边缘缩进）
                int startX = (i * partWidth) + PADDING;
                int endX = Math.min(((i + 1) * partWidth) + PADDING, width - PADDING);
                
                // 创建新的位图
                Bitmap part = Bitmap.createBitmap(
                    source,
                    startX,
                    PADDING,
                    endX - startX,
                    effectiveHeight
                );
                
                results[i] = part;
                
                Log.d("ImageProcess", "Created part " + i + " from x=" + startX + " to x=" + endX);
            }
        } catch (Exception e) {
            Log.e("ImageProcess", "Error creating bitmap part: " + e.getMessage());
            // 如果发生错误，清理已创建的位图
            for (Bitmap bitmap : results) {
                if (bitmap != null && !bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            return new Bitmap[0];
        }
        
        return results;
    }
}
