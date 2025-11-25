package com.luza.zippy.ui.sidebarList.turntable;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TurntableCoinFragment extends BaseFragment {
    private ImageView coinImage;
    private MaterialButton tossButton;
    private TextView resultText;
    private Random random = new Random();
    private boolean isAnimating = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turntable_coin, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_turntable_coin);
    }

    @Override
    protected void initViews(View view) {
        coinImage = view.findViewById(R.id.image_coin);
        tossButton = view.findViewById(R.id.btn_toss);
        resultText = view.findViewById(R.id.text_result);

        // 设置初始图片和角度
        try {
            Glide.with(this)
                .load(R.drawable.coin_front)
                .into(coinImage);
            
            coinImage.setRotationX(60f);
            resultText.setText("正面");
        } catch (Exception e) {
            e.printStackTrace();
        }

        tossButton.setOnClickListener(v -> {
            if (!isAnimating) {
                tossCoin();
            }
        });
    }

    private void tossCoin() {
        isAnimating = true;
        tossButton.setEnabled(false);

        // 在动画开始前设置初始图片和角度
        try {
            Glide.with(this)
                .load(R.drawable.coin_front)
                .into(coinImage);
            coinImage.setRotationX(60f);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建动画集合
        AnimatorSet animatorSet = new AnimatorSet();

        // 向上抛的动画（快速上升）
        ObjectAnimator moveUpAnim = ObjectAnimator.ofFloat(coinImage, "translationY", 0f, -800f);
        moveUpAnim.setDuration(800); // 增加上升时间
        moveUpAnim.setInterpolator(new android.view.animation.AccelerateInterpolator(1.5f));

        // 下落动画（较慢，有回弹效果）
        ObjectAnimator moveDownAnim = ObjectAnimator.ofFloat(coinImage, "translationY", -800f, 0f);
        moveDownAnim.setStartDelay(800); // 增加延迟时间
        moveDownAnim.setDuration(1000); // 增加下落时间
        moveDownAnim.setInterpolator(new android.view.animation.BounceInterpolator());

        // 旋转动画（从初始倾斜角度开始）
        ValueAnimator rotateXAnim = ValueAnimator.ofFloat(60f, 2160f);
        rotateXAnim.setDuration(1800); // 增加旋转时间
        rotateXAnim.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            coinImage.setRotationX(value);
            
            // 根据旋转角度切换图片
            float normalizedAngle = value % 360;
            if (normalizedAngle < 0) normalizedAngle += 360;
            
            // 当硬币处于90度到270度之间时显示背面
            if (normalizedAngle > 90 && normalizedAngle < 270) {
                try {
                    Glide.with(requireContext())
                        .load(R.drawable.coin_back)
                        .into(coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Glide.with(requireContext())
                        .load(R.drawable.coin_front)
                        .into(coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        
        ObjectAnimator rotateYAnim = ObjectAnimator.ofFloat(coinImage, "rotationY", 0f, 720f);
        rotateYAnim.setDuration(1800); // 增加旋转时间

        // 缩放动画
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(coinImage, "scaleX", 1f, 0.5f, 1f);
        scaleXAnim.setDuration(1800);
        
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(coinImage, "scaleY", 1f, 0.5f, 1f);
        scaleYAnim.setDuration(1800);

        // 组合所有动画
        animatorSet.playTogether(moveUpAnim, moveDownAnim, rotateXAnim, rotateYAnim, scaleXAnim, scaleYAnim);

        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                boolean isHeads = random.nextBoolean();
                
                float finalRotationX = isHeads ? 60f : 240f;
                coinImage.setRotationX(finalRotationX);
                coinImage.setRotationY(0f);
                
                try {
                    Glide.with(requireContext())
                        .load(isHeads ? R.drawable.coin_front : R.drawable.coin_back)
                        .into(coinImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // 更新结果显示
                String result = isHeads ? "正面" : "反面";
                resultText.setText(result);
                
                // 重置状态
                isAnimating = false;
                tossButton.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();
    }

    // 注释掉整个 playFlipSound 方法
    /*
    private void playFlipSound() {
        try {
            android.media.MediaPlayer mediaPlayer = android.media.MediaPlayer.create(requireContext(), R.raw.coin_flip);
            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */
}