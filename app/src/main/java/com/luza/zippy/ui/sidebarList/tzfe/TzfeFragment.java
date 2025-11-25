package com.luza.zippy.ui.sidebarList.tzfe;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.luza.zippy.R;
import com.luza.zippy.setting.ShardPerfenceSetting;
import com.luza.zippy.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 2048游戏Fragment
 */
public class TzfeFragment extends BaseFragment {

    private GridLayout gameGrid;
    private TextView tvCurrentScore;
    private TextView tvHighScore;
    private TextView tvGameStatus;
    private androidx.cardview.widget.CardView cardGameStatus;
    private MaterialButton btnNewGame;
    private MaterialButton btnUndo;
    
    private int[][] gameBoard = new int[4][4];
    private int[][] previousBoard = new int[4][4];
    private int currentScore = 0;
    private int previousScore = 0;
    private boolean gameWon = false;
    private boolean gameOver = false;
    
    private ShardPerfenceSetting shardPerfenceSetting;
    private GestureDetector gestureDetector;
    private Random random = new Random();
    
    // 数字颜色配置
    private static final int[] TILE_COLORS = {
        Color.parseColor("#cdc1b4"), // 0 - 空格
        Color.parseColor("#eee4da"), // 2
        Color.parseColor("#ede0c8"), // 4
        Color.parseColor("#f2b179"), // 8
        Color.parseColor("#f59563"), // 16
        Color.parseColor("#f67c5f"), // 32
        Color.parseColor("#f65e3b"), // 64
        Color.parseColor("#edcf72"), // 128
        Color.parseColor("#edcc61"), // 256
        Color.parseColor("#edc850"), // 512
        Color.parseColor("#edc53f"), // 1024
        Color.parseColor("#edc22e"), // 2048
    };
    
