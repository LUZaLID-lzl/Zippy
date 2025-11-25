package com.luza.zippy.ui.sidebarList.sort;

import java.util.List;

public interface SortAlgorithm {
    void sort(List<Integer> data, SortCallback callback);
    String getName();
}

// 冒泡排序
class BubbleSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        int n = arr.size();
        
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                // 检查暂停状态
                while (callback.shouldPause()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                // 高亮比较的元素
                callback.onStep(arr, j, j + 1, -1);
                
                if (arr.get(j) > arr.get(j + 1)) {
                    // 交换元素
                    int temp = arr.get(j);
                    arr.set(j, arr.get(j + 1));
                    arr.set(j + 1, temp);
                    
                    // 更新显示
                    callback.onStep(arr, j, j + 1, -1);
                }
                
                try {
                    Thread.sleep(getDelayTime(callback.getSortSpeed()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            // 标记已排序的部分
            callback.onStep(arr, -1, -1, n - i - 1);
        }
        
        callback.onComplete(arr);
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 800; // 很慢
            case 2: return 600; // 慢
            case 3: return 400; // 中等
            case 4: return 200; // 快
            case 5: return 100; // 很快
            default: return 400;
        }
    }
    
    @Override
    public String getName() {
        return "冒泡排序";
    }
}

// 选择排序
class SelectionSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        int n = arr.size();
        
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            
            for (int j = i + 1; j < n; j++) {
                // 检查暂停状态
                while (callback.shouldPause()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                // 高亮比较的元素
                callback.onStep(arr, minIndex, j, -1);
                
                if (arr.get(j) < arr.get(minIndex)) {
                    minIndex = j;
                }
                
                try {
                    Thread.sleep(getDelayTime(callback.getSortSpeed()) / 2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            // 交换元素
            if (minIndex != i) {
                int temp = arr.get(i);
                arr.set(i, arr.get(minIndex));
                arr.set(minIndex, temp);
                
                callback.onStep(arr, i, minIndex, -1);
            }
            
            // 标记已排序的部分
            callback.onStep(arr, -1, -1, i);
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()) / 3);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        callback.onComplete(arr);
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 600; // 很慢
            case 2: return 450; // 慢
            case 3: return 300; // 中等
            case 4: return 150; // 快
            case 5: return 75;  // 很快
            default: return 300;
        }
    }
    
    @Override
    public String getName() {
        return "选择排序";
    }
}

// 插入排序
class InsertionSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        int n = arr.size();
        
        for (int i = 1; i < n; i++) {
            int key = arr.get(i);
            int j = i - 1;
            
            // 高亮当前要插入的元素
            callback.onStep(arr, i, j, -1);
            
            while (j >= 0 && arr.get(j) > key) {
                // 检查暂停状态
                while (callback.shouldPause()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                // 高亮比较的元素
                callback.onStep(arr, j, j + 1, -1);
                
                arr.set(j + 1, arr.get(j));
                j--;
                
                try {
                    Thread.sleep(getDelayTime(callback.getSortSpeed()));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            arr.set(j + 1, key);
            callback.onStep(arr, -1, -1, j + 1);
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()) / 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        callback.onComplete(arr);
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 600; // 很慢
            case 2: return 450; // 慢
            case 3: return 300; // 中等
            case 4: return 150; // 快
            case 5: return 75;  // 很快
            default: return 300;
        }
    }
    
    @Override
    public String getName() {
        return "插入排序";
    }
}

// 快速排序
class QuickSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        quickSort(arr, 0, arr.size() - 1, callback);
        callback.onComplete(arr);
    }
    
    private void quickSort(List<Integer> arr, int low, int high, SortCallback callback) {
        if (low < high) {
            int pi = partition(arr, low, high, callback);
            quickSort(arr, low, pi - 1, callback);
            quickSort(arr, pi + 1, high, callback);
        }
    }
    
    private int partition(List<Integer> arr, int low, int high, SortCallback callback) {
        int pivot = arr.get(high);
        int i = low - 1;
        
        for (int j = low; j < high; j++) {
            // 检查暂停状态
            while (callback.shouldPause()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return i;
                }
            }
            
            // 高亮比较的元素
            callback.onStep(arr, j, high, -1);
            
            if (arr.get(j) < pivot) {
                i++;
                // 交换元素
                int temp = arr.get(i);
                arr.set(i, arr.get(j));
                arr.set(j, temp);
                
                callback.onStep(arr, i, j, -1);
            }
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return i;
            }
        }
        
        // 放置pivot到正确位置
        int temp = arr.get(i + 1);
        arr.set(i + 1, arr.get(high));
        arr.set(high, temp);
        
        callback.onStep(arr, -1, -1, i + 1);
        
        try {
            Thread.sleep(getDelayTime(callback.getSortSpeed()) / 2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return i + 1;
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 800; // 很慢
            case 2: return 600; // 慢
            case 3: return 400; // 中等
            case 4: return 200; // 快
            case 5: return 100; // 很快
            default: return 400;
        }
    }
    
    @Override
    public String getName() {
        return "快速排序";
    }
}

