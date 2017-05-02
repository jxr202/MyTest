package com.jxr.leui_clear_anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

/**
 * 自定义VIEW PinWheel 包含三个部分：圆环进度条、大风车和粒子系统
 * 几个常用方法：
 * {@link #init()}                      初始化
 * {@link #setMax(float)}               设置大风车旋转的总角度
 * {@link #setProgress(float)}          设置大风车当前旋转角度
 * {@link #start()}                     开始播放动画
 * {@link #stop()}                      播放停止动画
 */
public class LePinWheelWidget extends LeCircleProgressBar implements Handler.Callback {

    public static final String TAG = "LePinWheelWidget";

    //Handle message
    private static final int UNIFORM = 1; //to keep the speed of pinwheel
    private static final int STOP_EMIT = 2;//stop toast particle
    private static final int CANCEL_EMIT = 3;//cancel particle
    private static final int DOWN_PINWHEEL = 4;//make pinwheel's speed slow
    private static final int CANCLE_ALL_MESSAGE = 5;
    private static final int STOP_DELAY = 6;//delay to stop when ani < mProgressbarDuration
    private static final int STOP_DELAY_WITH_PROGRESS = 7;//delay to stop with progress when ani < mProgressbarDuration
    private static final int STOP_ALL = -1;

    //PinWheel
    private Bitmap pinWheelBmp = null;
    private volatile float rotationDegree = 0;
    public static final float UNIFORM_DEGREE = 16;
    private float xScaleFactor = 0;
    private float yScaleFactor = 0;
    private long startTime = 0;
    private long stopTime = 0;

    //Progressbar
    private static final int mProgressbarDuration = 1500;
    private volatile boolean isRunning = false;

    //use for interpolator new PathInterpolator(path));
    Interpolator interpolator;

    //Particle
    private LeParticleSystem particleSystem = null;
    private Handler handler = null;

    public ValueAnimator progressbarStartAnimator = null;
    public ValueAnimator pinWheelUpSpeedAnimator = null;
    public ValueAnimator progressbarStopAnimator = null;
    public ValueAnimator pinWheelDownAnimator = null;


    private float mDpToPxScale;
    /** 大风车旋转动画状态 **/
    private volatile boolean UNIFORM_STATE;
    /** 进度条动画状态 **/
    private volatile boolean ROTATION_STATE;


