package com.luza.zippy.ui.base;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentManager;

import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.fragments.HomeFragment;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 设置布局状态
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.btn_back).getParent();

        // 设置标题
        TextView titleText = view.findViewById(R.id.tv_title);
        titleText.setText(getTitle());

        // 设置返回按钮
        ImageButton backButton = view.findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                // 添加返回动画
                FragmentManager fm = getActivity().getSupportFragmentManager();
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
                }
            }
        });

        ShardPerfenceSetting setting = new ShardPerfenceSetting(getContext());
        if (setting.getArrange()) {
            //HORIZONTAL
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(0, 16, 16, 16);
            titleText.setPadding(0,16,16,0);
        } else {
            //VERTICAL
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(16, 16, 0, 0);
            titleText.setPadding(32,16,0,0);
            titleText.setTextSize(40);
        }

        // 初始化视图
        initViews(view);
    }

    // 子类必须实现的方法
    protected abstract String getTitle();
    protected abstract void initViews(View view);

    // 添加跳转到新Fragment的方法
    protected void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // 新Fragment进入动画
                            R.anim.slide_out_left,   // 当前Fragment退出动画
                            R.anim.slide_in_left,    // 返回时，上一个Fragment进入动画
                            R.anim.slide_out_right   // 返回时，当前Fragment退出动画
                    )
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
} 