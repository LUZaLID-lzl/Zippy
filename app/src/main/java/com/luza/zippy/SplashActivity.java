package com.luza.zippy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.sidebarList.settings.Util;
import com.luza.zippy.ui.sidebarList.turntable.TurntableDbHelper;
import com.luza.zippy.ui.utils.ImageProcess;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 0; // 1.5秒的延迟
    private ShardPerfenceSetting shardPerfenceSetting;
    private ImageView imageView;
    private ProgressBar loadingProgress;
    private Util util;
    private TurntableDbHelper dbHelper;

    public static Bitmap[] pikachuImages;
    public static Bitmap[] bulbasaurImages;
    public static Bitmap[] squirtleImages;
    public static Bitmap[] mewImages;
    public static Bitmap[] karsaImages;
    public static Bitmap[] capooImages;
    public static Bitmap[] mapleImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getApplicationContext());
        shardPerfenceSetting.addLaunchNum();

        dbHelper = new TurntableDbHelper(this);
        dbHelper.insertDefaultData();

        util = new Util();
        util.updateTheme(this);
        util.updateLocale(this);

        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.bg_screen_splash);
        loadingProgress = findViewById(R.id.loading_progress);

        // 等待imageView显示完成后再执行后续操作
        imageView.post(() -> {
            // 显示加载动画
            loadingProgress.setVisibility(View.VISIBLE);
            
            // 在新线程中执行图片转换
            Thread convertThread = new Thread(){
                @Override
                public void run(){
                    super.run();
                    convertPicture();
                }
            };
            convertThread.start();
            
            new Thread(() -> {
                try {
                    // 等待转换线程结束
                    convertThread.join();
                    // 隐藏加载动画
                    runOnUiThread(() -> loadingProgress.setVisibility(View.GONE));
                    // 延迟启动MainActivity
                    runOnUiThread(() -> new Handler().postDelayed(this::startMainActivity, SPLASH_DELAY));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 如果线程被中断，隐藏加载动画并直接启动MainActivity
                    runOnUiThread(() -> {
                        loadingProgress.setVisibility(View.GONE);
                        startMainActivity();
                    });
                }
            }).start();
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // 应用过渡动画
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // 禁用返回键
        super.onBackPressed();
    }

    public void convertPicture(){
        squirtleImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,0,7);
        pikachuImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,1,5);
        bulbasaurImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,2,5);
        mewImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,3,5);
        karsaImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,4,9);
        capooImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,5,16);
        mapleImages = ImageProcess.splitImageByRow(this,R.drawable.home_display,6,8);
    }
} 