package com.luza.zippy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.sidebarList.scrummage.ScrummageFragment;
import com.luza.zippy.ui.sidebarList.settings.Util;
import com.luza.zippy.ui.sidebarList.turntable.TurntableDbHelper;
import com.luza.zippy.ui.utils.ImageProcess;
import com.luza.zippy.ui.views.LiquidBackgroundView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Executors;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 0; // 1.5秒的延迟
    private ShardPerfenceSetting shardPerfenceSetting;
    private ImageView imageView;
    private ProgressBar loadingProgress;
    private TextView progressText;
    private static final int TOTAL_IMAGES = 65; // 总图片数（7+5+5+5+9+16+8+10）
    private volatile int convertedImages = 0;
    private Util util;
    private TurntableDbHelper dbHelper;

    public static Bitmap[] pikachuImages;
    public static Bitmap[] bulbasaurImages;
    public static Bitmap[] squirtleImages;
    public static Bitmap[] mewImages;
    public static Bitmap[] karsaImages;
    public static Bitmap[] capooImages;
    public static Bitmap[] mapleImages;
    public static Bitmap[] winterImages;
    public static Bitmap[] gengarImages;

    public static final boolean isDebug = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getApplicationContext());
        shardPerfenceSetting.addLaunchNum();

        // 检查是否开启了随机主题
        if (shardPerfenceSetting.getRandomTheme()) {
            setRandomTheme();
        }

        dbHelper = new TurntableDbHelper(this);
        dbHelper.insertDefaultData();

        util = new Util();
        util.updateTheme(this);
        util.updateLocale(this);

        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.bg_screen_splash);
        loadingProgress = findViewById(R.id.loading_progress);
        progressText = findViewById(R.id.text_progress);


        if (isDebug) {
            startMainActivity();
        } else {
            // 等待imageView显示完成后再执行后续操作
            imageView.post(() -> {
                // 显示加载动画
                loadingProgress.setVisibility(View.VISIBLE);
                progressText.setVisibility(View.VISIBLE);

                // 在新线程中执行图片转换
                Thread convertThread = new Thread(() -> {
                    try {
                        convertPicture();
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            loadingProgress.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);
                            startMainActivity();
                        });
                    }
                });
                convertThread.setPriority(Thread.MAX_PRIORITY); // 设置高优先级
                convertThread.start();

                new Thread(() -> {
                    try {
                        captureScreenColors();
                        // 等待转换线程结束
                        convertThread.join();
                        // 隐藏加载动画
                        runOnUiThread(() -> {
                            loadingProgress.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);
                        });
                        // 延迟启动MainActivity
                        runOnUiThread(() -> new Handler().postDelayed(this::startMainActivity, SPLASH_DELAY));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        // 如果线程被中断，隐藏加载动画并直接启动MainActivity
                        runOnUiThread(() -> {
                            loadingProgress.setVisibility(View.GONE);
                            progressText.setVisibility(View.GONE);
                            startMainActivity();
                        });
                    }
                }).start();
            });
        }
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

    private void updateProgress(final int progress) {
        runOnUiThread(() -> progressText.setText(String.valueOf(progress)));
    }

    /**
     * 捕获屏幕颜色
     */
    private void captureScreenColors() {

        //ColorCalibration.calculateGradientColors("#D76000");
        // 确保窗口已经准备好
        View decorView = getWindow().getDecorView();
        decorView.post(() -> {
            if (!isFinishing() && decorView.isAttachedToWindow()) {
                try {
                    // 获取状态栏高度
                    int statusBarHeight = 0;
                    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                    }

                    // 获取导航栏高度
                    int navigationBarHeight = 0;
                    resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        navigationBarHeight = getResources().getDimensionPixelSize(resourceId);
                    }

                    // 创建位图
                    Bitmap bitmap = Bitmap.createBitmap(
                            decorView.getWidth(),
                            decorView.getHeight(),
                            Bitmap.Config.ARGB_8888
                    );

                    // 计算采样位置（避开状态栏和导航栏）
                    int[] samplePoints = {
                            statusBarHeight + 10,  // 顶部位置
                            decorView.getHeight() - navigationBarHeight - 10  // 底部位置
                    };

                    // 使用 PixelCopy API 捕获屏幕
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        PixelCopy.request(
                                getWindow(),
                                bitmap,
                                (copyResult) -> {
                                    if (copyResult == PixelCopy.SUCCESS) {
                                        // 分析颜色
                                        int topColor = bitmap.getPixel(bitmap.getWidth() / 2, samplePoints[0]);
                                        int bottomColor = bitmap.getPixel(bitmap.getWidth() / 2, samplePoints[1]);

                                        // 更新背景颜色
                                        LiquidBackgroundView liquidBackground = findViewById(R.id.liquid_background);
                                        if (liquidBackground != null) {
                                            liquidBackground.setColors(
                                                    new int[]{topColor},
                                                    new int[]{bottomColor}
                                            );
                                        }

                                        Log.d("SplashActivity", String.format(
                                                "Captured colors - Top: # %06X , Bottom: # %06X",
                                                (0xFFFFFF & topColor),
                                                (0xFFFFFF & bottomColor)
                                        ));
                                    }
                                    // 回收位图
                                    bitmap.recycle();
                                },
                                new Handler(Looper.getMainLooper())
                        );
                    }
                } catch (Exception e) {
                    Log.e("SplashActivity", "Error capturing screen colors", e);
                }
            }
        });
    }

    public void convertPicture() {
        // 使用BitmapFactory.Options优化内存使用
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565; // 使用16位图片格式
        options.inMutable = true; // 允许修改Bitmap
        options.inPurgeable = true; // 允许系统回收内存
        options.inInputShareable = true; // 允许输入数据共享

        // 创建线程池进行并行处理
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        // 并行处理每组图片
        futures.add(executor.submit(() -> {
            squirtleImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 0, 7);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            pikachuImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 1, 5);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            bulbasaurImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 2, 5);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            mewImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 3, 5);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            karsaImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 4, 9);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            capooImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 5, 16);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            mapleImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 6, 8);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            winterImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 7, 10);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        futures.add(executor.submit(() -> {
            gengarImages = ImageProcess.splitImageByRow(this, R.drawable.home_display, 8, 12);
            updateProgress((int) (++convertedImages * 100.0f / TOTAL_IMAGES));
        }));

        // 等待所有任务完成
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 关闭线程池
        executor.shutdown();
    }

    /**
     * 设置随机主题
     */
    private void setRandomTheme() {
        // 所有可用的主题列表
        String[] themes = {
            "pikachu", "squirtle", "winter", "maple", "gengar", 
            "capoo", "bulbasaur", "mew", "karsa"
        };
        
        // 随机选择一个主题
        int randomIndex = (int) (Math.random() * themes.length);
        String randomTheme = themes[randomIndex];
        
        // 只有在主题不同时才更新
        if (!randomTheme.equals(shardPerfenceSetting.getHomeTheme())) {
            shardPerfenceSetting.setHomeTheme(randomTheme);
            android.util.Log.d("SplashActivity", "随机选择主题: " + randomTheme);
        }
    }
} 