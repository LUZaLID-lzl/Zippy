package com.luza.zippy.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.ui.fragments.HomeFragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置标题
        TextView titleText = view.findViewById(R.id.tv_title);
        titleText.setText(getTitle());

        // 设置返回按钮
        ImageButton backButton = view.findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 初始化视图
        initViews(view);

    }



    // 子类必须实现的方法
    protected abstract String getTitle();
    protected abstract void initViews(View view);
} 