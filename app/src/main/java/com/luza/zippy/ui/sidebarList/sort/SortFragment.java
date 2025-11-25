package com.luza.zippy.ui.sidebarList.sort;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.android.material.textfield.TextInputLayout;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import com.google.android.material.slider.Slider;

/**
 * 排序算法演示Fragment
 */
public class SortFragment extends BaseFragment {

    private SortVisualizationView sortVisualization;
    private TextView tvStepCount;
    private Button btnGenerateData;
    private Button btnStartSort;
    private Button btnPauseResume; // 替换重置按钮为暂停/继续按钮
    
    // 占位符布局
    private View placeholderLayout;
    
    // 下拉选择框相关
    private TextInputLayout algorithmSelectorLayout;
    private AutoCompleteTextView algorithmSelector;
    private ArrayAdapter<String> algorithmAdapter;
    
    // 滑动条相关
    private Slider sliderDataCount;
    private Slider sliderSortSpeed;
    private TextView tvDataCount;
    private TextView tvSortSpeed;
    
    private List<Integer> originalData = new ArrayList<>();
    private SortAlgorithm currentAlgorithm;
    private Handler mainHandler;
    private AtomicBoolean isSorting = new AtomicBoolean(false);
    private AtomicBoolean isPaused = new AtomicBoolean(false); // 暂停状态
    private AtomicBoolean isFragmentActive = new AtomicBoolean(true);
    private int stepCount = 0;
    
    // 参数设置
    private int dataCount = 15; // 默认数据个数
    private int sortSpeed = 3; // 默认排序速度 (1=很慢, 2=慢, 3=中等, 4=快, 5=很快)
    
    // 算法名称数组
    private String[] algorithmNames;
    private SortAlgorithm[] algorithms;
    
