package com.luza.zippy.ui.sidebarList.pyprender;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;
import android.graphics.Canvas;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class PyprenderFragment extends BaseFragment {
    private MaterialButton importButton;
    private MaterialButton exportButton;
    private CardView previewCard;
    private CardView settingsCard;
    private ImageView imagePreviewOuter;
    private ImageView imagePreviewInner;
    private Bitmap currentBitmap;
    private Bitmap maxBlurredBitmap;  // 存储最大模糊的图片
    private Bitmap blurredBitmap;     // 用于调整模糊度的图片
    private SeekBar outerBlurSeekBar;
    private SeekBar outerBrightnessSeekBar;
    private SeekBar innerMarginSeekBar;
    private SeekBar innerElevationSeekBar;
    private SeekBar innerElevationAlphaSeekBar;
    private SeekBar innerContrastSeekBar;
    private SeekBar innerBrightnessSeekBar;
    private CardView innerCardView;
    private TextView textOuterBlur;
    private TextView textOuterBrightness;
    private TextView textInnerMargin;
    private TextView textInnerElevation;
    private TextView textInnerElevationAlpha;
    private TextView textInnerContrast;
    private TextView textInnerBrightness;
    private SeekBar innerCornerRadiusSeekBar;
    private SeekBar innerSaturationSeekBar;
    private SeekBar innerColorTempSeekBar;
    private SeekBar outerSaturationSeekBar;
    private SeekBar outerContrastSeekBar;
    private TextView textInnerCornerRadius;
    private TextView textInnerSaturation;
    private TextView textInnerColorTemp;
    private TextView textOuterSaturation;
    private TextView textOuterContrast;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleImageResult(result.getData());
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pyprender, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_pyp);
    }

    @Override
    protected void initViews(View view) {
        importButton = view.findViewById(R.id.btn_import);
        exportButton = view.findViewById(R.id.btn_export);
        previewCard = view.findViewById(R.id.card_preview);
        settingsCard = view.findViewById(R.id.card_settings);
        imagePreviewOuter = view.findViewById(R.id.image_preview_outer);
        imagePreviewInner = view.findViewById(R.id.image_preview_inner);
        innerCardView = view.findViewById(R.id.inner_card_view);

        // 初始化滑动条
        outerBlurSeekBar = view.findViewById(R.id.seekbar_outer_blur);
        outerBrightnessSeekBar = view.findViewById(R.id.seekbar_outer_brightness);
        innerMarginSeekBar = view.findViewById(R.id.seekbar_inner_margin);
        innerElevationSeekBar = view.findViewById(R.id.seekbar_inner_elevation);
        innerElevationAlphaSeekBar = view.findViewById(R.id.seekbar_inner_elevation_alpha);
        innerContrastSeekBar = view.findViewById(R.id.seekbar_inner_contrast);
        innerBrightnessSeekBar = view.findViewById(R.id.seekbar_inner_brightness);

        // 初始化数值显示
        textOuterBlur = view.findViewById(R.id.text_outer_blur);
        textOuterBrightness = view.findViewById(R.id.text_outer_brightness);
        textInnerMargin = view.findViewById(R.id.text_inner_margin);
        textInnerElevation = view.findViewById(R.id.text_inner_elevation);
        textInnerElevationAlpha = view.findViewById(R.id.text_inner_elevation_alpha);
        textInnerContrast = view.findViewById(R.id.text_inner_contrast);
        textInnerBrightness = view.findViewById(R.id.text_inner_brightness);

        // 初始化新的滑动条和文本视图
        innerCornerRadiusSeekBar = view.findViewById(R.id.seekbar_inner_corner_radius);
        innerSaturationSeekBar = view.findViewById(R.id.seekbar_inner_saturation);
        innerColorTempSeekBar = view.findViewById(R.id.seekbar_inner_color_temp);
        outerSaturationSeekBar = view.findViewById(R.id.seekbar_outer_saturation);
        outerContrastSeekBar = view.findViewById(R.id.seekbar_outer_contrast);
        
        textInnerCornerRadius = view.findViewById(R.id.text_inner_corner_radius);
        textInnerSaturation = view.findViewById(R.id.text_inner_saturation);
        textInnerColorTemp = view.findViewById(R.id.text_inner_color_temp);
        textOuterSaturation = view.findViewById(R.id.text_outer_saturation);
        textOuterContrast = view.findViewById(R.id.text_outer_contrast);

        // 设置滑动条监听器
        outerBlurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textOuterBlur.setText(String.valueOf(progress));
                if (fromUser && blurredBitmap != null) {
                    updateOuterBlur(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        outerBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textOuterBrightness.setText(String.valueOf(progress));
                if (fromUser && blurredBitmap != null) {
                    updateOuterBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerMarginSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerMargin.setText(String.valueOf(progress));
                if (fromUser) {
                    updateInnerMargin(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerElevationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerElevation.setText(String.valueOf(progress));
                if (fromUser) {
                    updateInnerElevation(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerElevationAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerElevationAlpha.setText(String.valueOf(progress));
                if (fromUser) {
                    updateInnerElevationAlpha(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerContrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerContrast.setText(String.valueOf(progress));
                if (fromUser && currentBitmap != null) {
                    updateInnerContrast(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerBrightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerBrightness.setText(String.valueOf(progress));
                if (fromUser && currentBitmap != null) {
                    updateInnerBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 设置新的滑动条监听器
        innerCornerRadiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerCornerRadius.setText(String.valueOf(progress));
                updateInnerCornerRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerSaturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerSaturation.setText(String.valueOf(progress));
                updateInnerSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        innerColorTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textInnerColorTemp.setText(String.valueOf(progress));
                updateInnerColorTemp(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        outerSaturationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textOuterSaturation.setText(String.valueOf(progress));
                updateOuterSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        outerContrastSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textOuterContrast.setText(String.valueOf(progress));
                updateOuterContrast(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        importButton.setOnClickListener(v -> openImagePicker());
        exportButton.setOnClickListener(v -> exportImage());
        exportButton.setEnabled(false);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageResult(Intent data) {
        Uri selectedImageUri = data.getData();
        if (selectedImageUri != null) {
            try {
                // 从Uri读取图片
                InputStream inputStream = requireContext().getContentResolver().openInputStream(selectedImageUri);
                if (inputStream != null) {
                    // 释放之前的Bitmap
                    if (currentBitmap != null && !currentBitmap.isRecycled()) {
                        currentBitmap.recycle();
                    }
                    if (maxBlurredBitmap != null && !maxBlurredBitmap.isRecycled()) {
                        maxBlurredBitmap.recycle();
                    }
                    if (blurredBitmap != null && !blurredBitmap.isRecycled()) {
                        blurredBitmap.recycle();
                    }
                    
                    // 加载新图片
                    currentBitmap = BitmapFactory.decodeStream(inputStream);
                    
                    // 先创建最大模糊效果的背景图
                    maxBlurredBitmap = createBlurredBitmap(currentBitmap, 25f);
                    
                    // 基于最大模糊图片创建可调整的模糊图片
                    blurredBitmap = maxBlurredBitmap.copy(maxBlurredBitmap.getConfig(), true);
                    
                    // 调整CardView大小并设置图片
                    adjustCardView(previewCard, imagePreviewOuter, imagePreviewInner, currentBitmap, blurredBitmap);
                    
                    // 设置滑动条初始值
                    outerBlurSeekBar.setProgress(15); // 设置为默认值
                    textOuterBlur.setText("15");
                    
                    // 显示预览和设置面板
                    previewCard.setVisibility(View.VISIBLE);
                    settingsCard.setVisibility(View.VISIBLE);
                    exportButton.setEnabled(true);
                    inputStream.close();
                }
            } catch (IOException e) {
                Toast.makeText(requireContext(), R.string.pyp_import_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void adjustCardView(CardView cardView, ImageView outerImageView, ImageView innerImageView, 
                              Bitmap originalBitmap, Bitmap blurredBitmap) {
        if (originalBitmap == null) return;

        // 获取图片的宽高比
        float aspectRatio = (float) originalBitmap.getWidth() / originalBitmap.getHeight();

        // 获取屏幕尺寸
        int screenWidth = requireContext().getResources().getDisplayMetrics().widthPixels;
        int screenHeight = requireContext().getResources().getDisplayMetrics().heightPixels;
        
        // 设置卡片边距
        int cardMargin = (int) (24 * requireContext().getResources().getDisplayMetrics().density); // 12dp * 2
        
        // 计算实际可用宽度
        int availableWidth = screenWidth - cardMargin;
        
        // 计算高度
        int height = (int) (availableWidth / aspectRatio);
        
        // 如果是横向图片(宽高比>1),限制最大高度为屏幕高度的60%
        if (aspectRatio > 1) {
            int maxHeight = (int) (screenHeight * 0.6f);
            height = Math.min(height, maxHeight);
        }

        // 设置 CardView 的宽高
        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.width = availableWidth;
        params.height = height;
        cardView.setLayoutParams(params);

        // 设置外部和内部图片
        outerImageView.setImageBitmap(blurredBitmap);
        innerImageView.setImageBitmap(originalBitmap);
    }

    private Bitmap createBlurredBitmap(Bitmap originalBitmap) {
        return createBlurredBitmap(originalBitmap, 15f); // 使用默认值15
    }

    private Bitmap createBlurredBitmap(Bitmap originalBitmap, float radius) {
        // 确保模糊半径在有效范围内 (1-25)
        float validRadius = Math.max(1f, Math.min(25f, radius));
        
        // 创建一个新的Bitmap用于模糊处理
        Bitmap blurredBitmap = originalBitmap.copy(originalBitmap.getConfig(), true);
        
        // 使用RenderScript进行模糊处理
        RenderScript renderScript = RenderScript.create(requireContext());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        
        Allocation input = Allocation.createFromBitmap(renderScript, blurredBitmap);
        Allocation output = Allocation.createFromBitmap(renderScript, blurredBitmap);
        
        // 设置模糊半径 (范围: 1-25)
        blurScript.setRadius(validRadius);
        
        // 执行模糊处理
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(blurredBitmap);
        
        // 清理资源
        renderScript.destroy();
        input.destroy();
        output.destroy();
        blurScript.destroy();
        
        return blurredBitmap;
    }

    private void exportImage() {
        if (previewCard == null || previewCard.getVisibility() != View.VISIBLE) {
            Toast.makeText(requireContext(), R.string.pyp_no_image, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 创建一个与CardView大小相同的Bitmap
            Bitmap exportBitmap = Bitmap.createBitmap(
                previewCard.getWidth(),
                previewCard.getHeight(),
                Bitmap.Config.ARGB_8888
            );

            // 创建Canvas
            Canvas canvas = new Canvas(exportBitmap);
            
            // 绘制外部图片（带模糊效果）
            Paint paint = new Paint();
            paint.setFilterBitmap(true);
            if (blurredBitmap != null) {
                // 创建外部图片的目标区域（全屏）
                android.graphics.RectF outerRect = new android.graphics.RectF(0, 0, canvas.getWidth(), canvas.getHeight());
                
                // 应用亮度滤镜
                if (imagePreviewOuter.getColorFilter() != null) {
                    paint.setColorFilter(imagePreviewOuter.getColorFilter());
                }
                
                // 绘制外部图片，保持宽高比并填充整个区域
                canvas.drawBitmap(blurredBitmap, null, outerRect, paint);
            }

            // 获取内部CardView的边距
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) innerCardView.getLayoutParams();
            int margin = params.leftMargin;  // 假设四边边距相等

            // 计算内部图片的绘制区域
            float left = margin;
            float top = margin;
            float right = canvas.getWidth() - margin;
            float bottom = canvas.getHeight() - margin;
            
            // 创建圆角矩形路径
            android.graphics.Path path = new android.graphics.Path();
            float cornerRadius = innerCardView.getRadius();
            android.graphics.RectF rect = new android.graphics.RectF(left, top, right, bottom);
            path.addRoundRect(rect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW);

            // 保存Canvas状态
            canvas.save();
            // 应用裁剪路径
            canvas.clipPath(path);
            
            // 绘制内部图片
            if (currentBitmap != null) {
                // 创建内部图片的目标区域
                android.graphics.RectF targetRect = new android.graphics.RectF(left, top, right, bottom);
                
                // 应用亮度和对比度滤镜
                Paint innerPaint = new Paint();
                innerPaint.setFilterBitmap(true);
                if (imagePreviewInner.getColorFilter() != null) {
                    innerPaint.setColorFilter(imagePreviewInner.getColorFilter());
                }
                
                // 绘制内部图片
                canvas.drawBitmap(currentBitmap, null, targetRect, innerPaint);
            }
            
            // 恢复Canvas状态
            canvas.restore();

            // 保存图片到相册
            String fileName = "PyPRender_" + System.currentTimeMillis() + ".png";
            String mimeType = "image/png";
            
            // 使用MediaStore保存图片
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PyPRender");

            Uri imageUri = requireContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                try (OutputStream os = requireContext().getContentResolver().openOutputStream(imageUri)) {
                    if (os != null) {
                        exportBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                        Toast.makeText(requireContext(), R.string.pyp_export_success, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            // 回收Bitmap
            exportBitmap.recycle();

        } catch (Exception e) {
            Toast.makeText(requireContext(), getString(R.string.pyp_export_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOuterBlur(int radius) {
        if (maxBlurredBitmap != null) {
            // 确保模糊半径在有效范围内 (1-25)
            float validRadius = Math.max(1f, Math.min(25f, radius));
            
            // 重新创建模糊效果，基于最大模糊的图片
            if (blurredBitmap != null) {
                blurredBitmap.recycle();
            }
            blurredBitmap = createBlurredBitmap(maxBlurredBitmap, validRadius);
            imagePreviewOuter.setImageBitmap(blurredBitmap);
        }
    }

    private void updateOuterBrightness(int brightness) {
        if (blurredBitmap != null) {
            float scale = brightness / 100f;
            imagePreviewOuter.setColorFilter(adjustBrightness(scale));
        }
    }

    private void updateInnerContrast(int contrast) {
        if (currentBitmap != null) {
            float scale = contrast / 100f;
            imagePreviewInner.setColorFilter(adjustContrast(scale));
        }
    }

    private void updateInnerBrightness(int brightness) {
        if (currentBitmap != null) {
            float scale = brightness / 100f;
            imagePreviewInner.setColorFilter(adjustBrightness(scale));
        }
    }

    private ColorMatrixColorFilter adjustBrightness(float scale) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setScale(scale, scale, scale, 1);
        return new ColorMatrixColorFilter(matrix);
    }

    private ColorMatrixColorFilter adjustContrast(float contrast) {
        float scale = contrast;
        float translate = (-.5f * scale + .5f) * 255f;
        ColorMatrix cm = new ColorMatrix(new float[] {
            scale, 0, 0, 0, translate,
            0, scale, 0, 0, translate,
            0, 0, scale, 0, translate,
            0, 0, 0, 1, 0
        });
        return new ColorMatrixColorFilter(cm);
    }

    private void updateInnerMargin(int marginDp) {
        if (innerCardView != null) {
            int marginPx = (int) (marginDp * requireContext().getResources().getDisplayMetrics().density);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) innerCardView.getLayoutParams();
            params.setMargins(marginPx, marginPx, marginPx, marginPx);
            innerCardView.setLayoutParams(params);
        }
    }

    private void updateInnerCornerRadius(int radius) {
        if (innerCardView != null) {
            innerCardView.setRadius(radius);
        }
    }

    private void updateInnerSaturation(int saturation) {
        if (imagePreviewInner != null) {
            float scale = saturation / 100f;
            ColorMatrix saturationMatrix = new ColorMatrix();
            saturationMatrix.setSaturation(scale);
            imagePreviewInner.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        }
    }

    private void updateInnerColorTemp(int colorTemp) {
        if (imagePreviewInner != null) {
            float scale = (colorTemp - 100) / 100f; // 将100作为中性点
            ColorMatrix colorMatrix = new ColorMatrix();
            // 调整红色和蓝色通道来改变色温
            colorMatrix.set(new float[] {
                1, 0, 0, 0, scale * 50,  // 红色通道
                0, 1, 0, 0, 0,           // 绿色通道
                0, 0, 1, 0, -scale * 50, // 蓝色通道
                0, 0, 0, 1, 0
            });
            imagePreviewInner.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        }
    }

    private void updateOuterSaturation(int saturation) {
        if (imagePreviewOuter != null) {
            float scale = saturation / 100f;
            ColorMatrix saturationMatrix = new ColorMatrix();
            saturationMatrix.setSaturation(scale);
            imagePreviewOuter.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        }
    }

    private void updateOuterContrast(int contrast) {
        if (imagePreviewOuter != null) {
            imagePreviewOuter.setColorFilter(adjustContrast(contrast / 100f));
        }
    }

    private void updateInnerElevation(int elevationDp) {
        if (innerCardView != null) {
            float elevationPx = elevationDp * requireContext().getResources().getDisplayMetrics().density;
            innerCardView.setCardElevation(elevationPx);
        }
    }

    private void updateInnerElevationAlpha(int alpha) {
        if (innerCardView != null) {
            float alphaValue = alpha / 100f;
            // 设置阴影颜色的透明度
            int shadowColor = android.graphics.Color.argb(
                (int) (alphaValue * 255), // alpha通道
                0, // red
                0, // green
                0  // blue
            );
            innerCardView.setCardBackgroundColor(android.graphics.Color.WHITE); // 保持卡片背景为白色
            innerCardView.setCardElevation(innerCardView.getCardElevation()); // 触发阴影重绘
            innerCardView.setOutlineAmbientShadowColor(shadowColor); // 设置环境阴影颜色
            innerCardView.setOutlineSpotShadowColor(shadowColor); // 设置点光源阴影颜色
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理Bitmap
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
        if (maxBlurredBitmap != null && !maxBlurredBitmap.isRecycled()) {
            maxBlurredBitmap.recycle();
            maxBlurredBitmap = null;
        }
        if (blurredBitmap != null && !blurredBitmap.isRecycled()) {
            blurredBitmap.recycle();
            blurredBitmap = null;
        }
    }
}