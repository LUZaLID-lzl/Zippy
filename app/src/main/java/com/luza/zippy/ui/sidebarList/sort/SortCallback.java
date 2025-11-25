package com.luza.zippy.ui.sidebarList.sort;

import java.util.List;

public interface SortCallback {
    void onStep(List<Integer> data, int comparingIndex1, int comparingIndex2, int sortedIndex);
    void onComplete(List<Integer> data);
    boolean shouldPause(); // 暂停检查方法
    int getSortSpeed(); // 获取排序速度 (1=很慢, 2=慢, 3=中等, 4=快, 5=很快)
} 