    // 排序线程控制
    private Thread sortingThread;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sort, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_sort);
    }

    @Override
    protected void initViews(View view) {
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化视图
        sortVisualization = view.findViewById(R.id.sort_visualization);
        tvStepCount = view.findViewById(R.id.tv_step_count);
        btnGenerateData = view.findViewById(R.id.btn_generate_data);
        btnStartSort = view.findViewById(R.id.btn_start_sort);
        btnPauseResume = view.findViewById(R.id.btn_reset); // 重置按钮替换为暂停/继续按钮
        
        // 初始化占位符布局
        placeholderLayout = view.findViewById(R.id.layout_visualization_placeholder);
        
        // 初始化下拉选择框
        algorithmSelectorLayout = view.findViewById(R.id.algorithm_selector_layout);
        algorithmSelector = view.findViewById(R.id.algorithm_selector);
        
        // 初始化滑动条
        sliderDataCount = view.findViewById(R.id.slider_data_count);
        sliderSortSpeed = view.findViewById(R.id.slider_sort_speed);
        tvDataCount = view.findViewById(R.id.tv_data_count);
        tvSortSpeed = view.findViewById(R.id.tv_sort_speed);
        
        // 初始化算法数组
        initAlgorithms();
        
        // 设置下拉选择框
        setupAlgorithmSelector();
        
        // 设置滑动条
        setupSliders();
        
        // 设置按钮点击事件
        setupButtonListeners();
        
        // 初始化状态
        updateUI();
        updateVisualizationPlaceholder(); // 更新占位符显示
    }
    
    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive.set(true);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive.set(false);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive.set(false);
        isSorting.set(false);
    }
    
    private void initAlgorithms() {
        // 获取算法名称
        algorithmNames = new String[]{
            getString(R.string.sort_bubble),
            getString(R.string.sort_selection),
            getString(R.string.sort_insertion),
            getString(R.string.sort_quick),
            getString(R.string.sort_merge),
            getString(R.string.sort_heap)
        };
        
        // 初始化算法实例
        algorithms = new SortAlgorithm[]{
            new BubbleSort(),
            new SelectionSort(),
            new InsertionSort(),
            new QuickSort(),
            new MergeSort(),
            new HeapSort()
        };
    }

    private void setupAlgorithmSelector() {
        // 创建适配器
        algorithmAdapter = new ArrayAdapter<>(getContext(), 
            android.R.layout.simple_dropdown_item_1line, algorithmNames);
        algorithmSelector.setAdapter(algorithmAdapter);
        
        // 设置选择监听器
        algorithmSelector.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < algorithms.length) {
                selectAlgorithm(algorithms[position]);
            }
        });
    }

    private void setupSliders() {
        // 数据个数滑动条
        sliderDataCount.addOnChangeListener((slider, value, fromUser) -> {
            dataCount = (int) value;
            tvDataCount.setText(String.valueOf(dataCount));
        });
        
        // 排序速度滑动条
        sliderSortSpeed.addOnChangeListener((slider, value, fromUser) -> {
            sortSpeed = (int) value;
            String speedText;
            switch (sortSpeed) {
                case 1: speedText = "很慢"; break;
                case 2: speedText = "慢"; break;
                case 3: speedText = "中等"; break;
                case 4: speedText = "快"; break;
                case 5: speedText = "很快"; break;
                default: speedText = "中等"; break;
            }
            tvSortSpeed.setText(speedText);
        });
        
        // 设置初始值
        tvDataCount.setText(String.valueOf(dataCount));
        tvSortSpeed.setText("中等");
    }

    private void setupButtonListeners() {
        // 控制按钮
        btnGenerateData.setOnClickListener(v -> generateRandomData());
        btnStartSort.setOnClickListener(v -> startSorting());
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
    }
    
    private void selectAlgorithm(SortAlgorithm algorithm) {
        if (isSorting.get()) {
            Toast.makeText(getContext(), getString(R.string.sort_wait_complete), Toast.LENGTH_SHORT).show();
            return;
        }
        
        currentAlgorithm = algorithm;
        updateUI();
        Toast.makeText(getContext(), getString(R.string.sort_algorithm_selected, algorithm.getName()), Toast.LENGTH_SHORT).show();
    }
    
    private void generateRandomData() {
        if (isSorting.get()) {
            Toast.makeText(getContext(), getString(R.string.sort_wait_complete), Toast.LENGTH_SHORT).show();
            return;
        }
        
        originalData.clear();
        Random random = new Random();
        
        // 使用滑动条设置的数据个数
        for (int i = 0; i < dataCount; i++) {
            originalData.add(random.nextInt(90) + 10); // 10到99的随机数
        }
        
        sortVisualization.setData(originalData);
        stepCount = 0;
        updateUI();
        updateVisualizationPlaceholder(); // 更新占位符显示
        
        Toast.makeText(getContext(), getString(R.string.sort_data_generated, dataCount), Toast.LENGTH_SHORT).show();
    }
    
    private void startSorting() {
        if (isSorting.get()) {
            Toast.makeText(getContext(), getString(R.string.sort_wait_complete), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentAlgorithm == null) {
            Toast.makeText(getContext(), getString(R.string.sort_select_first), Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (originalData.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.sort_generate_first), Toast.LENGTH_SHORT).show();
            return;
        }
        
        isSorting.set(true);
        stepCount = 0;
        updateUI();
        
        // 在新线程中执行排序
        sortingThread = new Thread(() -> {
            currentAlgorithm.sort(originalData, new SortCallback() {
                @Override
                public void onStep(List<Integer> data, int comparingIndex1, int comparingIndex2, int sortedIndex) {
                    if (!isFragmentActive.get() || !isSorting.get()) {
                        return;
                    }
                    
                    stepCount++;
                    mainHandler.post(() -> {
                        if (isFragmentActive.get() && sortVisualization != null) {
                            sortVisualization.updateData(data);
                            sortVisualization.setComparingIndices(comparingIndex1, comparingIndex2);
                            sortVisualization.setSortedIndex(sortedIndex);
                            updateUI();
                        }
                    });
                }
                
                @Override
                public void onComplete(List<Integer> data) {
                    mainHandler.post(() -> {
                        if (isFragmentActive.get()) {
                            isSorting.set(false);
                            isPaused.set(false); // 重置暂停状态
                            if (sortVisualization != null) {
                                sortVisualization.resetHighlights();
                            }
                            updateUI();
                            Toast.makeText(getContext(), getString(R.string.sort_complete), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                
                @Override
                public boolean shouldPause() {
                    return isPaused.get();
                }
                
                @Override
                public int getSortSpeed() {
                    return sortSpeed;
                }
            });
        });
        sortingThread.start();
    }
    
    private void resetData() {
        if (isSorting.get()) {
            Toast.makeText(getContext(), getString(R.string.sort_wait_complete), Toast.LENGTH_SHORT).show();
            return;
        }
        
        sortVisualization.resetToOriginal();
        stepCount = 0;
        updateUI();
        Toast.makeText(getContext(), getString(R.string.sort_data_reset), Toast.LENGTH_SHORT).show();
    }

    private void togglePauseResume() {
        if (!isSorting.get()) {
            Toast.makeText(getContext(), getString(R.string.sort_not_sorting), Toast.LENGTH_SHORT).show();
            return;
        }

        isPaused.set(!isPaused.get());
        updateUI();
        
        String message = isPaused.get() ? getString(R.string.sort_paused) : getString(R.string.sort_resumed);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateUI() {
        if (!isFragmentActive.get()) return;
        
        // 更新按钮状态
        boolean hasData = !originalData.isEmpty();
        boolean canSort = hasData && currentAlgorithm != null && !isSorting.get();
        
        btnGenerateData.setEnabled(!isSorting.get());
        btnStartSort.setEnabled(canSort);
        
        // 暂停/继续按钮状态
        if (isSorting.get()) {
            btnPauseResume.setEnabled(true);
            btnPauseResume.setText(isPaused.get() ? getString(R.string.sort_resume) : getString(R.string.sort_pause));
        } else {
            btnPauseResume.setEnabled(false);
            btnPauseResume.setText(getString(R.string.sort_pause));
        }
        
        // 更新算法选择器状态
        boolean canSelectAlgorithm = !isSorting.get();
        algorithmSelector.setEnabled(canSelectAlgorithm);
        
        // 更新滑动条状态
        sliderDataCount.setEnabled(!isSorting.get());
        sliderSortSpeed.setEnabled(true); // 排序速度可以实时调整
        
        tvStepCount.setText(getString(R.string.sort_steps, stepCount));
        
        // 更新开始按钮文本
        if (isSorting.get()) {
            if (isPaused.get()) {
                btnStartSort.setText(getString(R.string.sort_paused));
            } else {
                btnStartSort.setText(getString(R.string.sort_sorting));
            }
        } else {
            btnStartSort.setText(getString(R.string.sort_start));
        }
    }

    private void updateVisualizationPlaceholder() {
        if (placeholderLayout != null) {
            placeholderLayout.setVisibility(originalData.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}