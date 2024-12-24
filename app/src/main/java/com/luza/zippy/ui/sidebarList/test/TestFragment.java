package com.luza.zippy.ui.sidebarList.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TestFragment extends BaseFragment {
    private static final String TAG = "TestFragment";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private ImageView testImage;
    private Button saveButton;
    private Bitmap currentBitmap;

    @Override
    protected String getTitle() {
        return getString(R.string.menu_test);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_test, container, false);
    }

    @Override
    protected void initViews(View view) {
        testImage = view.findViewById(R.id.test_image);
        saveButton = view.findViewById(R.id.btn_save_image);

        // 从指定路径加载图片
        String imagePath = "/storage/sdcard0/wallhaven-z827xy.jpg";
        loadImageFromPath(imagePath);

        saveButton.setOnClickListener(v -> checkPermissionAndSave());
    }

    private void checkPermissionAndSave() {
        saveImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        saveImage();
    }

    private void saveImage() {
        Log.e(TAG, "saveImage ");
        new SaveImageTask().execute();
    }

    private class SaveImageTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... ignored) {
            if (currentBitmap == null) {
                Log.e(TAG, "currentBitmap == null");
                return false;
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "IMG_" + timeStamp;

            File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "Zippy");
            
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory");
                return false;
            }

            File imageFile = new File(storageDir, fileName + ".png");
            boolean success = false;

            try {
                FileOutputStream stream = new FileOutputStream(imageFile);
                try {
                    success = currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                } finally {
                    stream.flush();
                    stream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving image", e);
            }

            return success;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(requireContext(), "图片保存成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "图片保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadImageFromPath(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            Log.e(TAG, "imageFile：" + imageFile);
            
            if (!imageFile.exists()) {
                Log.e(TAG, "Unable to read file: " + imageFile.getAbsolutePath());
                Toast.makeText(requireContext(), "无法读取图片文件", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建临时文件来保存处理后的图片
            File cacheDir = requireContext().getCacheDir();
            File tempFile = new File(cacheDir, "temp_image.png");
            
            FileOutputStream outputStream = null;
            try {
                // 先将原图片读取为Bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);

                // 计算压缩比例
                int scaleFactor = calculateInSampleSize(options, 1080, 1080);
                options.inJustDecodeBounds = false;
                options.inSampleSize = scaleFactor;
                
                currentBitmap = BitmapFactory.decodeFile(imagePath, options);
                Log.e(TAG, "currentBitmap：" + currentBitmap);
                
                if (currentBitmap != null) {
                    // 将处理后的图片保存到临时文件
                    outputStream = new FileOutputStream(tempFile);
                    boolean success = currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    
                    if (success) {
                        // 显示图片
                        testImage.setImageBitmap(currentBitmap);
                    } else {
                        Log.e(TAG, "Failed to compress bitmap");
                        Toast.makeText(requireContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to decode bitmap");
                    Toast.makeText(requireContext(), "图片解码失败", Toast.LENGTH_SHORT).show();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error processing image: " + e.getMessage());
                Toast.makeText(requireContext(), "图片处理出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.flush();
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Error closing stream: " + e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
            Toast.makeText(requireContext(), "图片加载出错: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
    }
}