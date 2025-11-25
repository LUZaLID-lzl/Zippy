package com.luza.zippy.ui.sidebarList.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.luza.zippy.R;

public class NextPiecePreview extends View {
    
    private Paint paint;
    private Paint borderPaint;
    private Paint textPaint;
    private int cellSize = 30;
    private int[] pieceColors;
    
    // 当前要显示的下一个方块信息
    private int nextPieceType = 0;
    private int[][] nextPieceShape;
    
    // 方块形状定义（与TetrisGameView保持一致）
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
    
    public NextPiecePreview(Context context) {
        super(context);
        init();
    }
    
    public NextPiecePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        
        borderPaint = new Paint();
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setAntiAlias(true);
        
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24);
        textPaint.setAntiAlias(true);
        
        initColors();
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
    
    public void setNextPiece(int pieceType) {
        this.nextPieceType = pieceType;
        if (pieceType > 0 && pieceType < pieceShapes.length) {
            this.nextPieceShape = pieceShapes[pieceType][0]; // 使用第一个旋转状态
        } else {
            this.nextPieceShape = null;
        }
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // 绘制背景
        canvas.drawColor(Color.argb(50, 0, 0, 0));
        
        // 绘制边框
        Paint bgBorderPaint = new Paint();
        bgBorderPaint.setColor(Color.argb(100, 255, 255, 255));
        bgBorderPaint.setStyle(Paint.Style.STROKE);
        bgBorderPaint.setStrokeWidth(2);
        canvas.drawRect(5, 5, getWidth() - 5, getHeight() - 5, bgBorderPaint);
        
        if (nextPieceShape != null && nextPieceType > 0) {
            // 计算方块在视图中的居中位置
            int shapeWidth = nextPieceShape[0].length * cellSize;
            int shapeHeight = nextPieceShape.length * cellSize;
            
            int startX = (getWidth() - shapeWidth) / 2;
            int startY = (getHeight() - shapeHeight) / 2;
            
            // 绘制方块
            for (int i = 0; i < nextPieceShape.length; i++) {
                for (int j = 0; j < nextPieceShape[i].length; j++) {
                    if (nextPieceShape[i][j] == 1) {
                        int x = startX + j * cellSize;
                        int y = startY + i * cellSize;
                        drawMiniBlock(canvas, x, y, cellSize, pieceColors[nextPieceType]);
                    }
                }
            }
        }
    }
    
    private void drawMiniBlock(Canvas canvas, int x, int y, int size, int color) {
        // 绘制阴影
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawRect(x + 2, y + 2, x + size + 2, y + size + 2, shadowPaint);
        
        // 绘制主体方块
        paint.setColor(color);
        canvas.drawRect(x, y, x + size, y + size, paint);
        
        // 绘制渐变效果
        Paint gradientPaint = new Paint();
        gradientPaint.setColor(Color.argb(40, 255, 255, 255));
        canvas.drawRect(x, y, x + size, y + size / 2, gradientPaint);
        
        // 绘制高光
        Paint highlightPaint = new Paint();
        highlightPaint.setColor(Color.argb(80, 255, 255, 255));
        canvas.drawRect(x + 2, y + 2, x + size - 2, y + size / 3, highlightPaint);
        
        // 绘制边框
        canvas.drawRect(x, y, x + size, y + size, borderPaint);
        
        // 绘制内部边框
        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(Color.argb(120, 255, 255, 255));
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(1);
        canvas.drawRect(x + 1, y + 1, x + size - 1, y + size - 1, innerBorderPaint);
    }
} 