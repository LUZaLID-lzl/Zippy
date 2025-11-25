package com.luza.zippy.ui.sidebarList.tetris;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.base.BaseFragment;

/**
 * 俄罗斯方块游戏Fragment
 */
public class TetrisFragment extends BaseFragment implements TetrisGameView.TetrisGameListener {

    private TetrisGameView gameView;
    private TextView tvScore, tvLevel, tvLines, tvHighScore;
    private TextView tvScoreDetail, tvLevelDetail, tvLinesDetail, tvHighScoreDetail;
    private NextPiecePreview nextPiecePreview;
    private Button btnNewGame, btnPause, btnMoveLeft, btnMoveRight, btnRotate, btnHardDrop;
    
    private ShardPerfenceSetting settings;
    private Handler gameHandler;
    private Runnable gameRunnable;
    private boolean isGameLoopRunning = false;
    
    // 游戏速度控制
    private static final int BASE_DROP_INTERVAL = 1000; // 基础下降间隔（毫秒）
    private int currentDropInterval = BASE_DROP_INTERVAL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tetris, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_tetris);
    }

    @Override
    protected void initViews(View view) {
        // 初始化设置
        settings = new ShardPerfenceSetting(getContext());
        
        // 初始化游戏视图
        gameView = view.findViewById(R.id.tetris_game_view);
        gameView.setGameListener(this);
        
        // 初始化UI组件
        initUIComponents(view);
        
        // 设置按钮监听器
        setupButtonListeners();
        
        // 初始化游戏循环
        setupGameLoop();
        
        // 更新UI显示
        updateUI();
        
        // 添加界面初始化动画
        startIntroAnimation(view);
    }
    
    private void initUIComponents(View view) {
        tvScore = view.findViewById(R.id.tv_score);
        tvLevel = view.findViewById(R.id.tv_level);
        tvLines = view.findViewById(R.id.tv_lines);
        tvHighScore = view.findViewById(R.id.tv_high_score);
        
        tvScoreDetail = view.findViewById(R.id.tv_score_detail);
        tvLevelDetail = view.findViewById(R.id.tv_level_detail);
        tvLinesDetail = view.findViewById(R.id.tv_lines_detail);
        tvHighScoreDetail = view.findViewById(R.id.tv_high_score_detail);
        
        nextPiecePreview = view.findViewById(R.id.next_piece_preview);
        
        btnNewGame = view.findViewById(R.id.btn_new_game);
        btnPause = view.findViewById(R.id.btn_pause);
        btnMoveLeft = view.findViewById(R.id.btn_move_left);
        btnMoveRight = view.findViewById(R.id.btn_move_right);
        btnRotate = view.findViewById(R.id.btn_rotate);
        btnHardDrop = view.findViewById(R.id.btn_hard_drop);
    }
    
    private void setupButtonListeners() {
        // 为所有按钮添加动画效果
        setupButtonWithAnimation(btnNewGame, v -> startNewGame());
        setupButtonWithAnimation(btnPause, v -> pauseGame());
        setupButtonWithAnimation(btnMoveLeft, v -> gameView.moveLeft());
        setupButtonWithAnimation(btnMoveRight, v -> gameView.moveRight());
        setupButtonWithAnimation(btnRotate, v -> gameView.rotate());
        setupButtonWithAnimation(btnHardDrop, v -> gameView.hardDrop());
    }
    
    private void setupButtonWithAnimation(Button button, View.OnClickListener action) {
        button.setOnClickListener(v -> {
            // 播放按钮点击动画
            Animation pressAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_press);
            Animation releaseAnim = AnimationUtils.loadAnimation(getContext(), R.anim.button_release);
            
            v.startAnimation(pressAnim);
            
            // 延迟执行动作和释放动画
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                action.onClick(v);
                v.startAnimation(releaseAnim);
            }, 100);
        });
    }
    
    private void setupGameLoop() {
        gameHandler = new Handler(Looper.getMainLooper());
        gameRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameView.isGameRunning() && !gameView.isGamePaused()) {
                    gameView.softDrop();
                    
                    // 更新下一个方块预览
                    updateNextPiecePreview();
                    
                    // 根据等级调整下降速度
                    currentDropInterval = Math.max(100, BASE_DROP_INTERVAL - (gameView.getLevel() - 1) * 100);
                    gameHandler.postDelayed(this, currentDropInterval);
                }
            }
        };
    }
    
    private void startNewGame() {
        if (gameView.isGameRunning()) {
            // 如果游戏正在进行，询问是否重新开始
            new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.tetris_new_game))
                .setMessage("确定要开始新游戏吗？当前进度将丢失。")
                .setPositiveButton(getString(R.string.confirm), (dialog, which) -> {
                    startGame();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
        } else {
            startGame();
        }
    }
    
    private void startGame() {
        gameView.startGame();
        isGameLoopRunning = true;
        currentDropInterval = BASE_DROP_INTERVAL;
        gameHandler.post(gameRunnable);
        
        // 更新按钮状态
        btnPause.setText(getString(R.string.tetris_pause));
        btnPause.setEnabled(true);
        
        updateUI();
    }
    
    private void pauseGame() {
        if (gameView.isGameRunning()) {
            gameView.pauseGame();
            
            if (gameView.isGamePaused()) {
                btnPause.setText(getString(R.string.tetris_resume));
                gameHandler.removeCallbacks(gameRunnable);
            } else {
                btnPause.setText(getString(R.string.tetris_pause));
                gameHandler.post(gameRunnable);
            }
        }
    }
    
    private void updateUI() {
        if (getContext() == null) return;
        
        // 更新分数显示
        tvScore.setText(getString(R.string.tetris_score) + ": " + gameView.getScore());
        tvLevel.setText(getString(R.string.tetris_level) + ": " + gameView.getLevel());
        tvLines.setText(getString(R.string.tetris_lines) + ": " + gameView.getLinesCleared());
        tvHighScore.setText(getString(R.string.tetris_high_score) + ": " + settings.getTetrisHighScore());
        
        // 更新详细信息显示
        tvScoreDetail.setText("分数: " + gameView.getScore());
        tvLevelDetail.setText("等级: " + gameView.getLevel());
        tvLinesDetail.setText("行数: " + gameView.getLinesCleared());
        tvHighScoreDetail.setText("最高分: " + settings.getTetrisHighScore());
        
        // 更新下一个方块预览
        updateNextPiecePreview();
    }
    
    private void updateNextPiecePreview() {
        if (nextPiecePreview != null && gameView.getNextPiece() != null) {
            nextPiecePreview.setNextPiece(gameView.getNextPiece().type);
        }
    }
    
    // TetrisGameListener 接口实现
    @Override
    public void onScoreChanged(int score) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvScore.setText(getString(R.string.tetris_score) + ": " + score);
                tvScoreDetail.setText("分数: " + score);
                // 添加分数更新动画
                Animation pulseAnim = AnimationUtils.loadAnimation(getContext(), R.anim.score_pulse);
                tvScore.startAnimation(pulseAnim);
                tvScoreDetail.startAnimation(pulseAnim);
            });
        }
    }
    
    @Override
    public void onLevelChanged(int level) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvLevel.setText(getString(R.string.tetris_level) + ": " + level);
                tvLevelDetail.setText("等级: " + level);
                // 添加等级更新动画
                Animation pulseAnim = AnimationUtils.loadAnimation(getContext(), R.anim.score_pulse);
                tvLevel.startAnimation(pulseAnim);
                tvLevelDetail.startAnimation(pulseAnim);
            });
        }
    }
    
    @Override
    public void onLinesChanged(int lines) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                tvLines.setText(getString(R.string.tetris_lines) + ": " + lines);
                tvLinesDetail.setText("行数: " + lines);
                // 添加行数更新动画
                Animation pulseAnim = AnimationUtils.loadAnimation(getContext(), R.anim.score_pulse);
                tvLines.startAnimation(pulseAnim);
                tvLinesDetail.startAnimation(pulseAnim);
            });
        }
    }
    
    @Override
    public void onGameOver() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                isGameLoopRunning = false;
                gameHandler.removeCallbacks(gameRunnable);
                
                int currentScore = gameView.getScore();
                int highScore = settings.getTetrisHighScore();
                
                // 检查是否创造新纪录
                boolean isNewHighScore = currentScore > highScore;
                if (isNewHighScore) {
                    settings.setTetrisHighScore(currentScore);
                    updateUI();
                }
                
                // 显示游戏结束对话框
                showGameOverDialog(currentScore, isNewHighScore);
                
                // 更新按钮状态
                btnPause.setText(getString(R.string.tetris_pause));
                btnPause.setEnabled(false);
            });
        }
    }
    
    @Override
    public void onNewHighScore(int score) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                settings.setTetrisHighScore(score);
                updateUI();
                Toast.makeText(getContext(), getString(R.string.tetris_new_high_score), Toast.LENGTH_LONG).show();
            });
        }
    }
    
    private void showGameOverDialog(int finalScore, boolean isNewHighScore) {
        String title = getString(R.string.tetris_game_over);
        String message = "最终得分: " + finalScore + "\n" +
                        "等级: " + gameView.getLevel() + "\n" +
                        "消除行数: " + gameView.getLinesCleared();
        
        if (isNewHighScore) {
            title = getString(R.string.tetris_new_high_score);
            message = "🎉 " + message + "\n\n恭喜创造新纪录！";
        }
        
        new AlertDialog.Builder(getContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.tetris_new_game), (dialog, which) -> startGame())
            .setNegativeButton(getString(R.string.cancel), null)
            .setCancelable(false)
            .show();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // 暂停游戏循环
        if (isGameLoopRunning) {
            gameHandler.removeCallbacks(gameRunnable);
        }
        
        // 如果游戏正在运行且未暂停，则暂停游戏
        if (gameView != null && gameView.isGameRunning() && !gameView.isGamePaused()) {
            gameView.pauseGame();
            if (btnPause != null) {
                btnPause.setText(getString(R.string.tetris_resume));
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 如果游戏正在运行且已暂停，恢复游戏循环
        if (gameView != null && gameView.isGameRunning() && gameView.isGamePaused()) {
            // 不自动恢复，让用户手动恢复
        } else if (gameView != null && gameView.isGameRunning() && !gameView.isGamePaused()) {
            // 恢复游戏循环
            isGameLoopRunning = true;
            gameHandler.post(gameRunnable);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
        if (gameHandler != null) {
            gameHandler.removeCallbacks(gameRunnable);
        }
        isGameLoopRunning = false;
    }
    
    private void startIntroAnimation(View view) {
        // 为游戏区域添加淡入动画
        Animation fadeInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.game_fade_in);
        gameView.startAnimation(fadeInAnim);
        
        // 为控制按钮区域添加延迟动画
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 直接查找控制按钮容器
            View buttonsContainer = view.findViewById(R.id.btn_new_game);
            if (buttonsContainer != null && buttonsContainer.getParent() instanceof ViewGroup) {
                ViewGroup parentContainer = (ViewGroup) buttonsContainer.getParent();
                if (parentContainer.getParent() instanceof ViewGroup) {
                    View controlsLayout = (View) parentContainer.getParent();
                    Animation slideUpAnim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                    controlsLayout.startAnimation(slideUpAnim);
                }
            }
        }, 200);
    }
}