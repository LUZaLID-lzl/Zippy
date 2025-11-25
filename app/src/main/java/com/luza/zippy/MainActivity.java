package com.luza.zippy;

import android.content.Context;

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
import com.luza.zippy.ui.sidebarList.compass.CompassFragment;
import com.luza.zippy.ui.sidebarList.consumption.ConsumptionFragment;
import com.luza.zippy.ui.sidebarList.deviceInformation.DeviceInfoFragment;
import com.luza.zippy.ui.sidebarList.battery.BatteryFragment;
import com.luza.zippy.ui.sidebarList.foodRecord.FoodRecordFragment;
import com.luza.zippy.ui.sidebarList.minecraft.MinecraftFragment;
import com.luza.zippy.ui.sidebarList.minecraft.location.MinecraftLocationFragment;
import com.luza.zippy.ui.sidebarList.performance.PerformanceFragment;
import com.luza.zippy.ui.sidebarList.pyprender.PyprenderFragment;
import com.luza.zippy.ui.sidebarList.scrummage.ScrummageFragment;
import com.luza.zippy.ui.sidebarList.settings.Util;
import com.luza.zippy.ui.sidebarList.sort.SortFragment;
import com.luza.zippy.ui.sidebarList.test.TestFragment;
import com.luza.zippy.ui.sidebarList.tetris.TetrisFragment;
import com.luza.zippy.ui.sidebarList.timer.TimerFragment;
import com.luza.zippy.ui.sidebarList.todo.TodoFragment;
import com.luza.zippy.ui.sidebarList.calorie.CalorieFragment;

import android.graphics.Color;

import com.luza.zippy.ui.sidebarList.turntable.TurntableFragment;
import com.luza.zippy.ui.sidebarList.tzfe.TzfeFragment;
import com.luza.zippy.ui.sidebarList.wifi.WifiFragment;
import com.luza.zippy.ui.utils.ColorCalibration;
import com.luza.zippy.ui.views.LiquidBackgroundView;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.view.WindowManager;
import android.view.Menu;

