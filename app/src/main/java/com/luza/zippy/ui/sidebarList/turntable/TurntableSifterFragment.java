package com.luza.zippy.ui.sidebarList.turntable;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.luza.zippy.R;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import android.util.Pair;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class TurntableSifterFragment extends BaseFragment {
    private TextView diceCountText;
    private RecyclerView recyclerDice;
    private MaterialButton rollButton;
    private MaterialButton increaseButton;
    private MaterialButton decreaseButton;
    private DiceAdapter diceAdapter;
    private int diceCount = 1;
    private static final int MAX_DICE = 6;
    private static final int MIN_DICE = 1;
    private Random random = new Random();
    private int animationCompletedCount = 0;  // 添加动画完成计数器
    private List<Integer> diceValues = new ArrayList<>();  // 当前骰子值
    private List<Integer> finalValues = new ArrayList<>();  // 最终骰子值
    private List<Boolean> isAnimating = new ArrayList<>();  // 动画状态

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_turntable_sifter, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_turntable_sifter);
    }

    @Override
    protected void initViews(View view) {
        diceCountText = view.findViewById(R.id.text_dice_count);
        recyclerDice = view.findViewById(R.id.recycler_dice);
        rollButton = view.findViewById(R.id.btn_roll);
        increaseButton = view.findViewById(R.id.btn_increase);
        decreaseButton = view.findViewById(R.id.btn_decrease);

        // 设置RecyclerView
        recyclerDice.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        diceAdapter = new DiceAdapter();
        recyclerDice.setAdapter(diceAdapter);

        // 初始化筛子数据
        updateDiceList();

        // 增加筛子数量
        increaseButton.setOnClickListener(v -> {
            if (diceCount < MAX_DICE) {
                diceCount++;
                updateDiceCount();
                updateDiceList();
            }
        });

        // 减少筛子数量
        decreaseButton.setOnClickListener(v -> {
            if (diceCount > MIN_DICE) {
                diceCount--;
                updateDiceCount();
                updateDiceList();
            }
        });

        // 投掷筛子
        rollButton.setOnClickListener(v -> rollDice());

        // 更新显示
        updateDiceCount();
    }

    // 更新筛子数量显示
    private void updateDiceCount() {
        diceCountText.setText(String.valueOf(diceCount));
        setButtonsEnabled(true);
    }

    // 更新筛子列表
    private void updateDiceList() {
        diceValues.clear();
        finalValues.clear();
        isAnimating.clear();
        for (int i = 0; i < diceCount; i++) {
            diceValues.add(1);  // 初始值为1
            finalValues.add(1);
            isAnimating.add(false);
        }
        diceAdapter.notifyDataSetChanged();
    }

    // 投掷筛子
    private void rollDice() {
        android.util.Log.d("DiceAnimation", "开始投掷骰子，当前骰子数量: " + diceCount);
        setButtonsEnabled(false);
        animationCompletedCount = 0;

        // 生成新的最终值
        finalValues.clear();
        for (int i = 0; i < diceCount; i++) {
            int finalValue = random.nextInt(6) + 1;
            finalValues.add(finalValue);
            android.util.Log.d("DiceAnimation", "骰子" + i + "的最终值: " + finalValue);
        }

        // 标记所有骰子开始动画
        for (int i = 0; i < diceCount; i++) {
            isAnimating.set(i, true);
        }

        // 确保RecyclerView完成布局后再开始动画
        android.util.Log.d("DiceAnimation", "等待RecyclerView布局完成...");
        recyclerDice.post(() -> {
            android.util.Log.d("DiceAnimation", "RecyclerView post执行");
            
            // 强制刷新所有骰子的显示
            diceAdapter.notifyDataSetChanged();
            
            // 等待布局完成后开始动画
            recyclerDice.postDelayed(() -> {
                android.util.Log.d("DiceAnimation", "开始所有骰子动画");
                
                // 先清除所有可能的旧动画
                for (int i = 0; i < diceCount; i++) {
                    RecyclerView.ViewHolder viewHolder = recyclerDice.findViewHolderForAdapterPosition(i);
                    if (viewHolder instanceof DiceAdapter.DiceViewHolder) {
                        DiceAdapter.DiceViewHolder holder = (DiceAdapter.DiceViewHolder) viewHolder;
                        Glide.with(requireContext()).clear(holder.gifImageView);
                    }
                }
                
                // 确保所有View都已经测量完成
                recyclerDice.post(() -> {
                    for (int i = 0; i < diceCount; i++) {
                        android.util.Log.d("DiceAnimation", "尝试开始骰子" + i + "的动画");
                        final int position = i;
                        recyclerDice.post(() -> startDiceAnimation(position));
                    }
                });
            }, 100);
        });
    }

    // 开始单个骰子的动画
    private void startDiceAnimation(int position) {
        RecyclerView.ViewHolder viewHolder = recyclerDice.findViewHolderForAdapterPosition(position);
        android.util.Log.d("DiceAnimation", "骰子" + position + " ViewHolder状态: " + (viewHolder != null ? "可见" : "不可见"));
        
        if (viewHolder instanceof DiceAdapter.DiceViewHolder) {
            DiceAdapter.DiceViewHolder holder = (DiceAdapter.DiceViewHolder) viewHolder;
            
            android.util.Log.d("DiceAnimation", "骰子" + position + "开始加载旋转动画");

            // 先清除之前的动画和缓存
            Glide.with(requireContext()).clear(holder.gifImageView);
            
            // 开始新的动画
            Glide.with(requireContext())
                .asGif()
                .load(R.drawable.sifter_rotate)  // 使用统一的旋转动画
                .skipMemoryCache(true)  // 禁用内存缓存
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)  // 禁用磁盘缓存
                .override(Target.SIZE_ORIGINAL)
                .listener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                        Target<GifDrawable> target, boolean isFirstResource) {
                        android.util.Log.e("DiceAnimation", "骰子" + position + "GIF加载失败", e);
                        handleAnimationComplete(position);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model,
                        Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        android.util.Log.d("DiceAnimation", "骰子" + position + "GIF资源加载成功");
                        
                        // 重置GIF动画
                        resource.setLoopCount(1);
                        resource.stop();
                        
                        // 使用Handler确保回调在主线程执行
                        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                        
                        // 设置一个超时机制，确保动画一定会结束
                        handler.postDelayed(() -> {
                            if (isAnimating.get(position)) {
                                android.util.Log.d("DiceAnimation", "骰子" + position + "动画超时，强制结束");
                                handleAnimationComplete(position);
                            }
                        }, 2000); // 2秒后强制结束
                        
                        resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                handler.post(() -> {
                                    android.util.Log.d("DiceAnimation", "骰子" + position + "动画结束");
                                    if (isAdded() && !isDetached() && isAnimating.get(position)) {
                                        handleAnimationComplete(position);
                                    }
                                });
                            }
                        });
                        
                        // 启动动画
                        handler.post(() -> {
                            if (isAdded() && !isDetached()) {
                                resource.start();
                            }
                        });
                        
                        return false;
                    }
                })
                .into(holder.gifImageView);
        } else {
            android.util.Log.d("DiceAnimation", "骰子" + position + "ViewHolder不可见，50ms后重试");
            recyclerDice.postDelayed(() -> {
                android.util.Log.d("DiceAnimation", "骰子" + position + "重试开始动画");
                startDiceAnimation(position);
            }, 50);
        }
    }

    // 处理动画完成
    private synchronized void handleAnimationComplete(int position) {
        if (!isAnimating.get(position)) {
            android.util.Log.d("DiceAnimation", "骰子" + position + "动画已经完成，忽略重复回调");
            return;
        }

        requireActivity().runOnUiThread(() -> {
            android.util.Log.d("DiceAnimation", "处理骰子" + position + "动画完成");
            // 更新状态
            isAnimating.set(position, false);
            diceValues.set(position, finalValues.get(position));
            
            // 检查是否所有动画都完成
            animationCompletedCount++;
            android.util.Log.d("DiceAnimation", "当前完成动画数: " + animationCompletedCount + "/" + diceCount);
            if (animationCompletedCount >= diceCount) {
                android.util.Log.d("DiceAnimation", "所有动画完成，启用按钮");
                setButtonsEnabled(true);
                
                // 所有动画完成后，对骰子进行排序
                sortDiceValues();
            }
        });
    }

    // 添加排序方法
    private void sortDiceValues() {
        // 创建索引和值的配对列表
        List<Pair<Integer, Integer>> indexedValues = new ArrayList<>();
        for (int i = 0; i < diceValues.size(); i++) {
            indexedValues.add(new Pair<>(i, diceValues.get(i)));
        }
        
        // 按值排序
        indexedValues.sort((a, b) -> a.second.compareTo(b.second));
        
        // 创建新的排序后的列表
        List<Integer> sortedValues = new ArrayList<>();
        List<Integer> sortedFinalValues = new ArrayList<>();
        for (Pair<Integer, Integer> pair : indexedValues) {
            sortedValues.add(diceValues.get(pair.first));
            sortedFinalValues.add(finalValues.get(pair.first));
        }
        
        // 更新数据
        diceValues.clear();
        diceValues.addAll(sortedValues);
        finalValues.clear();
        finalValues.addAll(sortedFinalValues);
        
        // 刷新显示
        diceAdapter.notifyDataSetChanged();
    }

    // 设置所有按钮的启用状态
    private void setButtonsEnabled(boolean enabled) {
        rollButton.setEnabled(enabled);
        if (enabled) {
            decreaseButton.setEnabled(diceCount > MIN_DICE);
            increaseButton.setEnabled(diceCount < MAX_DICE);
        } else {
            decreaseButton.setEnabled(false);
            increaseButton.setEnabled(false);
        }
    }

    // 筛子适配器
    private class DiceAdapter extends RecyclerView.Adapter<DiceAdapter.DiceViewHolder> {
        @NonNull
        @Override
        public DiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dice, parent, false);
            
            // 计算正方形大小
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int margin = (int) (16 * getResources().getDisplayMetrics().density);
            int spanCount = 3;
            int size = (screenWidth - (margin * 2) - (margin * (spanCount - 1))) / spanCount;
            
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            lp.width = size;
            lp.height = size;
            view.setLayoutParams(lp);
            
            return new DiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DiceViewHolder holder, int position) {
            android.util.Log.d("DiceAnimation", "绑定骰子" + position + "，动画状态: " + isAnimating.get(position));
            if (!isAnimating.get(position)) {
                showStaticImage(holder.gifImageView, diceValues.get(position));
                // 设置卡片背景颜色
                if (diceValues.get(position) == 1) {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.blue_200));
                    holder.gifImageView.setAlpha(0.5f);  // 设置图片透明度为50%
                } else {
                    holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.white));
                    holder.gifImageView.setAlpha(1.0f);  // 恢复图片完全不透明
                }
            } else {
                holder.gifImageView.setAlpha(1.0f);  // 动画时保持完全不透明
            }
        }

        @Override
        public int getItemCount() {
            return diceValues.size();
        }

        class DiceViewHolder extends RecyclerView.ViewHolder {
            final ImageView gifImageView;
            final androidx.cardview.widget.CardView cardView;

            DiceViewHolder(View itemView) {
                super(itemView);
                gifImageView = itemView.findViewById(R.id.gif_dice);
                cardView = (androidx.cardview.widget.CardView) itemView;
            }
        }
    }

    // 显示静态图片
    private void showStaticImage(ImageView imageView, int value) {
        int staticResource = 0;
        switch (value) {
            case 1: staticResource = R.drawable.sifter_static_1; break;
            case 2: staticResource = R.drawable.sifter_static_2; break;
            case 3: staticResource = R.drawable.sifter_static_3; break;
            case 4: staticResource = R.drawable.sifter_static_4; break;
            case 5: staticResource = R.drawable.sifter_static_5; break;
            case 6: staticResource = R.drawable.sifter_static_6; break;
        }
        
        Glide.with(requireContext())
            .load(staticResource)
            .fitCenter()
            .override(Target.SIZE_ORIGINAL)
            .into(imageView);
    }

    // 3D旋转动画类
    private static class Rotate3dAnimation extends Animation {
        private final float mFromDegrees;
        private final float mToDegrees;
        private final float mCenterX;
        private final float mCenterY;
        private final float mDepthZ;
        private final boolean mReverse;
        private android.graphics.Camera mCamera;

        public Rotate3dAnimation(float fromDegrees, float toDegrees,
                               float centerX, float centerY, float depthZ, boolean reverse) {
            mFromDegrees = fromDegrees;
            mToDegrees = toDegrees;
            mCenterX = centerX;
            mCenterY = centerY;
            mDepthZ = depthZ;
            mReverse = reverse;
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
            mCamera = new android.graphics.Camera();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
            final float fromDegrees = mFromDegrees;
            float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

            final float centerX = mCenterX;
            final float centerY = mCenterY;
            final android.graphics.Camera camera = mCamera;

            final android.graphics.Matrix matrix = t.getMatrix();

            camera.save();
            if (mReverse) {
                camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime);
            } else {
                camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime));
            }
            camera.rotateY(degrees); // 修改为rotateY，这样可以看到更好的3D效果
            camera.getMatrix(matrix);
            camera.restore();

            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }
}