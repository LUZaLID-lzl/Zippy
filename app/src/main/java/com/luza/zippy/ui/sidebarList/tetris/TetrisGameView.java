package com.luza.zippy.ui.sidebarList.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.luza.zippy.R;

public class TetrisGameView extends View {
    
    // 游戏区域尺寸
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    
    // 方块类型
    private static final int EMPTY = 0;
    private static final int I_PIECE = 1;
    private static final int O_PIECE = 2;
    private static final int T_PIECE = 3;
    private static final int S_PIECE = 4;
    private static final int Z_PIECE = 5;
    private static final int J_PIECE = 6;
    private static final int L_PIECE = 7;
    
    // 游戏状态
    private boolean isGameRunning = false;
    private boolean isGamePaused = false;
    private int score = 0;
    private int level = 1;
    private int linesCleared = 0;
    
    // 游戏板
    private int[][] board = new int[BOARD_HEIGHT][BOARD_WIDTH];
    
    // 当前方块
    private TetrisPiece currentPiece;
    private TetrisPiece nextPiece;
    
    // 绘制相关
    private Paint paint;
    private Paint borderPaint;
    private Paint textPaint;
    private int cellSize;
    private int boardStartX;
    private int boardStartY;
    
    // 颜色数组
    private int[] pieceColors;
    
    // 方块形状定义
    private int[][][][] pieceShapes = {
        {}, // EMPTY
        { // I_PIECE
            {{1,1,1,1}},
            {{1},{1},{1},{1}},
            {{1,1,1,1}},
            {{1},{1},{1},{1}}
        },
        { // O_PIECE
            {{1,1},{1,1}},
            {{1,1},{1,1}},
            {{1,1},{1,1}},
            {{1,1},{1,1}}
        },
        { // T_PIECE
            {{0,1,0},{1,1,1}},
            {{1,0},{1,1},{1,0}},
            {{1,1,1},{0,1,0}},
            {{0,1},{1,1},{0,1}}
        },
        { // S_PIECE
            {{0,1,1},{1,1,0}},
            {{1,0},{1,1},{0,1}},
            {{0,1,1},{1,1,0}},
            {{1,0},{1,1},{0,1}}
        },
        { // Z_PIECE
            {{1,1,0},{0,1,1}},
            {{0,1},{1,1},{1,0}},
            {{1,1,0},{0,1,1}},
            {{0,1},{1,1},{1,0}}
        },
        { // J_PIECE
            {{1,0,0},{1,1,1}},
            {{1,1},{1,0},{1,0}},
            {{1,1,1},{0,0,1}},
            {{0,1},{0,1},{1,1}}
        },
        { // L_PIECE
            {{0,0,1},{1,1,1}},
            {{1,0},{1,0},{1,1}},
            {{1,1,1},{1,0,0}},
            {{1,1},{0,1},{0,1}}
        }
    };
    
    private Random random = new Random();
    private TetrisGameListener gameListener;
    
    // 动画相关
    private boolean isAnimating = false;
    private List<Integer> animatingLines = new ArrayList<>();
    private float animationProgress = 0f;
    private ValueAnimator lineAnimator;
    
    public interface TetrisGameListener {
        void onScoreChanged(int score);
        void onLevelChanged(int level);
        void onLinesChanged(int lines);
        void onGameOver();
        void onNewHighScore(int score);
    }
    
    public TetrisGameView(Context context) {
        super(context);
        init();
    }
    
    public TetrisGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setAntiAlias(true);
        
