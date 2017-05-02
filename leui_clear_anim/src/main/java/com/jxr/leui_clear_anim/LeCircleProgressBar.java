package com.jxr.leui_clear_anim;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 通过Canvas.drawArc来实现的一个圆环进度条<br/>
 *
 * <p>调用{@link #setStartDegree(int)}来设计一个起始进度，调用{@link #setTotalDegree(int)}来设置总进度</p>
 *
 * 中心位置可以通过{@link #drawCustomView(Canvas, float, float, float)}画大风车<br/>
 */
public class LeCircleProgressBar extends LinearLayout {

    private static final float DEFAULT_PROGRESS_STROKE_WIDTH = 1.0f;

    private final RectF mProgressRectF = new RectF();
    private final Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mProgressBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint.Cap mCap;

    private float mRadius;
    private float mCenterX;
    private float mCenterY;

    private int mProgressColor;
    private int mBackgroundColor;
    private float mProgressStrokeWidth;
    private int mProgressBackgroundColor;
    private float mStartDegree;
    private float mTotalDegree;

    private float mMax = 100;
    private float mCurrentProgress;


    public LeCircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        initFromAttributes(context, attrs);
        initPaint();
    }

    /**
     * Basic data initialization
     */
    private void initFromAttributes(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LeCircleProgressBar);
        mBackgroundColor = a.getColor(R.styleable.LeCircleProgressBar_background_color, Color.TRANSPARENT);
        mCap = a.hasValue(R.styleable.LeCircleProgressBar_progress_stroke_cap) ?
                Paint.Cap.values()[a.getInt(R.styleable.LeCircleProgressBar_progress_stroke_cap, 0)] : Paint.Cap.ROUND;
        mProgressStrokeWidth = a.getDimensionPixelSize(R.styleable.LeCircleProgressBar_progress_stroke_width,
                (int) (context.getResources().getDisplayMetrics().density * DEFAULT_PROGRESS_STROKE_WIDTH + 0.5f));
        mProgressBackgroundColor = a.getColor(R.styleable.LeCircleProgressBar_progress_background_color, Color.GRAY);
        mProgressColor = a.getColor(R.styleable.LeCircleProgressBar_progress_color, Color.WHITE);
        mStartDegree = a.getFloat(R.styleable.LeCircleProgressBar_progress_start_degree, -90.0f);
        mTotalDegree = a.getFloat(R.styleable.LeCircleProgressBar_progress_total_degree, 360.0f);
        a.recycle();
    }

    /**
     * Paint initialization
     */
    private void initPaint() {
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressStrokeWidth);
        mProgressPaint.setStrokeCap(mCap);
        mProgressPaint.setShader(null);
        mProgressPaint.setColor(mProgressColor);

        mProgressBackgroundPaint.setStyle(Paint.Style.STROKE);
        mProgressBackgroundPaint.setStrokeWidth(mProgressStrokeWidth);
        mProgressBackgroundPaint.setColor(mProgressBackgroundColor);
        mProgressBackgroundPaint.setStrokeCap(mCap);

        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setColor(mBackgroundColor);
    }

    public synchronized float getProgress() {
        return mCurrentProgress;
    }

    public synchronized float getMax() {
        return mMax;
    }

    public void setMax(float mMax) {
        this.mMax = mMax;
    }


    public synchronized void setProgress(float progress) {

        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMax) {
            progress = mMax;
        }

        if (progress != mCurrentProgress) {
            mCurrentProgress = progress;
            invalidate();
        }
    }


    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        drawProgress(canvas);
        drawCustomView(canvas, mCenterX, mCenterY, mTotalDegree);
    }

    private void drawBackground(Canvas canvas) {
        if (mBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(mCenterX, mCenterX, mRadius, mBackgroundPaint);
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawArc(mProgressRectF, mStartDegree, mTotalDegree, false, mProgressBackgroundPaint);
        if (getProgress() != 0) {
            canvas.drawArc(mProgressRectF, mStartDegree, mTotalDegree * getProgress() / getMax(), false, mProgressPaint);
        }
    }

    /**
     * When the size of CircleProgressBar changed, need to re-adjust the drawing area
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;

        mRadius = Math.min(mCenterX, mCenterY);
        mProgressRectF.top = mCenterY - mRadius;
        mProgressRectF.bottom = mCenterY + mRadius;
        mProgressRectF.left = mCenterX - mRadius;
        mProgressRectF.right = mCenterX + mRadius;
        //Prevent the progress from clipping
        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2);
    }

    public void setBackgroundColor(int backgroundColor) {
        this.mBackgroundColor = backgroundColor;
        mBackgroundPaint.setColor(backgroundColor);
        invalidate();
    }

    public LeCircleProgressBar setStartDegree(int startDegree) {
        this.mStartDegree = startDegree;
        return this;
    }

    public LeCircleProgressBar setTotalDegree(int totalDegree) {
        this.mTotalDegree = totalDegree;
        return this;
    }


    /**
     * draw custom view ,is call be CircleProgressBar{@link #onDraw(Canvas)}
     *
     * @param canvas      画布
     * @param centerX     the center of X
     * @param centerY     the center of Y
     * @param totalDegree the arc of degree on the display
     * @return if has custom view must return true;
     */
    public boolean drawCustomView(Canvas canvas, float centerX, float centerY, float totalDegree) {

        return false;
    }
}


