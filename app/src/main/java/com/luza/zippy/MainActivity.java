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
import com.luza.zippy.ui.sidebarList.deviceInformation.DeviceInfoFragment;
import com.luza.zippy.ui.sidebarList.battery.BatteryFragment;
import com.luza.zippy.ui.sidebarList.settings.Util;
import com.luza.zippy.ui.sidebarList.test.TestFragment;
import com.luza.zippy.ui.sidebarList.todo.TodoFragment;
import com.luza.zippy.ui.sidebarList.calorie.CalorieFragment;

import java.util.Locale;

/**
 * MainActivity作为应用的主入口活动
 * 负责初始化主要UI组件和侧边栏导航
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnTouchListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean isHome = true; // 添加标记，用于判断是否在首页
    private GestureDetectorCompat gestureDetector;
    public static Context mContext;
    private ShardPerfenceSetting shardPerfenceSetting;
    public static boolean needRecreate = true;

    private Util util;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 在 super.onCreate 之前设置主题
        util = new Util();
        shardPerfenceSetting = new ShardPerfenceSetting(this);
        shardPerfenceSetting.update();
        util.updateTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupNavigationDrawer();
        setupGestureDetector();

        if (savedInstanceState == null) {
            android.util.Log.d("liziluo","test_log: ");
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
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, new HomeFragment());
        transaction.commit();
        isHome = true;
    }

    /**
     * 加载其他Fragment
     */
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
            R.anim.slide_in_right,  // 进入动画
            R.anim.slide_out_left,  // 退出动画
            R.anim.slide_in_left,   // 返回时进入动画
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
        }else if (id == R.id.nav_test) {
            loadFragment(new TestFragment());
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!isHome) {
            // 不在首页时才响应返回键，并添加动画
            FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                // 添加返回动画
                overridePendingTransition(
                    R.anim.slide_in_left,  // 新页面从左边进入
                    R.anim.slide_out_right  // 当前页面从右边退出
                );
            }
        }
        // 在首页时不做任何响应
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
        android.util.Log.d("liziluo","onResume: " + MainActivity.needRecreate);
        getSupportFragmentManager().addOnBackStackChangedListener(backStackChangedListener);
        util.updateLocale(this);

        if (MainActivity.needRecreate){
            android.util.Log.d("liziluo","need update: ");
            updateTheme();
        }
        initViews();
    }

    public void updateTheme(){
        this.recreate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSupportFragmentManager().removeOnBackStackChangedListener(backStackChangedListener);
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
}