        initializeBoard();
        generateNewPiece();
        generateNextPiece();
        initColors();
    }
    
    private void initializeBoard() {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[i][j] = EMPTY;
            }
        }
    }
    
    private void generateNewPiece() {
        if (nextPiece != null) {
            currentPiece = nextPiece;
        } else {
            currentPiece = new TetrisPiece(getRandomPieceType());
        }
        generateNextPiece();
        
        // 检查游戏是否结束
        if (isCollision(currentPiece, currentPiece.x, currentPiece.y)) {
            gameOver();
        }
    }
    
    private void generateNextPiece() {
        nextPiece = new TetrisPiece(getRandomPieceType());
    }
    
    private int getRandomPieceType() {
        return random.nextInt(7) + 1; // 1-7
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // 计算单元格大小和游戏板位置 - 使用更多可用空间
        int padding = 20; // 减少边距
        int availableWidth = w - padding * 2;
        int availableHeight = h - padding * 2;
        
        // 让方块更大
        cellSize = Math.min(availableWidth / BOARD_WIDTH, availableHeight / BOARD_HEIGHT);
        
        // 居中显示游戏板
        int boardWidth = BOARD_WIDTH * cellSize;
        int boardHeight = BOARD_HEIGHT * cellSize;
        
        boardStartX = (w - boardWidth) / 2;
        boardStartY = (h - boardHeight) / 2;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制游戏区域边框
        drawGameBorder(canvas);
        
        drawBoard(canvas);
        
        // 绘制方块虚影（在当前方块之前绘制）
        drawPieceGhost(canvas);
        
        drawCurrentPiece(canvas);
        
        if (isGamePaused) {
            drawPauseOverlay(canvas);
        }
    }
    
    private void drawGameBorder(Canvas canvas) {
        // 计算边框区域
        int borderWidth = 8;
        int outerBorderWidth = 12;
        
        // 绘制外层阴影
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(boardStartX - outerBorderWidth + 4, 
                       boardStartY - outerBorderWidth + 4, 
                       boardStartX + BOARD_WIDTH * cellSize + outerBorderWidth + 4, 
                       boardStartY + BOARD_HEIGHT * cellSize + outerBorderWidth + 4, 
                       shadowPaint);
        
        // 绘制外层边框（深色）
        Paint outerBorderPaint = new Paint();
        outerBorderPaint.setColor(Color.argb(200, 100, 100, 100));
        canvas.drawRect(boardStartX - outerBorderWidth, 
                       boardStartY - outerBorderWidth, 
                       boardStartX + BOARD_WIDTH * cellSize + outerBorderWidth, 
                       boardStartY + BOARD_HEIGHT * cellSize + outerBorderWidth, 
                       outerBorderPaint);
        
        // 绘制中层边框（渐变效果）
        Paint midBorderPaint = new Paint();
        midBorderPaint.setColor(Color.argb(150, 200, 200, 200));
        canvas.drawRect(boardStartX - borderWidth, 
                       boardStartY - borderWidth, 
                       boardStartX + BOARD_WIDTH * cellSize + borderWidth, 
                       boardStartY + BOARD_HEIGHT * cellSize + borderWidth, 
                       midBorderPaint);
        
        // 绘制内层边框（亮色高光）
        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(Color.argb(180, 255, 255, 255));
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(3);
        canvas.drawRect(boardStartX - 2, 
                       boardStartY - 2, 
                       boardStartX + BOARD_WIDTH * cellSize + 2, 
                       boardStartY + BOARD_HEIGHT * cellSize + 2, 
                       innerBorderPaint);
        
        // 绘制装饰性角落
        drawCornerDecorations(canvas);
        
        // 绘制游戏区域背景
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(30, 255, 255, 255));
        canvas.drawRect(boardStartX, boardStartY, 
                       boardStartX + BOARD_WIDTH * cellSize, 
                       boardStartY + BOARD_HEIGHT * cellSize, 
                       bgPaint);
    }
    
    private void drawCornerDecorations(Canvas canvas) {
        Paint decorPaint = new Paint();
        decorPaint.setColor(Color.argb(150, 255, 255, 255));
        decorPaint.setStrokeWidth(4);
        decorPaint.setStrokeCap(Paint.Cap.ROUND);
        
        int cornerSize = 20;
        int offset = 15;
        
        // 左上角
        canvas.drawLine(boardStartX - offset, boardStartY - offset + cornerSize, 
                       boardStartX - offset, boardStartY - offset, decorPaint);
        canvas.drawLine(boardStartX - offset, boardStartY - offset, 
                       boardStartX - offset + cornerSize, boardStartY - offset, decorPaint);
        
        // 右上角
        int rightX = boardStartX + BOARD_WIDTH * cellSize;
        canvas.drawLine(rightX + offset - cornerSize, boardStartY - offset, 
                       rightX + offset, boardStartY - offset, decorPaint);
        canvas.drawLine(rightX + offset, boardStartY - offset, 
                       rightX + offset, boardStartY - offset + cornerSize, decorPaint);
        
        // 左下角
        int bottomY = boardStartY + BOARD_HEIGHT * cellSize;
        canvas.drawLine(boardStartX - offset, bottomY + offset - cornerSize, 
                       boardStartX - offset, bottomY + offset, decorPaint);
        canvas.drawLine(boardStartX - offset, bottomY + offset, 
                       boardStartX - offset + cornerSize, bottomY + offset, decorPaint);
        
        // 右下角
        canvas.drawLine(rightX + offset - cornerSize, bottomY + offset, 
                       rightX + offset, bottomY + offset, decorPaint);
        canvas.drawLine(rightX + offset, bottomY + offset, 
                       rightX + offset, bottomY + offset - cornerSize, decorPaint);
    }
    
    private void drawBoard(Canvas canvas) {
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                int x = boardStartX + j * cellSize;
                int y = boardStartY + i * cellSize;
                
                if (board[i][j] != EMPTY) {
                    // 检查是否是正在动画的行
                    if (isAnimating && animatingLines.contains(i)) {
                        drawAnimatingBlock(canvas, x, y, pieceColors[board[i][j]], i);
                    } else {
                        drawBlock(canvas, x, y, pieceColors[board[i][j]]);
                    }
                } else {
                    // 绘制空白格子的精美网格
                    drawEmptyCell(canvas, x, y);
                }
            }
        }
    }
    
    private void drawAnimatingBlock(Canvas canvas, int x, int y, int color, int lineIndex) {
        // 计算动画效果
        float progress = animationProgress;
        
        // 闪烁效果 - 前半段时间
        if (progress < 0.6f) {
            float flashProgress = progress / 0.6f;
            int flashCount = 3;
            float flashPhase = (flashProgress * flashCount) % 1.0f;
            
            if (flashPhase < 0.5f) {
                // 闪烁为白色
                Paint flashPaint = new Paint();
                flashPaint.setColor(Color.WHITE);
                canvas.drawRect(x, y, x + cellSize, y + cellSize, flashPaint);
                
                // 绘制闪烁边框
                Paint flashBorderPaint = new Paint();
                flashBorderPaint.setColor(Color.YELLOW);
                flashBorderPaint.setStyle(Paint.Style.STROKE);
                flashBorderPaint.setStrokeWidth(4);
                canvas.drawRect(x, y, x + cellSize, y + cellSize, flashBorderPaint);
            } else {
                // 正常颜色
                drawBlock(canvas, x, y, color);
            }
        } else {
            // 淡出效果 - 后半段时间
            float fadeProgress = (progress - 0.6f) / 0.4f;
            int alpha = (int)(255 * (1 - fadeProgress));
            
            // 绘制淡出的方块
            Paint fadePaint = new Paint();
            fadePaint.setAntiAlias(true);
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            fadePaint.setColor(Color.argb(alpha, red, green, blue));
            
            canvas.drawRect(x, y, x + cellSize, y + cellSize, fadePaint);
            
            // 添加粒子效果
            drawParticleEffect(canvas, x, y, fadeProgress, color);
        }
    }
    
    private void drawParticleEffect(Canvas canvas, int x, int y, float progress, int color) {
        Paint particlePaint = new Paint();
        particlePaint.setAntiAlias(true);
        
        int particleCount = 8;
        for (int i = 0; i < particleCount; i++) {
            float angle = (float)(2 * Math.PI * i / particleCount);
            float distance = progress * cellSize * 0.8f;
            
            float particleX = x + cellSize / 2f + (float)Math.cos(angle) * distance;
            float particleY = y + cellSize / 2f + (float)Math.sin(angle) * distance;
            
            int alpha = (int)(255 * (1 - progress));
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            particlePaint.setColor(Color.argb(alpha, red, green, blue));
            
            float radius = 3 * (1 - progress);
            canvas.drawCircle(particleX, particleY, radius, particlePaint);
        }
    }
    
    private void drawEmptyCell(Canvas canvas, int x, int y) {
        // 绘制空白格子的微妙背景
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.argb(15, 255, 255, 255));
        canvas.drawRect(x, y, x + cellSize, y + cellSize, bgPaint);
        
        // 绘制精美的网格线
        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.argb(40, 255, 255, 255));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(0.5f);
        gridPaint.setAntiAlias(true);
        
        // 绘制外边框
        canvas.drawRect(x, y, x + cellSize, y + cellSize, gridPaint);
        
        // 绘制内部装饰点（每隔一定距离）
        if ((x / cellSize + y / cellSize) % 3 == 0) {
            Paint dotPaint = new Paint();
            dotPaint.setColor(Color.argb(25, 255, 255, 255));
            dotPaint.setAntiAlias(true);
            float centerX = x + cellSize / 2f;
            float centerY = y + cellSize / 2f;
            canvas.drawCircle(centerX, centerY, 1.5f, dotPaint);
        }
    }
    
    private void drawCurrentPiece(Canvas canvas) {
        if (currentPiece != null) {
            int[][] shape = pieceShapes[currentPiece.type][currentPiece.rotation];
            
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int x = boardStartX + (currentPiece.x + j) * cellSize;
                        int y = boardStartY + (currentPiece.y + i) * cellSize;
                        drawBlock(canvas, x, y, pieceColors[currentPiece.type]);
                    }
                }
            }
        }
    }
    
    private void drawPieceGhost(Canvas canvas) {
        if (currentPiece != null && isGameRunning && !isGamePaused) {
            // 计算虚影位置（方块的最终落下位置）
            int ghostY = calculateGhostPosition();
            
            if (ghostY != currentPiece.y) { // 只有当虚影位置与当前位置不同时才绘制
                int[][] shape = pieceShapes[currentPiece.type][currentPiece.rotation];
                
                for (int i = 0; i < shape.length; i++) {
                    for (int j = 0; j < shape[i].length; j++) {
                        if (shape[i][j] == 1) {
                            int x = boardStartX + (currentPiece.x + j) * cellSize;
                            int y = boardStartY + (ghostY + i) * cellSize;
                            drawGhostBlock(canvas, x, y, pieceColors[currentPiece.type]);
                        }
                    }
                }
            }
        }
    }
    
    private int calculateGhostPosition() {
        if (currentPiece == null) return 0;
        
        int ghostY = currentPiece.y;
        
        // 向下移动直到发生碰撞
        while (!isCollision(currentPiece, currentPiece.x, ghostY + 1)) {
            ghostY++;
        }
        
        return ghostY;
    }
    
    private void drawGhostBlock(Canvas canvas, int x, int y, int color) {
        // 绘制虚影方块 - 半透明效果
        Paint ghostPaint = new Paint();
        ghostPaint.setAntiAlias(true);
        
        // 使用半透明的颜色
        int alpha = 60; // 透明度
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        ghostPaint.setColor(Color.argb(alpha, red, green, blue));
        
        // 绘制虚影方块主体
        canvas.drawRect(x, y, x + cellSize, y + cellSize, ghostPaint);
        
        // 绘制虚影边框
        Paint ghostBorderPaint = new Paint();
        ghostBorderPaint.setColor(Color.argb(alpha + 40, 255, 255, 255));
        ghostBorderPaint.setStyle(Paint.Style.STROKE);
        ghostBorderPaint.setStrokeWidth(2);
        ghostBorderPaint.setAntiAlias(true);
        canvas.drawRect(x, y, x + cellSize, y + cellSize, ghostBorderPaint);
        
        // 绘制虚影内部虚线效果
        Paint dashedPaint = new Paint();
        dashedPaint.setColor(Color.argb(alpha + 60, 255, 255, 255));
        dashedPaint.setStyle(Paint.Style.STROKE);
        dashedPaint.setStrokeWidth(1);
        dashedPaint.setAntiAlias(true);
        
        // 绘制对角线虚线
        canvas.drawLine(x + 4, y + 4, x + cellSize - 4, y + cellSize - 4, dashedPaint);
        canvas.drawLine(x + cellSize - 4, y + 4, x + 4, y + cellSize - 4, dashedPaint);
    }
    
    private void drawBlock(Canvas canvas, int x, int y, int color) {
        // 绘制阴影 - 更深的阴影
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(120, 0, 0, 0));
        canvas.drawRect(x + 4, y + 4, x + cellSize + 4, y + cellSize + 4, shadowPaint);
        
        // 绘制主体方块 - 添加渐变效果
        paint.setColor(color);
        canvas.drawRect(x, y, x + cellSize, y + cellSize, paint);
        
        // 绘制渐变效果 - 从亮到暗
        Paint gradientPaint = new Paint();
        gradientPaint.setColor(Color.argb(40, 255, 255, 255));
        canvas.drawRect(x, y, x + cellSize, y + cellSize / 2, gradientPaint);
        
        // 绘制底部阴影
        Paint bottomShadowPaint = new Paint();
        bottomShadowPaint.setColor(Color.argb(60, 0, 0, 0));
        canvas.drawRect(x, y + cellSize * 2 / 3, x + cellSize, y + cellSize, bottomShadowPaint);
        
        // 绘制高光效果 - 更明显的高光
        Paint highlightPaint = new Paint();
        highlightPaint.setColor(Color.argb(100, 255, 255, 255));
        canvas.drawRect(x + 3, y + 3, x + cellSize - 3, y + cellSize / 4, highlightPaint);
        
        // 绘制左侧高光
        Paint leftHighlightPaint = new Paint();
        leftHighlightPaint.setColor(Color.argb(80, 255, 255, 255));
        canvas.drawRect(x + 1, y + 1, x + cellSize / 4, y + cellSize - 1, leftHighlightPaint);
        
        // 绘制主边框
        canvas.drawRect(x, y, x + cellSize, y + cellSize, borderPaint);
        
        // 绘制内部边框 - 更精细的边框
        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(Color.argb(180, 255, 255, 255));
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(1);
        canvas.drawRect(x + 2, y + 2, x + cellSize - 2, y + cellSize - 2, innerBorderPaint);
        
        // 绘制右下角暗边
        Paint darkEdgePaint = new Paint();
        darkEdgePaint.setColor(Color.argb(100, 0, 0, 0));
        darkEdgePaint.setStrokeWidth(2);
        canvas.drawLine(x + cellSize - 2, y + 2, x + cellSize - 2, y + cellSize - 2, darkEdgePaint);
        canvas.drawLine(x + 2, y + cellSize - 2, x + cellSize - 2, y + cellSize - 2, darkEdgePaint);
    }
    
    private void drawPauseOverlay(Canvas canvas) {
        paint.setColor(Color.argb(128, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        
        textPaint.setTextSize(50);
        String pauseText = getContext().getString(R.string.tetris_paused);
        Rect bounds = new Rect();
        textPaint.getTextBounds(pauseText, 0, pauseText.length(), bounds);
        
        int x = (getWidth() - bounds.width()) / 2;
        int y = getHeight() / 2;
        
        canvas.drawText(pauseText, x, y, textPaint);
    }
    
    // 游戏控制方法
    public void startGame() {
        isGameRunning = true;
        isGamePaused = false;
        score = 0;
        level = 1;
        linesCleared = 0;
        initializeBoard();
        generateNewPiece();
        invalidate();
        
        if (gameListener != null) {
            gameListener.onScoreChanged(score);
            gameListener.onLevelChanged(level);
            gameListener.onLinesChanged(linesCleared);
        }
    }
    
    public void pauseGame() {
        isGamePaused = !isGamePaused;
        invalidate();
    }
    
    public void moveLeft() {
        if (isGameRunning && !isGamePaused && !isAnimating && currentPiece != null) {
            if (!isCollision(currentPiece, currentPiece.x - 1, currentPiece.y)) {
                currentPiece.x--;
                invalidate();
            }
        }
    }
    
    public void moveRight() {
        if (isGameRunning && !isGamePaused && !isAnimating && currentPiece != null) {
            if (!isCollision(currentPiece, currentPiece.x + 1, currentPiece.y)) {
                currentPiece.x++;
                invalidate();
            }
        }
    }
    
    public void rotate() {
        if (isGameRunning && !isGamePaused && !isAnimating && currentPiece != null) {
            int newRotation = (currentPiece.rotation + 1) % 4;
            TetrisPiece testPiece = new TetrisPiece(currentPiece.type);
            testPiece.x = currentPiece.x;
            testPiece.y = currentPiece.y;
            testPiece.rotation = newRotation;
            
            if (!isCollision(testPiece, testPiece.x, testPiece.y)) {
                currentPiece.rotation = newRotation;
                invalidate();
            }
        }
    }
    
    public void softDrop() {
        if (isGameRunning && !isGamePaused && !isAnimating && currentPiece != null) {
            if (!isCollision(currentPiece, currentPiece.x, currentPiece.y + 1)) {
                currentPiece.y++;
                score += 1;
                invalidate();
                
                if (gameListener != null) {
                    gameListener.onScoreChanged(score);
                }
            } else {
                placePiece();
            }
        }
    }
    
    public void hardDrop() {
        if (isGameRunning && !isGamePaused && !isAnimating && currentPiece != null) {
            int dropDistance = 0;
            while (!isCollision(currentPiece, currentPiece.x, currentPiece.y + 1)) {
                currentPiece.y++;
                dropDistance++;
            }
            score += dropDistance * 2;
            placePiece();
            invalidate();
            
            if (gameListener != null) {
                gameListener.onScoreChanged(score);
            }
        }
    }
    
    private boolean isCollision(TetrisPiece piece, int newX, int newY) {
        int[][] shape = pieceShapes[piece.type][piece.rotation];
        
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int boardX = newX + j;
                    int boardY = newY + i;
                    
                    // 检查边界
                    if (boardX < 0 || boardX >= BOARD_WIDTH || 
                        boardY >= BOARD_HEIGHT) {
                        return true;
                    }
                    
                    // 检查是否与已放置的方块碰撞
                    if (boardY >= 0 && board[boardY][boardX] != EMPTY) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void placePiece() {
        int[][] shape = pieceShapes[currentPiece.type][currentPiece.rotation];
        
        // 将当前方块放置到游戏板上
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int boardX = currentPiece.x + j;
                    int boardY = currentPiece.y + i;
                    
                    if (boardY >= 0 && boardY < BOARD_HEIGHT && 
                        boardX >= 0 && boardX < BOARD_WIDTH) {
                        board[boardY][boardX] = currentPiece.type;
                    }
                }
            }
        }
        
        // 检查并清除完整的行
        clearLines();
        
        // 生成新方块
        generateNewPiece();
    }
    
    private void clearLines() {
        List<Integer> linesToClear = new ArrayList<>();
        
        // 找到需要清除的行
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean isLineFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (board[i][j] == EMPTY) {
                    isLineFull = false;
                    break;
                }
            }
            if (isLineFull) {
                linesToClear.add(i);
            }
        }
        
        // 如果有行需要清除，启动动画
        if (!linesToClear.isEmpty()) {
            startLineClearAnimation(linesToClear);
        }
    }
    
    private void startLineClearAnimation(List<Integer> linesToClear) {
        if (isAnimating) return; // 防止重复动画
        
        isAnimating = true;
        animatingLines.clear();
        animatingLines.addAll(linesToClear);
        
        // 创建动画
        lineAnimator = ValueAnimator.ofFloat(0f, 1f);
        lineAnimator.setDuration(800); // 动画持续时间
        lineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                animationProgress = (Float) animation.getAnimatedValue();
                invalidate(); // 重绘视图
            }
        });
        
        lineAnimator.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {}
            
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // 动画结束后实际清除行
                finishLineClear(linesToClear);
                isAnimating = false;
                animatingLines.clear();
                invalidate();
            }
            
            @Override
            public void onAnimationCancel(android.animation.Animator animation) {
                isAnimating = false;
                animatingLines.clear();
            }
            
            @Override
            public void onAnimationRepeat(android.animation.Animator animation) {}
        });
        
        lineAnimator.start();
    }
    
    private void finishLineClear(List<Integer> linesToClear) {
        // 移除完整的行
        for (int lineIndex : linesToClear) {
            for (int i = lineIndex; i > 0; i--) {
                System.arraycopy(board[i - 1], 0, board[i], 0, BOARD_WIDTH);
            }
            // 清空顶行
            for (int j = 0; j < BOARD_WIDTH; j++) {
                board[0][j] = EMPTY;
            }
        }
        
        // 计分
        int clearedLines = linesToClear.size();
        linesCleared += clearedLines;
        
        // 根据清除的行数计分
        int lineScore = 0;
        switch (clearedLines) {
            case 1: lineScore = 100; break;
            case 2: lineScore = 300; break;
            case 3: lineScore = 500; break;
            case 4: lineScore = 800; break; // Tetris!
        }
        score += lineScore * level;
        
        // 升级
        level = (linesCleared / 10) + 1;
        
        if (gameListener != null) {
            gameListener.onScoreChanged(score);
            gameListener.onLevelChanged(level);
            gameListener.onLinesChanged(linesCleared);
        }
    }
    
    private void gameOver() {
        isGameRunning = false;
        if (gameListener != null) {
            gameListener.onGameOver();
        }
    }
    
    public void setGameListener(TetrisGameListener listener) {
        this.gameListener = listener;
    }
    
    public boolean isGameRunning() {
        return isGameRunning;
    }
    
    public boolean isGamePaused() {
        return isGamePaused;
    }
    
    public int getScore() {
        return score;
    }
    
    public int getLevel() {
        return level;
    }
    
    public int getLinesCleared() {
        return linesCleared;
    }
    
    public TetrisPiece getNextPiece() {
        return nextPiece;
    }
    
    public int[][] getNextPieceShape() {
        if (nextPiece != null) {
            return pieceShapes[nextPiece.type][0];
        }
        return null;
    }
    
    public int getNextPieceColor() {
        if (nextPiece != null) {
            return pieceColors[nextPiece.type];
        }
        return Color.WHITE;
    }
    
    private void initColors() {
        pieceColors = new int[]{
            getContext().getColor(R.color.tetris_piece_empty),    // EMPTY
            getContext().getColor(R.color.tetris_piece_i),        // I_PIECE - 青色
            getContext().getColor(R.color.tetris_piece_o),        // O_PIECE - 黄色
            getContext().getColor(R.color.tetris_piece_t),        // T_PIECE - 粉色
            getContext().getColor(R.color.tetris_piece_s),        // S_PIECE - 绿色
            getContext().getColor(R.color.tetris_piece_z),        // Z_PIECE - 红色
            getContext().getColor(R.color.tetris_piece_j),        // J_PIECE - 蓝色
            getContext().getColor(R.color.tetris_piece_l)         // L_PIECE - 橙色
        };
    }
    
    // 内部类：俄罗斯方块
    public static class TetrisPiece {
        public int type;
        public int x;
        public int y;
        public int rotation;
        
        public TetrisPiece(int type) {
            this.type = type;
            this.x = BOARD_WIDTH / 2 - 1;
            this.y = 0;
            this.rotation = 0;
        }
    }
} 