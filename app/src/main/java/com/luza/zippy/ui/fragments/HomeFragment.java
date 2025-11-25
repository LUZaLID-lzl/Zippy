package com.luza.zippy.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Rect;
import android.view.MotionEvent;

import com.luza.zippy.R;
import com.luza.zippy.SplashActivity;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.sidebarList.battery.BatteryFragment;
import com.luza.zippy.ui.sidebarList.calendar.CalendarFragment;
import com.luza.zippy.ui.sidebarList.calorie.CalorieFragment;
import com.luza.zippy.ui.sidebarList.compass.CompassFragment;
import com.luza.zippy.ui.sidebarList.consumption.ConsumptionFragment;
import com.luza.zippy.ui.sidebarList.deviceInformation.DeviceInfoFragment;
import com.luza.zippy.ui.sidebarList.foodRecord.FoodRecordFragment;
import com.luza.zippy.ui.sidebarList.minecraft.location.MinecraftLocationFragment;
import com.luza.zippy.ui.sidebarList.minecraft.location.MinecraftLocationSettingFragment;
import com.luza.zippy.ui.sidebarList.performance.PerformanceFragment;
import com.luza.zippy.ui.sidebarList.pyprender.PyprenderFragment;
import com.luza.zippy.ui.sidebarList.scrummage.ScrummageFragment;
import com.luza.zippy.ui.sidebarList.sort.SortFragment;
import com.luza.zippy.ui.sidebarList.test.TestFragment;
import com.luza.zippy.ui.sidebarList.tetris.TetrisFragment;
import com.luza.zippy.ui.sidebarList.timer.TimerFragment;
import com.luza.zippy.ui.sidebarList.todo.TodoFragment;
import com.luza.zippy.ui.sidebarList.turntable.TurntableFragment;
import com.luza.zippy.ui.sidebarList.tzfe.TzfeFragment;
import com.luza.zippy.ui.sidebarList.wifi.WifiFragment;
import com.luza.zippy.ui.utils.ImageProcess;
import com.luza.zippy.ui.utils.ColorCalibration;
import com.luza.zippy.ui.views.CenterScaleLayoutManager;
import com.luza.zippy.ui.views.SparkView;
import com.luza.zippy.ui.sidebarList.settings.SettingsFragment;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import android.animation.ValueAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.TypedValue;

public class HomeFragment extends Fragment {
    private FrameLayout sparkContainer;
    private Handler handler;
    private Random random = new Random();
    private boolean isGeneratingSparks = true;
    private ImageView lightningImage;
    private ShardPerfenceSetting shardPerfenceSetting;
    private ImageProcess imageProcess;
    private int animationNum;
    
    // 添加图片切换相关变量
    private Handler imageSwitchHandler;
    private Runnable imageSwitchRunnable;
    private static final int IMAGE_SWITCH_INTERVAL = 20000; // 20秒
    private Bitmap[] currentImages;
    private int currentImageIndex = 0;

    private RecyclerView bottomMenuRecycler;
    private float lastY;
    private static final float SWIPE_THRESHOLD = 50; // 滑动阈值
    private boolean isExpanded = false;
    private static final int COLLAPSED_SPAN_COUNT = 1; // 收起时显示1行
    private static final int EXPANDED_SPAN_COUNT = 3;  // 展开时显示3行

    public int[] capooSpark = {
            R.drawable.ic_hamburg,
            R.drawable.ic_cola,
            R.drawable.ic_fries
    };