    private static final int[] TEXT_COLORS = {
        Color.parseColor("#776e65"), // 0 - 空格
        Color.parseColor("#776e65"), // 2
        Color.parseColor("#776e65"), // 4
        Color.parseColor("#f9f6f2"), // 8
        Color.parseColor("#f9f6f2"), // 16
        Color.parseColor("#f9f6f2"), // 32
        Color.parseColor("#f9f6f2"), // 64
        Color.parseColor("#f9f6f2"), // 128
        Color.parseColor("#f9f6f2"), // 256
        Color.parseColor("#f9f6f2"), // 512
        Color.parseColor("#f9f6f2"), // 1024
        Color.parseColor("#f9f6f2"), // 2048
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tzfe, container, false);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.menu_tzfe);
    }

    @Override
    protected void initViews(View view) {
        shardPerfenceSetting = ShardPerfenceSetting.getInstance(getContext());
        
        gameGrid = view.findViewById(R.id.game_grid);
        tvCurrentScore = view.findViewById(R.id.tv_current_score);
        tvHighScore = view.findViewById(R.id.tv_high_score);
        tvGameStatus = view.findViewById(R.id.tv_game_status);
        cardGameStatus = view.findViewById(R.id.card_game_status);
        btnNewGame = view.findViewById(R.id.btn_new_game);
        btnUndo = view.findViewById(R.id.btn_undo);
        
        setupGestureDetector();
        setupButtons();
        initializeGame();
        updateHighScore();
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (gameOver || e1 == null || e2 == null) return false;
                
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                android.util.Log.d("TzfeFragment", "Fling detected: deltaX=" + deltaX + ", deltaY=" + deltaY);
                
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // 水平滑动
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (deltaX > 0) {
                            android.util.Log.d("TzfeFragment", "Move Right");
                            moveRight();
                        } else {
                            android.util.Log.d("TzfeFragment", "Move Left");
                            moveLeft();
                        }
                        return true;
                    }
                } else {
                    // 垂直滑动
                    if (Math.abs(deltaY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (deltaY > 0) {
                            android.util.Log.d("TzfeFragment", "Move Down");
                            moveDown();
                        } else {
                            android.util.Log.d("TzfeFragment", "Move Up");
                            moveUp();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        
        // 设置触摸监听器到游戏网格
        gameGrid.setOnTouchListener((v, event) -> {
            android.util.Log.d("TzfeFragment", "Touch event: " + event.getAction());
            return gestureDetector.onTouchEvent(event);
        });
        
        // 确保游戏网格可以接收触摸事件
        gameGrid.setClickable(true);
        gameGrid.setFocusable(true);
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private void setupButtons() {
        btnNewGame.setOnClickListener(v -> {
            initializeGame();
            cardGameStatus.setVisibility(View.GONE);
        });
        
        btnUndo.setOnClickListener(v -> undoMove());
    }
    
    private void initializeGame() {
        gameBoard = new int[4][4];
        currentScore = 0;
        gameWon = false;
        gameOver = false;
        
        addRandomTile();
        addRandomTile();
        
        updateUI();
        createGameGrid();
    }
    
    private void createGameGrid() {
        gameGrid.removeAllViews();
        
        // 计算每个格子的大小（正方形）
        gameGrid.post(() -> {
            int gridWidth = gameGrid.getWidth();
            int gridHeight = gameGrid.getHeight();
            int size = Math.min(gridWidth, gridHeight);
            int cellSize = (size - 48) / 4; // 减去边距
            
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    CardView cardView = new CardView(getContext());
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = cellSize;
                    params.height = cellSize;
                    params.rowSpec = GridLayout.spec(i);
                    params.columnSpec = GridLayout.spec(j);
                    params.setMargins(4, 4, 4, 4);
                    cardView.setLayoutParams(params);
                    cardView.setCardElevation(4);
                    cardView.setRadius(8);
                    
                    TextView textView = new TextView(getContext());
                    textView.setGravity(android.view.Gravity.CENTER);
                    textView.setTextSize(18);
                    textView.setTypeface(null, android.graphics.Typeface.BOLD);
                    
                    cardView.addView(textView);
                    gameGrid.addView(cardView);
                    
                    updateTile(i, j);
                }
            }
            
            // 重新设置触摸监听器
            gameGrid.setOnTouchListener((v, event) -> {
                android.util.Log.d("TzfeFragment", "Touch event: " + event.getAction());
                return gestureDetector.onTouchEvent(event);
            });
            gameGrid.setClickable(true);
            gameGrid.setFocusable(true);
        });
    }
    
    private void updateTile(int row, int col) {
        int index = row * 4 + col;
        CardView cardView = (CardView) gameGrid.getChildAt(index);
        TextView textView = (TextView) cardView.getChildAt(0);
        
        int value = gameBoard[row][col];
        boolean isNewTile = false;
        
        // 检查是否是新方块（之前为0，现在不为0）
        if (previousBoard != null && previousBoard[row][col] == 0 && value != 0) {
            isNewTile = true;
        }
        
        if (value == 0) {
            textView.setText("");
            cardView.setCardBackgroundColor(TILE_COLORS[0]);
            textView.setTextColor(TEXT_COLORS[0]);
        } else {
            textView.setText(String.valueOf(value));
            
            int colorIndex = getColorIndex(value);
            cardView.setCardBackgroundColor(TILE_COLORS[colorIndex]);
            textView.setTextColor(TEXT_COLORS[colorIndex]);
            
            // 为新方块添加出现动画
            if (isNewTile) {
                animateTile(cardView);
            }
            // 为合并的方块添加合并动画
            else if (previousBoard != null && previousBoard[row][col] != 0 && 
                     previousBoard[row][col] != value && value > previousBoard[row][col]) {
                animateMerge(cardView);
            }
        }
    }
    
    private int getColorIndex(int value) {
        int index = 0;
        int temp = value;
        while (temp > 2) {
            temp /= 2;
            index++;
        }
        return Math.min(index + 1, TILE_COLORS.length - 1);
    }
    
    private void animateTile(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(150);
        animatorSet.start();
    }
    
    private void animateMove(View view, float fromX, float toX, float fromY, float toY) {
        ObjectAnimator moveX = ObjectAnimator.ofFloat(view, "translationX", fromX, toX);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(view, "translationY", fromY, toY);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animatorSet.start();
    }
    
    private void animateMerge(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new android.view.animation.OvershootInterpolator());
        animatorSet.start();
    }
    
    private void animateSlideDirection(String direction) {
        // 为整个游戏网格添加轻微的滑动反馈动画
        float translationX = 0, translationY = 0;
        switch (direction) {
            case "left":
                translationX = -10f;
                break;
            case "right":
                translationX = 10f;
                break;
            case "up":
                translationY = -10f;
                break;
            case "down":
                translationY = 10f;
                break;
        }
        
        ObjectAnimator moveX = ObjectAnimator.ofFloat(gameGrid, "translationX", 0, translationX, 0);
        ObjectAnimator moveY = ObjectAnimator.ofFloat(gameGrid, "translationY", 0, translationY, 0);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(moveX, moveY);
        animatorSet.setDuration(150);
        animatorSet.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animatorSet.start();
    }
    
    private void addRandomTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (gameBoard[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        
        if (!emptyCells.isEmpty()) {
            int[] randomCell = emptyCells.get(random.nextInt(emptyCells.size()));
            gameBoard[randomCell[0]][randomCell[1]] = random.nextFloat() < 0.9f ? 2 : 4;
        }
    }
    
    private void saveState() {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(gameBoard[i], 0, previousBoard[i], 0, 4);
        }
        previousScore = currentScore;
    }
    
    private void undoMove() {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(previousBoard[i], 0, gameBoard[i], 0, 4);
        }
        currentScore = previousScore;
        updateUI();
        updateAllTiles();
    }
    
    private void moveLeft() {
        if (gameOver) return;
        
        animateSlideDirection("left");
        
        saveState();
        boolean moved = false;
        
        for (int i = 0; i < 4; i++) {
            int[] row = new int[4];
            System.arraycopy(gameBoard[i], 0, row, 0, 4);
            
            int[] newRow = moveAndMergeArray(row);
            if (!java.util.Arrays.equals(gameBoard[i], newRow)) {
                moved = true;
                System.arraycopy(newRow, 0, gameBoard[i], 0, 4);
            }
        }
        
        if (moved) {
            addRandomTile();
            updateUI();
            updateAllTiles();
            checkGameState();
        }
    }
    
    private void moveRight() {
        if (gameOver) return;
        
        animateSlideDirection("right");
        
        saveState();
        boolean moved = false;
        
        for (int i = 0; i < 4; i++) {
            int[] row = new int[4];
            for (int j = 0; j < 4; j++) {
                row[j] = gameBoard[i][3 - j];
            }
            
            int[] newRow = moveAndMergeArray(row);
            boolean rowChanged = false;
            for (int j = 0; j < 4; j++) {
                if (gameBoard[i][3 - j] != newRow[j]) {
                    rowChanged = true;
                    gameBoard[i][3 - j] = newRow[j];
                }
            }
            if (rowChanged) moved = true;
        }
        
        if (moved) {
            addRandomTile();
            updateUI();
            updateAllTiles();
            checkGameState();
        }
    }
    
    private void moveUp() {
        if (gameOver) return;
        
        animateSlideDirection("up");
        
        saveState();
        boolean moved = false;
        
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) {
                column[i] = gameBoard[i][j];
            }
            
            int[] newColumn = moveAndMergeArray(column);
            boolean columnChanged = false;
            for (int i = 0; i < 4; i++) {
                if (gameBoard[i][j] != newColumn[i]) {
                    columnChanged = true;
                    gameBoard[i][j] = newColumn[i];
                }
            }
            if (columnChanged) moved = true;
        }
        
        if (moved) {
            addRandomTile();
            updateUI();
            updateAllTiles();
            checkGameState();
        }
    }
    
    private void moveDown() {
        if (gameOver) return;
        
        animateSlideDirection("down");
        
        saveState();
        boolean moved = false;
        
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) {
                column[i] = gameBoard[3 - i][j];
            }
            
            int[] newColumn = moveAndMergeArray(column);
            boolean columnChanged = false;
            for (int i = 0; i < 4; i++) {
                if (gameBoard[3 - i][j] != newColumn[i]) {
                    columnChanged = true;
                    gameBoard[3 - i][j] = newColumn[i];
                }
            }
            if (columnChanged) moved = true;
        }
        
        if (moved) {
            addRandomTile();
            updateUI();
            updateAllTiles();
            checkGameState();
        }
    }
    
    private int[] moveAndMergeArray(int[] array) {
        int[] result = new int[4];
        int index = 0;
        
        // 移动非零元素
        for (int value : array) {
            if (value != 0) {
                result[index++] = value;
            }
        }
        
        // 合并相同元素
        for (int i = 0; i < 3; i++) {
            if (result[i] != 0 && result[i] == result[i + 1]) {
                result[i] *= 2;
                currentScore += result[i];
                result[i + 1] = 0;
            }
        }
        
        // 再次移动
        int[] finalResult = new int[4];
        index = 0;
        for (int value : result) {
            if (value != 0) {
                finalResult[index++] = value;
            }
        }
        
        return finalResult;
    }
    
    private void updateUI() {
        tvCurrentScore.setText(String.valueOf(currentScore));
        
        // 更新最高分
        int highScore = shardPerfenceSetting.getGame2048HighScore();
        if (currentScore > highScore) {
            shardPerfenceSetting.setGame2048HighScore(currentScore);
            highScore = currentScore;
        }
        tvHighScore.setText(String.valueOf(highScore));
    }
    
    private void updateHighScore() {
        int highScore = shardPerfenceSetting.getGame2048HighScore();
        tvHighScore.setText(String.valueOf(highScore));
    }
    
    private void updateAllTiles() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                updateTile(i, j);
            }
        }
    }
    
    private void checkGameState() {
        // 检查是否获胜
        if (!gameWon) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (gameBoard[i][j] == 2048) {
                        gameWon = true;
                        showGameStatus(getString(R.string.game_2048_win_message));
                        return;
                    }
                }
            }
        }
        
        // 检查是否游戏结束
        if (isGameOver()) {
            gameOver = true;
            showGameStatus(getString(R.string.game_2048_game_over));
        }
    }
    
    private boolean isGameOver() {
        // 检查是否有空格
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (gameBoard[i][j] == 0) {
                    return false;
                }
            }
        }
        
        // 检查是否可以合并
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameBoard[i][j] == gameBoard[i][j + 1]) {
                    return false;
                }
            }
        }
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (gameBoard[i][j] == gameBoard[i + 1][j]) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void showGameStatus(String message) {
        tvGameStatus.setText(message);
        cardGameStatus.setVisibility(View.VISIBLE);
        
        // 添加动画效果
        ObjectAnimator alpha = ObjectAnimator.ofFloat(cardGameStatus, "alpha", 0f, 1f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(cardGameStatus, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(cardGameStatus, "scaleY", 0.8f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(alpha, scaleX, scaleY);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new android.view.animation.OvershootInterpolator());
        animatorSet.start();
    }
}