package com.luza.zippy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.sidebarList.settings.Util;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // 1.5秒的延迟
    private ShardPerfenceSetting shardPerfenceSetting;
    private ImageView imageView;
    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        // 设置全屏
//        requestWindowFeature(Window.FEATURE_NO_TITLE);  // 去除标题栏
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);  // 设置全屏

        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getApplicationContext());
        util = new Util();
        util.updateTheme(this);
        util.updateLocale(this);

        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.bg_screen_splash);

        // 使用Handler延迟跳转
        new Handler().postDelayed(this::startMainActivity, SPLASH_DELAY);
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
} 