    public static int capoo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageProcess = new ImageProcess();
        capoo = capooSpark[random.nextInt(capooSpark.length)];
        procesImage();
        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());
        animationNum = shardPerfenceSetting.getHomeAnimationNum();
        
        // 设置闪电动画
        lightningImage = view.findViewById(R.id.lightning_image);
        
        // 设置电流效果
        sparkContainer = view.findViewById(R.id.spark_container);
        handler = new Handler(Looper.getMainLooper());
        
        // 初始化图片切换处理器
        imageSwitchHandler = new Handler(Looper.getMainLooper());

        // 设置触摸监听
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = event.getY();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = lastY - event.getY();
                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (deltaY > 0 && !isExpanded) {
                            // 向上滑动，展开显示更多行
                            expandGrid();
                        } else if (deltaY < 0 && isExpanded) {
                            // 向下滑动，收起为一行
                            collapseGrid();
                        }
                        lastY = event.getY();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;
            }
            return false;
        });

        // 设置菜单按钮点击事件
        ImageButton menuButton = view.findViewById(R.id.btn_menu);
        menuButton.setOnClickListener(v -> {
            DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        
        // 设置按钮点击事件
        ImageButton settingsButton = view.findViewById(R.id.btn_settings);
        settingsButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.slide_out_left
                    )
                    .replace(R.id.content_frame, new SettingsFragment())
                    .addToBackStack(null)
                    .commit();
            }
        });

        // 初始化底部菜单
        setupBottomMenu(view);

        // 等待视图布局完成后再开始生成粒子
        sparkContainer.post(() -> startGeneratingSparks());
        
        return view;
    }

    private void setupBottomMenu(View view) {
        bottomMenuRecycler = view.findViewById(R.id.bottom_menu_recycler);
        
        // 检查底部菜单背景设置
        Boolean isBottomMenu = shardPerfenceSetting.isBottom_menu_bkg();
        if (!isBottomMenu) {
            bottomMenuRecycler.setVisibility(View.GONE);
            return;
        }
        
        // 设置底部菜单卡片的背景颜色
        androidx.cardview.widget.CardView bottomMenuCard = view.findViewById(R.id.bottom_menu_card);
        if (bottomMenuCard != null) {
            String theme = shardPerfenceSetting.getHomeTheme();
            String cardBackgroundColor = ColorCalibration.getThemeColorType(theme, 2);
            bottomMenuCard.setCardBackgroundColor(android.graphics.Color.parseColor(cardBackgroundColor));

            // 根据用户设置的菜单项大小动态计算卡片高度
            updateBottomMenuCardHeight(bottomMenuCard);
        }

        // 初始状态使用水平LinearLayoutManager
        LinearLayoutManager horizontalManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        bottomMenuRecycler.setLayoutManager(horizontalManager);

        // 添加平滑滚动
        bottomMenuRecycler.setHasFixedSize(true);
        bottomMenuRecycler.setNestedScrollingEnabled(false);
        
        // 调整间距 - 统一间距设置
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing) / 2;
        bottomMenuRecycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, 
                                     @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
                
                if (layoutManager instanceof LinearLayoutManager && 
                    ((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    // 水平布局时只设置左右间距，上下间距由RecyclerView的paddingVertical处理
                    outRect.left = spacing;
                    outRect.right = spacing;
                    outRect.top = 0;
                    outRect.bottom = 0;
                } else if (layoutManager instanceof GridLayoutManager) {
                    // 网格布局时设置四周间距
                    outRect.left = spacing;
                    outRect.right = spacing;
                    outRect.top = spacing;
                    outRect.bottom = spacing;
                }
            }
        });

        // 创建菜单项列表
        List<BottomMenuAdapter.MenuItem> menuItems = new ArrayList<>();
        String currentWorkMode = shardPerfenceSetting.getWorkMode();

        // 添加设备信息菜单项 - 工作
        if ("all".equals(currentWorkMode) || "work".equals(currentWorkMode)) {
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_subscriptions, R.drawable.ic_device, getString(R.string.menu_device_info)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_battery, R.drawable.ic_battery, getString(R.string.menu_battery)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_performance_test, R.drawable.ic_performance, getString(R.string.menu_performance_test)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_test, R.drawable.ic_test, getString(R.string.menu_test)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_wifi, R.drawable.ic_wifi, getString(R.string.menu_wifi)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_compass, R.drawable.ic_compass, getString(R.string.menu_compass)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_sort, R.drawable.ic_sort, getString(R.string.menu_sort)));
        }
        
        // 添加生活相关菜单项
        if ("all".equals(currentWorkMode) || "life".equals(currentWorkMode)) {
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_todo, R.drawable.ic_todo, getString(R.string.menu_todo)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_calorie, R.drawable.ic_calorie, getString(R.string.menu_calorie)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_calendar, R.drawable.ic_calendar, getString(R.string.menu_calendar)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_pyp, R.drawable.ic_pyp, getString(R.string.menu_pyp)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_turntable, R.drawable.ic_turntable, getString(R.string.menu_turntable)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_food_record, R.drawable.ic_food_record, getString(R.string.menu_food_record)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_scrummage, R.drawable.ic_scrummage, getString(R.string.menu_scrummage)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_consumption, R.drawable.ic_consumption, getString(R.string.menu_consumption)));
            
            // 计时器只在激活状态下显示
            if (shardPerfenceSetting.getActivate()) {
                menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_timer, R.drawable.ic_timer, getString(R.string.menu_timer)));
            }
        }

        // 添加设备信息菜单项 - 游戏
        if ("all".equals(currentWorkMode) || "game".equals(currentWorkMode)) {
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_tzfe, R.drawable.ic_tzfe, getString(R.string.menu_tzfe)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_tetris, R.drawable.ic_tetris, getString(R.string.menu_tetris)));
            menuItems.add(new BottomMenuAdapter.MenuItem(R.id.nav_minecraft, R.drawable.ic_minecraft, getString(R.string.menu_minecraft)));
        }

        // 更新适配器初始化
        BottomMenuAdapter adapter = new BottomMenuAdapter(
            requireContext(),  // 添加 context 参数
            menuItems, 
            item -> {
                if (getActivity() != null) {
                    Fragment targetFragment = null;
                    switch (item.id) {
                        case R.id.nav_subscriptions:
                            targetFragment = new DeviceInfoFragment();
                            break;
                        case R.id.nav_battery:
                            targetFragment = new BatteryFragment();
                            break;
                        case R.id.nav_todo:
                            targetFragment = new TodoFragment();
                            break;
                        case R.id.nav_calorie:
                            targetFragment = new CalorieFragment();
                            break;
                        case R.id.nav_calendar:
                            targetFragment = new CalendarFragment();
                            break;
                        case R.id.nav_compass:
                            targetFragment = new CompassFragment();
                            break;
                        case R.id.nav_performance_test:
                            targetFragment = new PerformanceFragment();
                            break;
                        case R.id.nav_timer:
                            targetFragment = new TimerFragment();
                            break;
                        case R.id.nav_pyp:
                            targetFragment = new PyprenderFragment();
                            break;
                        case R.id.nav_turntable:
                            targetFragment = new TurntableFragment();
                            break;
                        case R.id.nav_food_record:
                            targetFragment = new FoodRecordFragment();
                            break;
                        case R.id.nav_scrummage:
                            targetFragment = new ScrummageFragment();
                            break;
                        case R.id.nav_consumption:
                            targetFragment = new ConsumptionFragment();
                            break;
                        case R.id.nav_minecraft:
                            targetFragment = new MinecraftLocationFragment();
                            break;
                        case R.id.nav_wifi:
                            targetFragment = new WifiFragment();
                            break;
                        case R.id.nav_test:
                            targetFragment = new TestFragment();
                            break;
                        case R.id.nav_tzfe:
                            targetFragment = new TzfeFragment();
                            break;
                        case R.id.nav_tetris:
                            targetFragment = new TetrisFragment();
                            break;
                        case R.id.nav_sort:
                            targetFragment = new SortFragment();
                            break;
                    }

                    if (targetFragment != null) {
                        getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(
                                R.anim.slide_in_right,
                                R.anim.slide_out_left
                            )
                            .replace(R.id.content_frame, targetFragment)
                            .addToBackStack(null)
                            .commit();
                    }
                }
            }
        );

        bottomMenuRecycler.setAdapter(adapter);
    }

    public void procesImage(){

    }

    @Override
    public void onResume() {
        super.onResume();
        // 随机显示皮卡丘图片

        animationNum = shardPerfenceSetting.getHomeAnimationNum();

        switch (shardPerfenceSetting.getHomeTheme()){
            case "pikachu":
                currentImages = SplashActivity.pikachuImages;
                break;
            case "bulbasaur":
                currentImages = SplashActivity.bulbasaurImages;
                break;
            case "squirtle":
                currentImages = SplashActivity.squirtleImages;
                break;
            case "mew":
                currentImages = SplashActivity.mewImages;
                break;
            case "karsa":
                currentImages = SplashActivity.karsaImages;
                break;
            case "capoo":
                currentImages = SplashActivity.capooImages;
                break;
            case "maple":
                currentImages = SplashActivity.mapleImages;
                break;
            case "winter":
                currentImages = SplashActivity.winterImages;
                break;
            case "gengar":
                currentImages = SplashActivity.gengarImages;
                break;
            default:
                currentImages = SplashActivity.squirtleImages;
                break;
        }

        // 设置初始图片（直接显示，不使用淡出动画）
        setInitialImage(currentImages[0]);
        
        // 开始定时切换图片
        startImageSwitching();
        
        // 更新底部菜单卡片背景颜色
        if (getView() != null) {
            androidx.cardview.widget.CardView bottomMenuCard = getView().findViewById(R.id.bottom_menu_card);
            if (bottomMenuCard != null) {
                String theme = shardPerfenceSetting.getHomeTheme();
                String cardBackgroundColor = ColorCalibration.getThemeColorType(theme, 2); // 使用 case 2
                bottomMenuCard.setCardBackgroundColor(android.graphics.Color.parseColor(cardBackgroundColor));
            }
        }
        
        // 如果已经有动画在运行，就不需要重新启动
        restartAnimations();
    }

    /**
     * 设置初始图片（直接显示，不使用动画）
     */
    private void setInitialImage(Bitmap bitmap) {
        if (lightningImage == null || bitmap == null) return;
        
        // 确保图片尺寸合适
        int targetSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - targetSize) / 2;
        int y = (bitmap.getHeight() - targetSize) / 2;
        
        // 裁剪为正方形
        Bitmap squareBitmap = Bitmap.createBitmap(
            bitmap, 
            x, y, 
            targetSize, 
            targetSize
        );
        
        // 直接设置图片，不使用动画
        lightningImage.setImageBitmap(squareBitmap);
        
        // 启动闪电动画
        restartChargingAnimation();
    }

    /**
     * 开始定时切换图片
     */
    private void startImageSwitching() {
        // 取消之前的定时任务
        if (imageSwitchRunnable != null) {
            imageSwitchHandler.removeCallbacks(imageSwitchRunnable);
        }
        
        // 创建新的定时任务
        imageSwitchRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && currentImages != null && currentImages.length > 0) {
                    // 切换到下一张图片
                    currentImageIndex = (currentImageIndex + 1) % currentImages.length;
                    setImageWithAnimation(currentImages[currentImageIndex]);
                    
                    // 继续定时切换
                    imageSwitchHandler.postDelayed(this, IMAGE_SWITCH_INTERVAL);
                }
            }
        };
        
        // 启动定时任务
        imageSwitchHandler.postDelayed(imageSwitchRunnable, IMAGE_SWITCH_INTERVAL);
    }

    /**
     * 设置图片并添加过渡动画
     */
    private void setImageWithAnimation(Bitmap bitmap) {
        if (lightningImage == null || bitmap == null) return;
        
        // 确保图片尺寸合适
        int targetSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - targetSize) / 2;
        int y = (bitmap.getHeight() - targetSize) / 2;
        
        // 裁剪为正方形
        Bitmap squareBitmap = Bitmap.createBitmap(
            bitmap, 
            x, y, 
            targetSize, 
            targetSize
        );
        
        // 创建淡出动画
        Animation fadeOut = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out);
        fadeOut.setDuration(500); // 500ms淡出
        
        // 创建淡入动画
        Animation fadeIn = AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_in);
        fadeIn.setDuration(500); // 500ms淡入
        
        // 设置淡入动画监听器
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // 淡入完成后，重新启动闪电动画
                restartChargingAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        // 设置淡出动画监听器
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                // 淡出完成后，更换图片并开始淡入
                lightningImage.setImageBitmap(squareBitmap);
                lightningImage.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        
        // 开始淡出动画
        lightningImage.startAnimation(fadeOut);
    }

    /**
     * 重新启动闪电动画
     */
    private void restartChargingAnimation() {
        if (lightningImage != null) {
            Animation chargingAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.charging_animation);
            lightningImage.startAnimation(chargingAnimation);
        }
    }

    private void restartAnimations() {
        // 重新启动中心闪电动画（但不影响图片切换）
        restartChargingAnimation();
        
        // 重新生成小闪电
        isGeneratingSparks = true;
        startGeneratingSparks();
        
        // 确保图片切换功能正常运行
        if (currentImages != null && currentImages.length > 0 && imageSwitchRunnable == null) {
            startImageSwitching();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止生成小闪电
        isGeneratingSparks = false;
        handler.removeCallbacksAndMessages(null);
        // 停止中心闪电动画
        lightningImage.clearAnimation();
        
        // 停止图片切换定时任务
        if (imageSwitchRunnable != null) {
            imageSwitchHandler.removeCallbacks(imageSwitchRunnable);
        }
    }

    private void startGeneratingSparks() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isGeneratingSparks && isAdded() && sparkContainer != null 
                    && sparkContainer.getWidth() > 0 && sparkContainer.getHeight() > 0) {
                    // 创建新的闪电
                    SparkView sparkView = new SparkView(requireContext(), 
                        sparkContainer.getWidth(), 
                        sparkContainer.getHeight());
                    sparkContainer.addView(sparkView, 
                        new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    
                    // 随机间隔后生成下一个闪电
                    if (animationNum == 0){
                        return; // 直接返回，停止线程
                    }else{
                        int delayMills = (int) (100 + (100 - 300) * ((animationNum - 10) / (float) (100 - 10))) * 10;
                        handler.postDelayed(this, delayMills);
                    }


                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置闪电动画
        lightningImage = view.findViewById(R.id.lightning_image);
        
        // 设置电流效果
        sparkContainer = view.findViewById(R.id.spark_container);
        handler = new Handler(Looper.getMainLooper());
        
        // 初始化图片切换处理器
        imageSwitchHandler = new Handler(Looper.getMainLooper());

        // 等待视图布局完成后再开始生成粒子
        sparkContainer.post(() -> startGeneratingSparks());
        
        // 如果当前图片数组已初始化，显示初始图片并开始切换
        if (currentImages != null && currentImages.length > 0) {
            setInitialImage(currentImages[currentImageIndex]);
            startImageSwitching();
        }
    }

    private void expandCard() {
        if (isExpanded) return;
        
        // 显示展开内容
        // expandedContent.setVisibility(View.VISIBLE); // This line is removed
        // expandedContent.setAlpha(0f); // This line is removed
        
        // 创建展开动画
        // expandedContent.animate() // This line is removed
        //         .alpha(1f) // This line is removed
        //         .setDuration(300) // This line is removed
        //         .start(); // This line is removed
        
        isExpanded = true;
    }

    private void collapseCard() {
        if (!isExpanded) return;
        
        // 创建收起动画
        // expandedContent.animate() // This line is removed
        //         .alpha(0f) // This line is removed
        //         .setDuration(300) // This line is removed
        //         .withEndAction(() -> { // This line is removed
        //             expandedContent.setVisibility(View.GONE); // This line is removed
        //         }) // This line is removed
        //         .start(); // This line is removed
        
        isExpanded = false;
    }

    private void expandGrid() {
        if (isExpanded) return;
        
        // 获取用户设置的菜单项大小
        int menuItemSize = shardPerfenceSetting.getBottomMenuItemSize();
        
        // 切换到GridLayoutManager，计算合适的列数
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int itemWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
        int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        int columns = Math.max(1, (screenWidth - spacing * 2) / (itemWidthPx + spacing));
        
        GridLayoutManager gridManager = new GridLayoutManager(getContext(), columns);
        bottomMenuRecycler.setLayoutManager(gridManager);
        
        // 展开动画：增加高度以显示所有内容
        View recyclerParent = (View) bottomMenuRecycler.getParent();
        int originalHeight = recyclerParent.getHeight();
        
        // 计算需要的行数和理想高度
        int itemCount = bottomMenuRecycler.getAdapter() != null ? bottomMenuRecycler.getAdapter().getItemCount() : 0;
        int rows = (int) Math.ceil((double) itemCount / columns);
        int itemHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing) / 2; // 使用实际的间距值
        
        // 计算理想总高度：项目高度 + ItemDecoration间距 + RecyclerView的paddingVertical
        int recyclerPadding = (int) (12 * getResources().getDisplayMetrics().density); // 12dp转换为px
        int idealHeight = rows * itemHeightPx + (rows - 1) * itemSpacing * 2 + recyclerPadding * 2;
        
        // 设置最大高度限制（屏幕高度的40%）
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int maxHeight = (int) (screenHeight * 0.4f);
        
        // 确定实际目标高度
        int targetHeight = Math.min(idealHeight, maxHeight);
        
        // 如果内容超过最大高度，启用滑动
        if (idealHeight > maxHeight) {
            bottomMenuRecycler.setNestedScrollingEnabled(true);
            bottomMenuRecycler.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        } else {
            bottomMenuRecycler.setNestedScrollingEnabled(false);
            bottomMenuRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        
        // 第一阶段：轻微的预备动画
        recyclerParent.animate()
            .scaleX(1.01f) // 轻微放大
            .scaleY(1.01f)
            .alpha(0.95f) // 轻微透明
            .setDuration(100)
            .withEndAction(() -> {
                // 第二阶段：高度变化动画，同时为非第一排项目添加渐现效果
                ValueAnimator heightAnimator = ValueAnimator.ofInt(originalHeight, targetHeight);
                heightAnimator.setDuration(400);
                heightAnimator.addUpdateListener(animation -> {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams params = recyclerParent.getLayoutParams();
                    params.height = value;
                    recyclerParent.setLayoutParams(params);
                    
                    // 计算动画进度
                    float progress = animation.getAnimatedFraction();
                    
                    // 为非第一排的项目添加渐现效果
                    for (int i = 0; i < bottomMenuRecycler.getChildCount(); i++) {
                        View child = bottomMenuRecycler.getChildAt(i);
                        int position = bottomMenuRecycler.getChildAdapterPosition(child);
                        
                        if (position >= columns) { // 非第一排的项目
                            // 计算该项目应该出现的时间点
                            int row = position / columns;
                            float itemStartProgress = (float) (row - 1) / (rows - 1) * 0.7f + 0.3f;
                            
                            if (progress >= itemStartProgress) {
                                // 计算该项目的渐现进度
                                float itemProgress = Math.min(1.0f, (progress - itemStartProgress) / (1.0f - itemStartProgress));
                                
                                // 应用渐现效果
                                child.setAlpha(itemProgress);
                                child.setScaleX(0.8f + 0.2f * itemProgress);
                                child.setScaleY(0.8f + 0.2f * itemProgress);
                                child.setTranslationY(20 * (1 - itemProgress));
                            } else {
                                // 尚未到该项目出现的时间
                                child.setAlpha(0f);
                                child.setScaleX(0.8f);
                                child.setScaleY(0.8f);
                                child.setTranslationY(20);
                            }
                        } else {
                            // 第一排保持完全可见
                            child.setAlpha(1.0f);
                            child.setScaleX(1.0f);
                            child.setScaleY(1.0f);
                            child.setTranslationY(0);
                        }
                    }
                });
                
                heightAnimator.start();
                
                // 第三阶段：恢复动画
                recyclerParent.animate()
                    .scaleX(1.0f) // 恢复原始大小
                    .scaleY(1.0f)
                    .alpha(1.0f) // 恢复完全不透明
                    .setDuration(400)
                    .start();
            })
            .start();
        
        isExpanded = true;
    }

    private void collapseGrid() {
        if (!isExpanded) return;
        
        // 获取用户设置的菜单项大小
        int menuItemSize = shardPerfenceSetting.getBottomMenuItemSize();
        
        // 切换回水平LinearLayoutManager
        LinearLayoutManager horizontalManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        bottomMenuRecycler.setLayoutManager(horizontalManager);
        
        // 恢复原始滑动设置
        bottomMenuRecycler.setNestedScrollingEnabled(false);
        bottomMenuRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
        
        // 收起动画：恢复到单行高度
        View recyclerParent = (View) bottomMenuRecycler.getParent();
        int originalHeight = recyclerParent.getHeight();
        
        // 计算单行高度（只包括RecyclerView的paddingVertical，因为水平布局不使用ItemDecoration的上下间距）
        int itemHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
        int recyclerPadding = (int) (12 * getResources().getDisplayMetrics().density); // 12dp转换为px
        int targetHeight = itemHeightPx + recyclerPadding * 2;
        
        // 第一阶段：非第一排项目的消失动画
        for (int i = 0; i < bottomMenuRecycler.getChildCount(); i++) {
            View child = bottomMenuRecycler.getChildAt(i);
            int position = bottomMenuRecycler.getChildAdapterPosition(child);
            
            if (position >= 0) { // 所有项目都参与收起动画
                // 计算延迟时间，从后往前消失
                int delay = Math.max(0, position * 30);
                
                child.animate()
                    .alpha(position == 0 ? 1.0f : 0.0f) // 第一个项目保持可见
                    .scaleX(position == 0 ? 1.0f : 0.8f)
                    .scaleY(position == 0 ? 1.0f : 0.8f)
                    .translationY(position == 0 ? 0 : -20)
                    .setDuration(200)
                    .setStartDelay(delay)
                    .start();
            }
        }
        
        // 第二阶段：高度收起动画
        recyclerParent.animate()
            .scaleX(0.99f) // 轻微缩小
            .scaleY(0.99f)
            .alpha(0.9f) // 轻微透明
            .setDuration(150)
            .setStartDelay(100)
            .withEndAction(() -> {
                // 高度变化动画
                ValueAnimator heightAnimator = ValueAnimator.ofInt(originalHeight, targetHeight);
                heightAnimator.setDuration(300);
                heightAnimator.addUpdateListener(animation -> {
                    int value = (int) animation.getAnimatedValue();
                    ViewGroup.LayoutParams params = recyclerParent.getLayoutParams();
                    params.height = value;
                    recyclerParent.setLayoutParams(params);
                });
                heightAnimator.start();
                
                // 恢复动画
                recyclerParent.animate()
                    .scaleX(1.0f) // 恢复原始大小
                    .scaleY(1.0f)
                    .alpha(1.0f) // 恢复完全不透明
                    .setDuration(300)
                    .start();
            })
            .start();
        
        isExpanded = false;
    }

    private void updateBottomMenuCardHeight(androidx.cardview.widget.CardView bottomMenuCard) {
        if (bottomMenuCard == null) return;

        // 获取用户设置的菜单项大小
        int menuItemSize = shardPerfenceSetting.getBottomMenuItemSize();
        
        // 获取RecyclerView的布局参数
        ViewGroup.LayoutParams params = bottomMenuCard.getLayoutParams();
        
        // 计算RecyclerView的paddingVertical
        int recyclerPadding = (int) (12 * getResources().getDisplayMetrics().density); // 12dp转换为px
        
        // 根据当前布局状态计算高度
        if (!isExpanded) {
            // 收起状态：单行高度
            int itemHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
            params.height = itemHeightPx + recyclerPadding * 2;
        } else {
            // 展开状态：计算多行高度
            int itemCount = bottomMenuRecycler.getAdapter() != null ? bottomMenuRecycler.getAdapter().getItemCount() : 0;
            
            // 计算列数（基于屏幕宽度和菜单项大小）
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int itemWidthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
            int spacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
            int columns = Math.max(1, (screenWidth - spacing * 2) / (itemWidthPx + spacing));
            
            // 计算行数
            int rows = (int) Math.ceil((double) itemCount / columns);
            
            // 计算理想高度
            int itemHeightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, menuItemSize, getResources().getDisplayMetrics());
            int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing) / 2;
            int idealHeight = rows * itemHeightPx + (rows - 1) * itemSpacing * 2 + recyclerPadding * 2;
            
            // 设置最大高度限制（屏幕高度的40%）
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int maxHeight = (int) (screenHeight * 0.4f);
            
            // 确定实际目标高度
            int targetHeight = Math.min(idealHeight, maxHeight);
            params.height = targetHeight;
            
            // 如果内容超过最大高度，启用滑动
            if (idealHeight > maxHeight) {
                bottomMenuRecycler.setNestedScrollingEnabled(true);
                bottomMenuRecycler.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
            } else {
                bottomMenuRecycler.setNestedScrollingEnabled(false);
                bottomMenuRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
            }
        }
        
        // 更新CardView的高度
        bottomMenuCard.setLayoutParams(params);
    }

    /**
     * 刷新底部菜单大小
     * 当设置中的菜单项大小改变时调用此方法
     */
    public void refreshBottomMenuSize() {
        if (getView() != null) {
            androidx.cardview.widget.CardView bottomMenuCard = getView().findViewById(R.id.bottom_menu_card);
            if (bottomMenuCard != null) {
                updateBottomMenuCardHeight(bottomMenuCard);
                
                // 通知适配器数据已改变，触发重新绑定
                if (bottomMenuRecycler.getAdapter() != null) {
                    bottomMenuRecycler.getAdapter().notifyDataSetChanged();
                }
            }
        }
    }
} 