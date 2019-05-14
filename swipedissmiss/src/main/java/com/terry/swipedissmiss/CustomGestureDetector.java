package com.terry.swipedissmiss;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Author:ChenXinming
 * Date:2019/05/13
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public class CustomGestureDetector {

    private static final int INVALID_POINTER_ID = -1;
    private Context mContext;
    private OnCustomGestureListener mOnCustomGestureListener;
    private ScaleGestureDetector mGestureDetector;
    private float mMinimumVelocity;
    private float mTouchSlop;
    private int mActivePointerId;
    private VelocityTracker mVelocityTracker;
    private float mLastTouchX;
    private float mLastTouchY;
    private boolean mIsDraging;
    private int mActionPointerIndex;


    public CustomGestureDetector(Context context, OnCustomGestureListener
            listener) {
        this.mContext = context;
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mOnCustomGestureListener = listener;
        ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }
                if (scaleFactor >= 0) {
                    mOnCustomGestureListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        };
        mGestureDetector = new ScaleGestureDetector(context, scaleListener);
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            mGestureDetector.onTouchEvent(event);
            return processTouchEvent(event);
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private boolean processTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                //活动的手指id
                mActivePointerId = event.getPointerId(0);
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(event);
                }
                mLastTouchX = getActiveX(event);
                mLastTouchY = getActiveY(event);
                mIsDraging = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float activeX = getActiveX(event);
                float activeY = getActiveY(event);
                float delX = activeX - mLastTouchX, delY = activeY - mLastTouchY;
                if (!mIsDraging) {
                    mIsDraging = Math.sqrt(delX * delX + delY * delY) >= mTouchSlop;
                }
                if (mIsDraging) {
                    mOnCustomGestureListener.onDrag(mLastTouchX, mLastTouchY, event.getX(), event.getY(), delX, delY);
                    mLastTouchY = activeY;
                    mLastTouchX = activeX;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(event);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDraging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(event);
                        mLastTouchY = getActiveY(event);
                        mVelocityTracker.addMovement(event);
                        mVelocityTracker.computeCurrentVelocity(1000);
                        float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker.getYVelocity();
                        Log.i("cxm", "vX = " + vX + ", vy = " + vY);
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mOnCustomGestureListener.fling(mLastTouchX, mLastTouchY, -vX, -vY);
                        }
                    }
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                int actionIndex = event.getActionIndex();
                int pointerId = event.getPointerId(actionIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerId == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                }

                break;
        }
        mActionPointerIndex = event.findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId : 0);
        return true;
    }

    private float getActiveX(MotionEvent event) {
        try {
            return event.getX(mActionPointerIndex);
        } catch (Exception e) {
            return event.getX();
        }
    }

    private float getActiveY(MotionEvent event) {
        try {
            return event.getY(mActionPointerIndex);
        } catch (Exception e) {
            return event.getY();
        }
    }

    public boolean isScaling() {
        return mGestureDetector.isInProgress();
    }

    public boolean isDraging() {
        return mIsDraging;
    }

}
