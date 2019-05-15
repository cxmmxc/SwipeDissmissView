package com.terry.swipedissmiss;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Author:ChenXinming
 * Date:2019/05/14
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public class CloudTerraceView extends View {

    private int mRadius;

    private static final String TAG_CXM = "cxm";
    private Paint mRadiusStrokePaint; //圆形线和半径线画笔
    private Paint mBgPaint; //背景画笔
    private Paint mPressPaint; // 按下状态的画笔
    private Paint mDeltaPaint; // 小三角画笔
    private Paint mOutSidePaint; // 外环画笔

    private int mBgColor;
    private int mRadiusStrokeWidth;
    private int mOutsideStrokeWidth;
    private int mOutsideStrokeColor;
    private int mRadiusStrokeColor;
    private int mPressColor;
    private int mDeltaColor;
    private int mArcCount;
    private float perAngle;
    private int mDeltaWidth;
    private RectF mBaseRectF;
    private int mDeltaRadius;

    private Point mDownPoint;
    private boolean mIsDown;
    private SweepGradient mSweepGradient;
    private List<Region> mRegionList;

    public CloudTerraceView(Context context) {
        this(context, null);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CloudTerraceView);
        mBgColor = attributes.getColor(R.styleable.CloudTerraceView_circle_bg, ContextCompat.getColor(context, R.color.ciclr_bg));
        mOutsideStrokeColor = attributes.getColor(R.styleable.CloudTerraceView_outside_stroke_color, ContextCompat.getColor(context, R.color.outside_color));
        mRadiusStrokeColor = attributes.getColor(R.styleable.CloudTerraceView_radius_stroke_color, ContextCompat.getColor(context, R.color.outside_color));
        mDeltaColor = attributes.getColor(R.styleable.CloudTerraceView_triangle_color, ContextCompat.getColor(context, R.color.delta_color));
        mPressColor = attributes.getColor(R.styleable.CloudTerraceView_press_color, ContextCompat.getColor(context, R.color.press_color));
        mOutsideStrokeWidth = attributes.getDimensionPixelSize(R.styleable.CloudTerraceView_outside_stroke_width, 2);
        mRadiusStrokeWidth = attributes.getDimensionPixelSize(R.styleable.CloudTerraceView_radius_stroke_width, 2);
        mDeltaWidth = attributes.getDimensionPixelSize(R.styleable.CloudTerraceView_delta_width, 30);
        mArcCount = attributes.getInteger(R.styleable.CloudTerraceView_arc_count, 8);
        Log.w(TAG_CXM, "count = " + mArcCount);
        attributes.recycle();
        initData();
    }

    private void initData() {
        mDownPoint = new Point();
        mRegionList = new ArrayList<>();
        mRadiusStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRadiusStrokePaint.setStyle(Paint.Style.STROKE);
        mRadiusStrokePaint.setStrokeWidth(mRadiusStrokeWidth);
        mRadiusStrokePaint.setColor(mRadiusStrokeColor);

        mDeltaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDeltaPaint.setStyle(Paint.Style.FILL);
        mDeltaPaint.setColor(mDeltaColor);

        mOutSidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutSidePaint.setStyle(Paint.Style.STROKE);
        mOutSidePaint.setStrokeWidth(mOutsideStrokeWidth);
        mOutSidePaint.setColor(mOutsideStrokeColor);

        mPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPressPaint.setStyle(Paint.Style.FILL);

        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setColor(mBgColor);

        perAngle = 360f / mArcCount;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRadius == 0) {
            return;
        }
        Log.w(TAG_CXM, "onDraw = mRadius = " + mRadius);
        canvas.drawCircle(mRadius, mRadius, mRadius, mBgPaint);
        if (mDownPoint.x != 0 && mDownPoint.y != 0) {
            double v = Math.toDegrees(Math.atan2(mDownPoint.y, mDownPoint.x));
            Log.w(TAG_CXM, "v = " + v);
        }

        drawAllArc(canvas);
        drawDownArc(canvas);
        canvas.save();
        for (int i = 0; i < mArcCount; i++) {
            Path path = new Path();
            path.moveTo(mRadius, mRadius - mDeltaRadius);
            path.lineTo(mRadius - (float) (mDeltaWidth * Math.sin(Math.toRadians(30d))), mRadius - mDeltaRadius + (float) (mDeltaWidth * Math.cos(Math.toRadians(30d))));
            path.lineTo(mRadius + (float) (mDeltaWidth * Math.sin(Math.toRadians(30d))), mRadius - mDeltaRadius + (float) (Math.cos(Math.toRadians(30d)) * mDeltaWidth));
            path.close();
            canvas.drawPath(path, mDeltaPaint);
            Log.i(TAG_CXM, "rotate = "+perAngle * i);
            canvas.rotate(perAngle, mRadius, mRadius);
        }
        canvas.restore();
    }

    private void drawDownArc(Canvas canvas) {
        mSweepGradient = new SweepGradient(mRadius, mRadius, new int[]{Color.TRANSPARENT, Color.YELLOW}, new float[]{0.6f, 1f});
        mPressPaint.setShader(mSweepGradient);
        //判断按下状态
        for (int i = 0; i < mRegionList.size(); i++) {
            Region region = mRegionList.get(i);
            if (region.contains(mDownPoint.x, mDownPoint.y)) {
                canvas.drawArc(mBaseRectF);
            }
        }
    }

    private void drawAllArc(Canvas canvas) {
        for (int i = 0; i < mArcCount; i++) {
            Path path = new Path();
            path.moveTo(mRadius, mRadius);
            path.arcTo(mBaseRectF, i * perAngle + perAngle / 2f, perAngle);
            RectF rectF = new RectF();
            path.computeBounds(rectF, true);
            path.close();
            Region region = new Region();
            region.setPath(path, new Region((int)rectF.left, (int)rectF.top, (int)rectF.right, (int)rectF.bottom));
            mRegionList.add(region);
            canvas.drawPath(path, mRadiusStrokePaint);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        Log.i(TAG_CXM, "widthMode = " + widthMode + ", width = " + widthSize + ", heigtMode = " + heightMode + " height = " + heightSize + " --- EXACTLY = " + MeasureSpec.EXACTLY + " ---AT_MOST = " + MeasureSpec.AT_MOST + "----UNSPECIFIED = " + MeasureSpec.UNSPECIFIED);
        int width = 0, height = 0;
        if (widthSize > heightSize) {
            width = height = heightSize;
        }
        if (heightSize > widthSize) {
            width = height = widthSize;
        }
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.w(TAG_CXM, "w = " + w + ", h = " + h + ", oldW = " + oldw + ", oldH = " + oldh);
        mRadius = Math.min(w, h) / 2;
        mBaseRectF = new RectF(0, 0, mRadius * 2f, mRadius * 2f);
        mDeltaRadius = (int) (mRadius / 4f * 3f);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsDown = true;
                float x = event.getX();
                float y = event.getY();
                mDownPoint.x = (int) x;
                mDownPoint.y = (int) y;
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                mDownPoint.x = (int) event.getX();
                mDownPoint.y = (int) event.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsDown = false;
                mDownPoint.x = 0;
                mDownPoint.y = 0;
                invalidate();
                break;
        }
        Log.i(TAG_CXM, "pointX = " + mDownPoint.x + ", pointY = " + mDownPoint.y);
        return super.onTouchEvent(event);
    }
}
