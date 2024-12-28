package com.luza.zippy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.fragments.HomeFragment;
import com.luza.zippy.ui.sidebarList.calendar.CalendarFragment;
import com.luza.zippy.ui.sidebarList.deviceInformation.DeviceInfoFragment;
import com.luza.zippy.ui.sidebarList.battery.BatteryFragment;
import com.luza.zippy.ui.sidebarList.performance.PerformanceFragment;
import com.luza.zippy.ui.sidebarList.settings.Util;
import com.luza.zippy.ui.sidebarList.test.TestFragment;
import com.luza.zippy.ui.sidebarList.timer.TimerFragment;
import com.luza.zippy.ui.sidebarList.todo.TodoFragment;
import com.luza.zippy.ui.sidebarList.calorie.CalorieFragment;

import java.util.Locale;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import com.luza.zippy.ui.views.LiquidBackgroundView;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.view.Window;
import android.view.WindowManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Build;

/**
 * MainActivity作为应用的主入口活动
 * 负责初始化主要UI组件和侧边栏导航
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    private static final String TAG = "Zippy-MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean isHome = true; // 添加标记，用于判断是否在首页
    private GestureDetectorCompat gestureDetector;
    public static Context mContext;
    private ShardPerfenceSetting shardPerfenceSetting;

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 super.onCreate 之前设置主题
        util = new Util();
        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getApplicationContext());
        
        // 根据保存的主题设置当前主题
        String currentTheme = shardPerfenceSetting.getHomeTheme();
        switch (currentTheme){
            case "pikachu":
                setTheme(R.style.PikachuTheme);
                break;
            case "bulbasaur":
                setTheme(R.style.BulbasaurTheme);
                break;
            case "squirtle":
                setTheme(R.style.SquirtleTheme);
                break;
            case "mew":
                setTheme(R.style.MewTheme);
                break;
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置背景颜色
        LiquidBackgroundView liquidBackground = findViewById(R.id.liquidBackground);
        int[] startColors;
        int[] endColors;
        
        switch (currentTheme) {
            case "pikachu":
                startColors = new int[]{
                    Color.parseColor("#FFC603"),
                    Color.parseColor("#FFE085"),
                    Color.parseColor("#FFD44C"),
                    Color.parseColor("#FFB300")
                };
                endColors = new int[]{
                    Color.parseColor("#FFE085"),
                    Color.parseColor("#FFD44C"),
                    Color.parseColor("#FFC603"),
                    Color.parseColor("#FFD44C")
                };
                break;
            case "bulbasaur":
                startColors = new int[]{
                    Color.parseColor("#13B4FC"),
                    Color.parseColor("#7ED8FA"),
                    Color.parseColor("#45C7FC"),
                    Color.parseColor("#0099E5")
                };
                endColors = new int[]{
                    Color.parseColor("#7ED8FA"),
                    Color.parseColor("#45C7FC"),
                    Color.parseColor("#13B4FC"),
                    Color.parseColor("#45C7FC")
                };
                break;
            case "squirtle":
                startColors = new int[]{
                    Color.parseColor("#5CB860"),
                    Color.parseColor("#96D897"),
                    Color.parseColor("#74C677"),
                    Color.parseColor("#45A948")
                };
                endColors = new int[]{
                    Color.parseColor("#96D897"),
                    Color.parseColor("#74C677"),
                    Color.parseColor("#5CB860"),
                    Color.parseColor("#74C677")
                };
                break;
            case "mew":
                startColors = new int[]{
                    Color.parseColor("#FBA7BD"),
                    Color.parseColor("#FDD3DE"),
                    Color.parseColor("#FCC0CE"),
                    Color.parseColor("#F98DA8")
                };
                endColors = new int[]{
                    Color.parseColor("#FDD3DE"),
                    Color.parseColor("#FCC0CE"),
                    Color.parseColor("#FBA7BD"),
                    Color.parseColor("#FCC0CE")
                };
                break;
            default:
                startColors = new int[]{
                    Color.parseColor("#1A1A1A"),
                    Color.parseColor("#2D2D2D"),
                    Color.parseColor("#404040"),
                    Color.parseColor("#333333")
                };
                endColors = new int[]{
                    Color.parseColor("#2D2D2D"),
                    Color.parseColor("#404040"),
                    Color.parseColor("#1A1A1A"),
                    Color.parseColor("#404040")
                };
        }
        liquidBackground.setColors(startColors, endColors);

        initViews();
        setupNavigationDrawer();
        setupGestureDetector();


        // 3秒后捕获屏幕颜色
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            captureScreenColors();
        }, 3000);
        if (savedInstanceState == null) {
            loadHomeFragment();
        }
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mContext = this;
        
        drawerLayout.setOnTouchListener(this);
    }

    /**
     * 设置侧边栏导航
     */
    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * 加载首页Fragment
     */
    private void loadHomeFragment() {
        // 清空回退栈
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, new HomeFragment());
        transaction.commit();
        isHome = true;
    }

    /**
     * 加载其他Fragment
     */
    private void loadFragment(Fragment fragment) {
        // 如果当前不是首页，先清空回退栈
        if (!isHome) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
            R.anim.slide_in_right,  // 进入动画
            R.anim.slide_out_left,  // 退出动画
            R.anim.slide_in_left,   // 返回时���入动画
            R.anim.slide_out_right  // 返回时退出动画
        );
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        
        // 更新首页标记
        isHome = fragment instanceof HomeFragment;
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_subscriptions) {
            loadFragment(new DeviceInfoFragment());
        } else if (id == R.id.nav_battery) {
            loadFragment(new BatteryFragment());
        } else if (id == R.id.nav_todo) {
            loadFragment(new TodoFragment());
        } else if (id == R.id.nav_calorie) {
            loadFragment(new CalorieFragment());
        } else if (id == R.id.nav_calendar) {
            loadFragment(new CalendarFragment());
        } else if (id == R.id.nav_performance_test) {
            loadFragment(new PerformanceFragment());
        } else if (id == R.id.nav_timer) {
            loadFragment(new TimerFragment());
        }