// 归并排序
class MergeSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        mergeSort(arr, 0, arr.size() - 1, callback);
        callback.onComplete(arr);
    }
    
    private void mergeSort(List<Integer> arr, int left, int right, SortCallback callback) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(arr, left, mid, callback);
            mergeSort(arr, mid + 1, right, callback);
            merge(arr, left, mid, right, callback);
        }
    }
    
    private void merge(List<Integer> arr, int left, int mid, int right, SortCallback callback) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        
        List<Integer> leftArr = new java.util.ArrayList<>();
        List<Integer> rightArr = new java.util.ArrayList<>();
        
        for (int i = 0; i < n1; i++) {
            leftArr.add(arr.get(left + i));
        }
        for (int j = 0; j < n2; j++) {
            rightArr.add(arr.get(mid + 1 + j));
        }
        
        int i = 0, j = 0, k = left;
        
        while (i < n1 && j < n2) {
            // 检查暂停状态
            while (callback.shouldPause()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            // 高亮比较的元素
            callback.onStep(arr, left + i, mid + 1 + j, -1);
            
            if (leftArr.get(i) <= rightArr.get(j)) {
                arr.set(k, leftArr.get(i));
                i++;
            } else {
                arr.set(k, rightArr.get(j));
                j++;
            }
            k++;
            
            callback.onStep(arr, -1, -1, k - 1);
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        while (i < n1) {
            arr.set(k, leftArr.get(i));
            i++;
            k++;
            callback.onStep(arr, -1, -1, k - 1);
        }
        
        while (j < n2) {
            arr.set(k, rightArr.get(j));
            j++;
            k++;
            callback.onStep(arr, -1, -1, k - 1);
        }
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 600; // 很慢
            case 2: return 450; // 慢
            case 3: return 300; // 中等
            case 4: return 150; // 快
            case 5: return 75;  // 很快
            default: return 300;
        }
    }
    
    @Override
    public String getName() {
        return "归并排序";
    }
}

// 堆排序
class HeapSort implements SortAlgorithm {
    @Override
    public void sort(List<Integer> data, SortCallback callback) {
        List<Integer> arr = new java.util.ArrayList<>(data);
        int n = arr.size();
        
        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i, callback);
        }
        
        // 逐个提取元素
        for (int i = n - 1; i > 0; i--) {
            // 交换根节点和最后一个节点
            int temp = arr.get(0);
            arr.set(0, arr.get(i));
            arr.set(i, temp);
            
            callback.onStep(arr, 0, i, i);
            
            // 对剩余元素重新构建最大堆
            heapify(arr, i, 0, callback);
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        callback.onComplete(arr);
    }
    
    private void heapify(List<Integer> arr, int n, int i, SortCallback callback) {
        int largest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        
        // 检查暂停状态
        while (callback.shouldPause()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        
        if (left < n) {
            callback.onStep(arr, largest, left, -1);
            if (arr.get(left) > arr.get(largest)) {
                largest = left;
            }
        }
        
        if (right < n) {
            callback.onStep(arr, largest, right, -1);
            if (arr.get(right) > arr.get(largest)) {
                largest = right;
            }
        }
        
        if (largest != i) {
            int temp = arr.get(i);
            arr.set(i, arr.get(largest));
            arr.set(largest, temp);
            
            callback.onStep(arr, i, largest, -1);
            
            try {
                Thread.sleep(getDelayTime(callback.getSortSpeed()) / 2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            
            heapify(arr, n, largest, callback);
        }
    }
    
    private int getDelayTime(int speed) {
        switch (speed) {
            case 1: return 800; // 很慢
            case 2: return 600; // 慢
            case 3: return 400; // 中等
            case 4: return 200; // 快
            case 5: return 100; // 很快
            default: return 400;
        }
    }
    
    @Override
    public String getName() {
        return "堆排序";
    }
} 