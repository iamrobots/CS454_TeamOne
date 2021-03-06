package com.iamrobots.connectfour.gamePlay;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.iamrobots.connectfour.R;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by David Lively on 1/18/18.
 * lively@iamrobots.com
 */

public class BoardView extends View {

    private static final int DEFAULT_ROWS = 6;
    private static final int DEFAULT_COLUMNS = 7;
    private static final int BOARD_HOLE_PADDING = 6;
    private static final int DEFAULT_BOARD_COLOR = Color.parseColor("#3498db");
    private static final int DEFAULT_PLAYER1_COLOR = Color.parseColor("#f1c40f");
    private static final int DEFAULT_PLAYER2_COLOR = Color.parseColor("#e74c3c");

    private int mRows;
    private int mColumns;
    private float mRadius;
    private float[] mPosX;
    private float[] mPosY;

    private Bitmap mBoardBitmap;
    private Paint mBoardPaint;
    private Paint mEraser;

    private Bitmap mBackBoardBitmap;
    private Paint mBackBoardPaint;
    private Paint mFirstPlayerPaint;
    private Paint mSecondPlayerPaint;

//    private Bitmap mAnimationBitmap;

    private LinkedList<Token> mTokenList;
    private Token mToken;


    private void init(@Nullable AttributeSet attrs) {

        // Setting up paints
        mBoardPaint = new Paint();
        mBoardPaint.setAntiAlias(true);
        mBoardPaint.setStyle(Paint.Style.FILL);

        mEraser = new Paint();
        mEraser.setAntiAlias(true);
        mEraser.setColor(Color.TRANSPARENT);
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mBackBoardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBackBoardPaint.setColor(Color.WHITE);
        mBackBoardPaint.setStyle(Paint.Style.FILL);

        mFirstPlayerPaint = new Paint();
        mFirstPlayerPaint.setAntiAlias(true);
        mFirstPlayerPaint.setStyle(Paint.Style.FILL);

        mSecondPlayerPaint = new Paint();
        mSecondPlayerPaint.setAntiAlias(true);
        mSecondPlayerPaint.setStyle(Paint.Style.FILL);

        // Parse attributes from attrs.xml here
        if (attrs != null) {
            TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.BoardView, 0, 0);
            try {
                mRows = a.getInt(R.styleable.BoardView_rows, DEFAULT_ROWS);
                mColumns = a.getInt(R.styleable.BoardView_columns, DEFAULT_COLUMNS);
                mBoardPaint.setColor(a.getColor(R.styleable.BoardView_board_color, DEFAULT_BOARD_COLOR));
                mFirstPlayerPaint.setColor(a.getColor(R.styleable.BoardView_player1_color, DEFAULT_PLAYER1_COLOR));
                mSecondPlayerPaint.setColor(a.getColor(R.styleable.BoardView_player2_color, DEFAULT_PLAYER2_COLOR));

            } finally {
                a.recycle();
            }
        } else {
            mRows = DEFAULT_ROWS;
            mColumns = DEFAULT_COLUMNS;
            mBoardPaint.setColor(DEFAULT_BOARD_COLOR);
            mFirstPlayerPaint.setColor(DEFAULT_PLAYER1_COLOR);
            mSecondPlayerPaint.setColor(DEFAULT_PLAYER2_COLOR);
        }

        mPosY = new float[mRows];
        mPosX = new float[mColumns];