//        else if (id == R.id.nav_test){
//            loadFragment(new TestFragment());
//        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 1) {
                // 如果回退栈中有多个Fragment，弹出顶部的Fragment
                fm.popBackStack();
            } else if (fm.getBackStackEntryCount() == 1) {
                // 如果只剩一个Fragment，清空回退栈并加载首页
                loadHomeFragment();
            } else {
                super.onBackPressed();
            }
        }
    }

    // 添加返回栈监听器
    private final FragmentManager.OnBackStackChangedListener backStackChangedListener = 
        new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                // 当返回栈为空时，说明回到了首页
                isHome = getSupportFragmentManager().getBackStackEntryCount() == 0;
            }
        };

    @Override
    protected void onResume() {
        super.onResume();
        getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);
        util.updateLocale(this);
    }

    private void setupGestureDetector() {
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY) &&
                    Math.abs(diffX) > SWIPE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    
                    if (diffX > 0) {  // 从左向右滑动
                        drawerLayout.openDrawer(GravityCompat.START);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event != null) {
            gestureDetector.onTouchEvent(event);
        }
        return false;
    }

    /**
     * 获取颜色的ARGB信息
     */
    private String getColorInfo(int color) {
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format("#%02X%02X%02X (A:%d, R:%d, G:%d, B:%d)",
                red, green, blue, alpha, red, green, blue);
    }

    /**
     * 将颜色整数值转换为6位十六进制颜色代码
     */
    private String colorToHex(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    /**
     * 捕获屏幕颜色
     */
    private void captureScreenColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            final Bitmap bitmap = Bitmap.createBitmap(
                    window.getDecorView().getWidth(),
                    window.getDecorView().getHeight(),
                    Bitmap.Config.ARGB_8888);

            // 使用PixelCopy捕获屏幕内容
            PixelCopy.request(window, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();

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

                    // 向内偏移50px，并避开状态栏和导航栏
                    int topY = statusBarHeight + 1;
                    int bottomY = height - navigationBarHeight - 1;

                    // 获取颜色
                    int topColor = bitmap.getPixel(width/2, topY);
                    int bottomColor = bitmap.getPixel(width/2, bottomY);

                    // 打印颜色信息
                    Log.d(TAG, "Screen colors (excluding system bars):");
                    Log.d(TAG, String.format("Sample positions - Top Y: %d, Bottom Y: %d", topY, bottomY));
                    Log.d(TAG, "Top color: " + colorToHex(topColor) + " " + getColorInfo(topColor));
                    Log.d(TAG, "Bottom color: " + colorToHex(bottomColor) + " " + getColorInfo(bottomColor));
                } else {
                    Log.e(TAG, "Failed to capture screen content");
                }
                bitmap.recycle();
            }, new Handler(Looper.getMainLooper()));
        }
    }
}