/**
 * MainActivity作为应用的主入口活动
 * 负责初始化主要UI组件和侧边栏导航
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    private static final String TAG = "Zippy-MainActivity";
    private DrawerLayout drawerLayout;
    public NavigationView navigationView;
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
            case "karsa":
                setTheme(R.style.KarsaTheme);
                break;
            case "capoo":
                setTheme(R.style.CapooTheme);
                break;
            case "maple":
                setTheme(R.style.MapleTheme);
                break;
            case "winter":
                setTheme(R.style.WinterTheme);
                break;
            case "gengar":
                setTheme(R.style.GengarTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        ColorCalibration.calculateGradientColors();

        if (SplashActivity.isDebug){
            loadFragment(new TurntableFragment());
        }

        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 初始化视图组件
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mContext = this;
        
        drawerLayout.setOnTouchListener(this);

        // 根据工作模式过滤菜单项
        filterMenuItemsByWorkMode();
    }

    /**
     * 根据当前工作模式过滤菜单项
     */
    public void filterMenuItemsByWorkMode() {
        String currentWorkMode = shardPerfenceSetting.getWorkMode();
        Menu navMenu = navigationView.getMenu();
        
        // 遍历所有菜单项
        for (int i = 0; i < navMenu.size(); i++) {
            MenuItem menuItem = navMenu.getItem(i);
            if (menuItem.hasSubMenu()) {
                // 如果有子菜单，递归处理
                Menu subMenu = menuItem.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    filterMenuItem(subMenu.getItem(j), currentWorkMode);
                }
            } else {
                // 处理单个菜单项
                filterMenuItem(menuItem, currentWorkMode);
            }
        }

        // 检查激活状态并控制计时器菜单项的显示
        MenuItem timerItem = navMenu.findItem(R.id.nav_timer);
        Boolean activationCode = shardPerfenceSetting.getActivate();
        timerItem.setVisible(activationCode);
    }
    
    /**
     * 根据工作模式过滤单个菜单项
     */
    private void filterMenuItem(MenuItem menuItem, String workMode) {
        // 使用菜单项ID来确定其类型
        String itemTag = getMenuItemTag(menuItem.getItemId());
        
        android.util.Log.d("liziluo","menuItem: " + menuItem.getTitle() + ", itemTag: " + itemTag);
        
        // 如果工作模式是"all"，显示所有菜单项
        if ("all".equals(workMode) || itemTag.equals(workMode)) {
            menuItem.setVisible(true);
        } else {
            menuItem.setVisible(false);
        }
    }
    
    /**
     * 根据菜单项ID获取其标签类型
     */
    private String getMenuItemTag(int itemId) {
        // 工作相关的菜单项
        if (itemId == R.id.nav_subscriptions ||
            itemId == R.id.nav_battery ||
            itemId == R.id.nav_performance_test ||
            itemId == R.id.nav_wifi ||
            itemId == R.id.nav_test ||
            itemId == R.id.nav_compass ||
            itemId == R.id.nav_sort) {
            return "work";
        }
        // 生活相关的菜单项
        else if (itemId == R.id.nav_todo ||
                itemId == R.id.nav_calorie ||
                itemId == R.id.nav_calendar ||
                itemId == R.id.nav_timer ||
                itemId == R.id.nav_pyp ||
                itemId == R.id.nav_turntable ||
                itemId == R.id.nav_food_record ||
                itemId == R.id.nav_scrummage ||
                itemId == R.id.nav_consumption) {
            return "life";
        }

        else if (itemId == R.id.nav_tzfe ||
                itemId == R.id.nav_tetris ||
                itemId == R.id.nav_minecraft) {
            return "game";
        }

        // 默认为通用菜单项
        else {
            return "all";
        }
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
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(
            R.anim.slide_in_left,  // 返回时，上一个Fragment进入动画
            R.anim.slide_out_right  // 返回时，当前Fragment退出动画
        );
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
            R.anim.slide_in_left,   // 返回时入动画
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
        } else if (id == R.id.nav_pyp) {
            loadFragment(new PyprenderFragment());
        } else if (id == R.id.nav_turntable) {
            loadFragment(new TurntableFragment());
        } else if (id == R.id.nav_food_record) {
            loadFragment(new FoodRecordFragment());
        }  else if (id == R.id.nav_scrummage) {
            loadFragment(new ScrummageFragment());
        } else if (id == R.id.nav_consumption) {
            loadFragment(new ConsumptionFragment());
        } else if (id == R.id.nav_minecraft) {
            loadFragment(new MinecraftLocationFragment());
        } else if (id == R.id.nav_test) {
            loadFragment(new TestFragment());
        } else if (id == R.id.nav_wifi){
            loadFragment(new WifiFragment());
        } else if (id == R.id.nav_tzfe){
            loadFragment(new TzfeFragment());
        } else if (id == R.id.nav_tetris){
            loadFragment(new TetrisFragment());
        } else if (id == R.id.nav_compass){
            loadFragment(new CompassFragment());
        } else if (id == R.id.nav_sort){
            loadFragment(new SortFragment());
        }

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
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.setCustomAnimations(
                    R.anim.slide_in_left,  // 返回时，上一个Fragment进入动画
                    R.anim.slide_out_right  // 返回时，当前Fragment退出动画
                );
                fm.popBackStack();
                transaction.commit();
            } else if (fm.getBackStackEntryCount() == 1) {
                // 如果只剩一个Fragment，清空回退栈并加载首页
                FragmentTransaction transaction = fm.beginTransaction();
                transaction.setCustomAnimations(
                    R.anim.slide_in_left,  // 返回时，上一个Fragment进入动画
                    R.anim.slide_out_right  // 返回时，当前Fragment退出动画
                );
                transaction.replace(R.id.content_frame, new HomeFragment());
                fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                transaction.commit();
                isHome = true;
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
        
        // 获取当前主题
        String currentTheme = shardPerfenceSetting.getHomeTheme();
        Log.d("MainActivity-color", "当前主题: " + currentTheme);
        
        // 设置背景颜色
        LiquidBackgroundView liquidBackgroundView = findViewById(R.id.liquid_background);
        if (liquidBackgroundView != null) {
            int[] startColors = ColorCalibration.judgeColor(currentTheme, 0);
            int[] endColors = ColorCalibration.judgeColor(currentTheme, 1);
            Log.d("MainActivity-color", "设置背景颜色");
            liquidBackgroundView.setColors(startColors, endColors);
            
            // 获取当前实际颜色
            String actualBottomColor = liquidBackgroundView.getActualDisplayedBottomColor();
            String actualTopColor = liquidBackgroundView.getActualDisplayedTopColor();
            Log.d("MainActivity-color", "当前实际颜色 - 顶部: " + actualTopColor + ", 底部: " + actualBottomColor);
            
            // 更新 ColorCalibration 中的实际颜色
            ColorCalibration.setActualDisplayedColors(actualBottomColor, actualTopColor);
        } else {
            Log.e("MainActivity-color", "liquidBackgroundView is null");
        }
        
        // 根据工作模式过滤菜单项
        filterMenuItemsByWorkMode();
        
        // 如果当前是首页，刷新首页以更新底部菜单
        if (isHome) {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (currentFragment instanceof HomeFragment) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.detach(currentFragment);
                transaction.attach(currentFragment);
                transaction.commit();
            }
        }
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
                        //drawerLayout.openDrawer(GravityCompat.START);
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

                                        Log.d("MainActivity", String.format(
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
                    Log.e("MainActivity", "Error capturing screen colors", e);
                }
            }
        });
    }
}