    public LePinWheelWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWidget();
    }

    private void initWidget() {
        setStartDegree(-222);
        setTotalDegree(264);
        setMax(100.0f);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = (int) getResources().getDisplayMetrics().density;
        pinWheelBmp = BitmapFactory.decodeResource(getResources(), R.drawable.le_control_center_pinwheel);
    }

    /**
     * 初始化，设置一些变量
     */
    public void init() {
        rotationDegree = 0;
        handler = new Handler(this);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mDpToPxScale = (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
        float mDensity = getResources().getDisplayMetrics().density;

        Log.i(TAG, "init.. mDpToPxScale: " + mDpToPxScale + ", mDensity: " + mDensity);

        Path path = new Path();
        path.moveTo(0.0f, 0.0f);
        path.cubicTo(1 / 3f, 0, 0, 1, 1, 1);
        interpolator = new PathInterpolator(path);
    }


    /**
     * <p>开始播放三个动画：粒子系统动效、大风车动画、环状进度条动画</p>
     * 1.环状进度条动画：从开始的进度80，回归到0 <br/>
     * 2.大风车动画：启动一个0到16度的旋转动画，并通过handler不停地旋转，直到调用了{@link #stop()}方法<br/>
     * 3.粒子系统动效：不停地在中心位置产生粒子，并通过随机路径流动。<br/>
     * @return success
     */
    public boolean start() {
        if (isRunning) {
            return false;
        }

        isRunning = true;

        if (this.handler == null) {
            throw new IllegalArgumentException("PinWheel#handler is null,you must call PinWheel#init()");
        }

        //粒子系统动画
        particleSystem = new LeParticleSystem(this, getResources().getDrawable(R.drawable.le_control_center_point, null), 30, 6000, mDpToPxScale)
                .setSpeedByComponentsRange(-0.08f, 0.08f, -0.08f, 0.08f)
                .addModifier(new LeParticleSystem.AlphaModifier(-250, 250, 0, 2000))
                .addModifier(new LeParticleSystem.ScaleModifier(0.3f, 0.5f, 0, 400))
                .emit();

        //大风车动画
        pinWheelUpSpeedAnimator = ValueAnimator.ofFloat(0.0f, UNIFORM_DEGREE);
        pinWheelUpSpeedAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setRotationDegree((Float) animation.getAnimatedValue());
            }
        });
        pinWheelUpSpeedAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                UNIFORM_STATE = true;
                handler.sendEmptyMessageDelayed(UNIFORM, 0);
            }
        });
        pinWheelUpSpeedAnimator.start();    //启动大风车动画

        //环状进度条动画
        progressbarStartAnimator = ValueAnimator.ofFloat(/*getProgress()*/90f, 0.0f);
        progressbarStartAnimator.setDuration(mProgressbarDuration);
        progressbarStartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressbarStartAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ROTATION_STATE = true;
                startTime = SystemClock.elapsedRealtime();
            }
        });
        progressbarStartAnimator.start();   //启动环状进度条动画

        //模拟动画在2000毫秒停止，
        handler.sendEmptyMessageDelayed(STOP_ALL, 2000);

        return true;
    }


    /**
     * <p>停止三个动画：粒子系统动效、大风车动画、环状进度条动画</p>
     * before call this you must call {@link #setProgress(float)}
     * @return success
     */
    public boolean stop() {
        if (!isRunning) {
            return false;
        }
        stopTime = SystemClock.elapsedRealtime();
        if (stopTime - startTime < mProgressbarDuration) {
            handler.removeMessages(STOP_DELAY);
            handler.sendEmptyMessageDelayed(STOP_DELAY, mProgressbarDuration - (stopTime - startTime));
            return true;
        }
        isRunning = false;
        rotationDegree = 0;

        //停止大风车动画
        pinWheelDownAnimator = ValueAnimator.ofFloat(UNIFORM_DEGREE, 0);
        pinWheelDownAnimator.setDuration(mProgressbarDuration * 2);
        pinWheelDownAnimator.setInterpolator(new LinearInterpolator());
        pinWheelDownAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float degree = (float) animation.getAnimatedValue();
                if (degree > 1f) {
                    setRotationDegree(degree);
                }
            }
        });
        pinWheelDownAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                UNIFORM_STATE = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ROTATION_STATE = true;
                handler.sendEmptyMessageDelayed(LePinWheelWidget.CANCEL_EMIT, 5);   //大风车动画停止后，取消粒子动画的运行
            }
        });

        //停止环状进度条动画
        progressbarStopAnimator = ValueAnimator.ofFloat(0.0f, /*getProgress()*/90f);
        progressbarStopAnimator.setDuration(mProgressbarDuration);
        progressbarStopAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressbarStopAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setProgress((float) animation.getAnimatedValue());
            }
        });
        progressbarStopAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                pinWheelDownAnimator.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                handler.sendEmptyMessage(LePinWheelWidget.STOP_EMIT);   //停止粒子动画
            }
        });
        progressbarStopAnimator.start();

        return true;
    }

    /**
     * @param progress degree
     * @return success
     */
    public boolean stop(float progress) {
        stopTime = SystemClock.elapsedRealtime();
        if(stopTime - startTime < mProgressbarDuration){
            handler.removeMessages(LePinWheelWidget.STOP_DELAY_WITH_PROGRESS);
            Message msg = Message.obtain();
            msg.what = LePinWheelWidget.STOP_DELAY_WITH_PROGRESS;
            msg.obj = progress;
            handler.sendMessageDelayed(msg, mProgressbarDuration - (stopTime - startTime));
            return true;
        }
        setProgress(progress);
        return stop();
    }


    /**
     * set rotation angle of pinwheel
     *
     * @param rad angle
     * @return this
     */
    private LePinWheelWidget setRotationDegree(float rad) {
        this.rotationDegree -= rad;
        postInvalidate();
        return this;
    }

    //draw the center of circle view,means pinwheel
    @Override
    public boolean drawCustomView(Canvas canvas, float mCenterX, float mCenterY, float mTotalDegree) {
        Matrix matrix = new Matrix();
        float halfBmpWidth = pinWheelBmp.getWidth() / 2;
        float halfBmpHeight = pinWheelBmp.getHeight() / 2;
        if (xScaleFactor == 0 || yScaleFactor == 0) {
            xScaleFactor = (float) (0.9 * mCenterX / halfBmpWidth); //大风车和圆圈的比率
            yScaleFactor = (float) (0.9 * mCenterY / halfBmpWidth); //大风车和圆圈的比率
            Log.i(TAG, "drawCustomView.. mCenterX:" + mCenterX + ", halfBmpWidth: " + halfBmpWidth + ", xScaleFactor: " + xScaleFactor);
        }
        matrix.setScale(xScaleFactor, yScaleFactor, halfBmpWidth, halfBmpWidth);
        if (ROTATION_STATE) {
            matrix.preRotate(rotationDegree, halfBmpWidth, halfBmpHeight);
        }
        canvas.drawBitmap(pinWheelBmp, matrix, null);
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case UNIFORM: {
                LePinWheelWidget.this.setRotationDegree(UNIFORM_DEGREE);
                if (UNIFORM_STATE)
                    handler.sendEmptyMessageDelayed(UNIFORM, 0);
                break;
            }
            case DOWN_PINWHEEL: {

            }
            case STOP_EMIT: {
                particleSystem.stopEmitting();
                break;
            }
            case CANCEL_EMIT: {
                particleSystem.cancel();
                break;
            }
            case CANCLE_ALL_MESSAGE: {

                handler.removeCallbacksAndMessages(null);
                break;
            }
            case STOP_DELAY:
                stop();
                break;
            case STOP_DELAY_WITH_PROGRESS:
                float progess = (float) msg.obj;
                stop(progess);
                break;
            case STOP_ALL: {
                stop(90f);
                break;
            }

        }
        return false;
    }



}