        mTokenList = new LinkedList<>();
    }

    public BoardView(Context context) {
        super(context);
        init(null);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public BoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(attrs);
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBackBoardBitmap, 0, 0, null);
//        canvas.drawBitmap(mAnimationBitmap, 0, 0, null);
        if (!mTokenList.isEmpty()) {
            for (Token token : mTokenList) {
                canvas.drawCircle(token.getX(), token.getY(), mRadius, token.getTokenPaint());
            }
        }
        canvas.drawBitmap(mBoardBitmap, 0, 0, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int w = resolveSize(minw, widthMeasureSpec);

        int minh = getPaddingBottom() + getPaddingTop() + ((w / mColumns) * mRows);
        int h = resolveSize(minh, heightMeasureSpec);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        initBoard(right - left, bottom - top);
    }


    // TODO: move createBitmap to onLayout or sizeChanged (use drawPaint here) and think about moving mPos and mRadius.
    private void initBoard(int width, int height) {
        mRadius = Math.min(width / mColumns, height / mRows) / 2;
        mPosX[0] = mRadius + ((float) width - (mRadius * mColumns) * 2) / 2;
        mPosY[mRows - 1] = mRadius + ((float) height - (mRadius * mRows) * 2) / 2;

        for (int i = 1; i < mColumns; i++) {
            mPosX[i] = mPosX[i - 1] + mRadius * 2;
        }

        for (int i = mRows - 1; i > 0; i--) {
            mPosY[i - 1] = mPosY[i] + mRadius * 2;
        }

        mBackBoardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas backboardCanvas = new Canvas(mBackBoardBitmap);
        backboardCanvas.drawRect(0f, 0f, width, height, mBackBoardPaint);

//        mAnimationBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mBoardBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas boardCanvas = new Canvas(mBoardBitmap);
        boardCanvas.drawRect(0f, 0f, (float) width, (float) height, mBoardPaint);

        for (int i = 0; i < mRows; ++i) {
            for (int j = 0; j < mColumns; ++j) {
                boardCanvas.drawCircle(mPosX[j], mPosY[i], mRadius - BOARD_HOLE_PADDING, mEraser);
            }
        }
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /*
     * Drops a ball of the players color at the given row and column.
     */
    public void dropToken(final int row, final int column, int player) {
        final Paint playerPaint;

        if (column < 0 || column >= mColumns)
            return;
        if (row < 0 || row >= mRows)
            return;
        if (player > 1 || player < 0)
            return;
        if (mBackBoardBitmap == null)
            return;

        if (player == 1)
            playerPaint = mSecondPlayerPaint;
        else
            playerPaint = mFirstPlayerPaint;

        final Token token = new Token(row, column, mPosY[row], mPosX[column], new Paint(playerPaint));
        mTokenList.add(token);


        ValueAnimator animation = ValueAnimator.ofFloat(0f, mPosY[row]);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                token.setY(value);
                invalidate();
            }
        });

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mTokenList.contains(token) == true) {
                    mTokenList.remove(token);
                    Canvas backBoardCanvas = new Canvas(mBackBoardBitmap);
                    backBoardCanvas.drawCircle(mPosX[token.getColumn()], mPosY[token.getRow()], mRadius, token.getTokenPaint());
                    invalidate();
                }
            }
        });

        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(120 * (mRows - row));
        animation.start();
    }

    public void removeToken(int row, int column) {

        if (column < 0 || column >= mColumns)
            return;
        if (row < 0 || row >= mRows)
            return;
        if (mBackBoardBitmap == null)
            return;

        if (mTokenList.isEmpty() == false) {
            Token token = mTokenList.removeLast();
        } else {
            Canvas canvas = new Canvas(mBackBoardBitmap);
            canvas.drawCircle(mPosX[column], mPosY[row], mRadius, mBackBoardPaint);
        }

        invalidate();
    }

    public void setRowsColumns(int rows, int columns) {
        mRows = rows;
        mColumns = columns;
        mPosX = new float[columns];
        mPosY = new float[rows];
    }

    public void setFirstPlayerColor(int color) {
        mFirstPlayerPaint.setColor(color);
    }

    public void setSecondPlayerColor(int color) {
        mSecondPlayerPaint.setColor(color);
    }

    // TODO: figure out what to do when out of bounds (above, below, margins, padding) on getRow and getColumn
    public int getRow(float y) {
        if (y < 0 || y > getMeasuredHeight())
            return -1;
        int interval = getMeasuredHeight() / mRows;
        return mRows - ( (int) y / interval) - 1;

    }

    public int getColumn(float x) {
        if (x < 0 || x > getMeasuredWidth())
            return -1;
        int interval = getMeasuredWidth() / mColumns;
        return (int) x / interval;

    }


    public void highlightTokens(final ArrayList<Pair<Integer, Integer>> rowColumnArray, int player) {

        if (rowColumnArray == null || player < 0 || player > 1) {
            return;
        }

        final Paint paint;
        final Canvas canvas = new Canvas(mBoardBitmap);
        paint = player == 0 ? new Paint(mFirstPlayerPaint) : new Paint(mSecondPlayerPaint);

        // Begin new code (remove new Paint from above if not used)
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(6.0f);
        paint.setColor(Color.BLACK);

        for (Pair<Integer, Integer> rowColumnPair : rowColumnArray) {
            int row = rowColumnPair.first;
            int column = rowColumnPair.second;
            canvas.drawCircle(mPosX[column], mPosY[row], mRadius - BOARD_HOLE_PADDING, paint);
        }
    }

    public void clear() {
        initBoard(getWidth(), getHeight());
        invalidate();
    }

    private static class Token {
        private Paint mTokenPaint;
        private int mRow;
        private int mColumn;
        private float mY;
        private float mX;

        Token(int row, int column, float y, float x, Paint paint) {
            mTokenPaint= paint;
            mRow = row;
            mColumn = column;
            mY = y;
            mX = x;
        }

        public Paint getTokenPaint() {
            return mTokenPaint;
        }

        public void setTokenPaint(Paint tokenPaint) {
            mTokenPaint = tokenPaint;
        }

        public float getY() {
            return mY;
        }

        public void setY(float y) {
            mY = y;
        }

        public float getX() {
            return mX;
        }

        public void setX(float x) {
            mX = x;
        }

        public int getRow() {
            return mRow;
        }

        public void setRow(int row) {
            mRow = row;
        }

        public int getColumn() {
            return mColumn;
        }

        public void setColumn(int column) {
            mColumn = column;
        }
    }
}
