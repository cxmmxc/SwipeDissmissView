package com.terry.swipedissmiss;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.OverScroller;
import android.widget.Scroller;


/**
 * 作者：Terry.CHen
 * 创建日期：2019/05/09
 * 邮箱：herewinner@163.com
 * 描述：TODO
 */
public class Attacher implements View.OnLayoutChangeListener, View.OnTouchListener, OnCustomGestureListener {

    private static final float DEFAULT_MAX_SCALE = 3.0f;
    private static final float DEFAULT_MID_SCALE = 1.75f;
    private static final float DEFAULT_MIN_SCALE = 1.0f;
    private static final int DEFAULT_ZOOM_DURATION = 200;
    private boolean mZoomEnabled = true;

    private ImageView mImageView;

    private OnPhotoTapListener mPhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private View.OnClickListener mOnClickListener;
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mBaseMatrix = new Matrix();
    private Matrix mDrawableMatrix = new Matrix();
    private OnMatrixChangeListener mMatrixChangeListener;
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;
    private RectF mDisplayRect = new RectF();
    private CustomGestureDetector mCustomGestureDetector;
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float[] mMatrixValues = new float[9];

    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;

    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private boolean mBlockParentIntercept;
    private GestureDetector mGestureDetector;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private FlingRunnable mFlingRunnable;


    public Attacher(ImageView imageView) {
        this.mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);
        mCustomGestureDetector = new CustomGestureDetector(imageView.getContext(), this);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mImageView);
                }
                RectF displayRect = getDisplayRect();
                float x = e.getX(), y = e.getY();
                if (mViewTapListener != null) {
                    mViewTapListener.onPhotoTap(mImageView, x, y);
                }
                if (displayRect != null) {
                    if (displayRect.contains(x, y)) {
                        float percentX = (x - displayRect.left) / displayRect.width();
                        float percentY = (y - displayRect.top) / displayRect.height();
                        if (null != mPhotoTapListener) {
                            mPhotoTapListener.onPhotoTap(mImageView, percentX, percentY);
                        }
                        return true;
                    } else {
                        //在photo之外的点击，如果需要可以设置回调
                    }
                }

                return false;
            }
        });
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Log.i("cxm", "onLayoutChange");
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix();
        }
    }

    private void updateBaseMatrix() {
        Drawable drawable = mImageView.getDrawable();
        if (drawable == null) {
            return;
        }
        float viewWidth = mImageView.getWidth();
        float viewHeight = mImageView.getHeight();
        float width = drawable.getIntrinsicWidth();
        float height = drawable.getIntrinsicHeight();
        mBaseMatrix.reset();
        float scaleX = viewWidth / width;
        float scaleY = viewHeight / height;
        Log.w("cxm", "scaleType --- " + mScaleType + " , viewWidth = " + viewWidth + ", viewheight = " + viewHeight + ", width = " + width + ", height = " + height);
        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - width) / 2f, (viewHeight - height) / 2f);
        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float maxScale = Math.max(scaleX, scaleY);
            mBaseMatrix.postScale(maxScale, maxScale);
            mBaseMatrix.postTranslate((viewWidth - width * maxScale) / 2f, (viewHeight - height * maxScale) / 2f);
        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float minScale = Math.min(1.0f, Math.min(scaleX, scaleY));
            mBaseMatrix.postScale(minScale, minScale);
            mBaseMatrix.postTranslate((viewWidth - width * minScale) / 2f, (viewHeight - height * minScale) / 2f);
        } else {
            RectF tempSrc = new RectF(0, 0, width, height);
            RectF tempDsc = new RectF(0, 0, viewWidth, viewHeight);
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(tempSrc, tempDsc, Matrix.ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(tempSrc, tempDsc, Matrix.ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(tempSrc, tempDsc, Matrix.ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(tempSrc, tempDsc, Matrix.ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;
        if (mZoomEnabled && ((ImageView) v).getDrawable() != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //请求父类不要拦截事件
                    ViewParent parent = v.getParent();
                    if (null != parent) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    cancelFling();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (getScale() < mMinScale) {
                        RectF rect = getDisplayRect();
                        if (null != rect) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMinScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    } else if (getScale() > mMaxScale) {
                        RectF rect = getDisplayRect();
                        if (null != rect) {
                            v.post(new AnimatedZoomRunnable(getScale(), mMaxScale, rect.centerX(), rect.centerY()));
                            handled = true;
                        }
                    }
                    break;
            }
            if (null != mCustomGestureDetector) {
                boolean draging = mCustomGestureDetector.isDraging();
                boolean scaling = mCustomGestureDetector.isScaling();
                handled = mCustomGestureDetector.onTouchEvent(event);
                boolean didnotScale = !scaling && !mCustomGestureDetector.isScaling();
                boolean didnotDrag = !draging && !mCustomGestureDetector.isDraging();
                mBlockParentIntercept = didnotDrag && didnotScale;
            }
            if (null != mGestureDetector && mGestureDetector.onTouchEvent(event)) {
                handled = true;
            }
        }
        return handled;
    }

    private class AnimatedZoomRunnable implements Runnable{

        float currentScale;
        float targetScale;
        float focusX;
        float focusY;
        long mStartTime;

        public AnimatedZoomRunnable(float currentScale, float targetScale, float focusX, float focusY) {
            this.currentScale = currentScale;
            this.targetScale = targetScale;
            this.focusX = focusX;
            this.focusY = focusY;
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            float interpolate = interpolate();
            float scale = currentScale + interpolate * (targetScale - currentScale);
            float deltaScale = scale / getScale();
            onScale(deltaScale, focusX, focusY);
            if (interpolate < 1f) {
                mImageView.postOnAnimation(this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(t, 1.0f);
            t = mInterpolator.getInterpolation(t);
            return t;
        }
    }

    private class FlingRunnable implements Runnable{

        private final OverScroller mScroller;
        private int mCurrentX;
        private int mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            RectF displayRect = getDisplayRect();
            if (null == displayRect) {
                return;
            }
            int startX = Math.round(-displayRect.left);
            int minX, maxX, minY, maxY;
            if (viewWidth < displayRect.width()) {
                minX = 0;
                maxX = Math.round(displayRect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            int startY = Math.round(-displayRect.top);
            if (viewHeight < displayRect.height()) {
                minY = 0;
                maxY = Math.round(displayRect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            Log.i("cxm", "startX = " + startX + ", startY = " + startY + ", minX = " + minX + ", minY = " + minY + ", maxX = " + maxX + ", maxY =" + maxY);
            mCurrentX = startX;
            mCurrentY = startY;
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return;
            }
            if (mScroller.computeScrollOffset()) {
                int currX = mScroller.getCurrX();
                int currY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - currX, mCurrentY - currY);
                checkAndDisplayMatrix();
                mCurrentX = currX;
                mCurrentY = currY;
                mImageView.postOnAnimation(this);
            }
        }
    }

    /**
     * 取消fling
     */
    private void cancelFling() {
        if (mFlingRunnable != null) {
            mFlingRunnable.cancelFling();
        }
    }

    /**
     * 进行更新操作
     */
    public void update() {
        if (mZoomEnabled) {
            updateBaseMatrix();
        } else {
            resetMatrix();
        }
    }

    private void resetMatrix() {
        mSuppMatrix.reset();
        setImageViewMatrix(getDrawableMatrix());
        checkMatrixBounds();
    }
    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawableMatrix());
    }

    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            mImageView.setImageMatrix(getDrawableMatrix());
        }
    }

    private boolean checkMatrixBounds() {
        RectF rectF = getDisplayRect(getDrawableMatrix());
        if(rectF == null)
            return false;
        final float height = rectF.height(), width = rectF.width();
        float deltaX = 0, deltaY = 0;
        final float viewHeight = getImageViewHeight(mImageView);
        final float viewWidth = getImageViewWidth(mImageView);
        if (height < viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rectF.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - rectF.bottom;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rectF.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        }else if (rectF.top > 0) {
            deltaY = -rectF.top;
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
        } else if (rectF.bottom < viewHeight) {
            deltaY = viewHeight - rectF.bottom;
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
        }else{
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }

        if (width < viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rectF.left;
                    break;
                case FIT_END:
                    deltaX = -rectF.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rectF.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rectF.left > 0) {
            deltaX = -rectF.left;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
        } else if (rectF.right < viewWidth) {
            deltaX = viewWidth - rectF.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        Log.i("cxm", "viewWidth = " + viewWidth + ", width = " + width + ", viewHeight = " + viewHeight + ", height = " + height + ", deltaX = " + deltaX + ", deltaY = " + deltaY + ", rect.Top = " + rectF.top);
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private float getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private float getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private RectF getDisplayRect(Matrix drawableMatrix) {
        Drawable drawable = mImageView.getDrawable();
        if (drawable != null) {
            mDisplayRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawableMatrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    private void setImageViewMatrix(Matrix drawableMatrix) {
        mImageView.setImageMatrix(drawableMatrix);
    }

    private Matrix getDrawableMatrix() {
        mDrawableMatrix.set(mBaseMatrix);
        mDrawableMatrix.postConcat(mSuppMatrix);
        return mDrawableMatrix;
    }

    public void setOnPhotoTapLisener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangeListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setScaleType(ScaleType scaleType) {
        if (Util.isSupportScaleType(scaleType) && mScaleType != scaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    @Override
    public void onDrag(float downX, float downY, float pointX, float pointY, float dx, float dy) {
        if (mCustomGestureDetector.isScaling()) {
            return;
        }
        RectF displayRect = getDisplayRect();
        if (null != displayRect ) {
            mSuppMatrix.postTranslate(dx, dy);
            checkAndDisplayMatrix();
        }
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY) {
        if (getScale() < mMaxScale || scaleFactor < 1f) {
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkAndDisplayMatrix();
        }
    }

    private float getScale() {
        return (float) Math.sqrt(Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    private float getValue(Matrix matrix, int valueIndex) {
         matrix.getValues(mMatrixValues);
        return mMatrixValues[valueIndex];
    }

    @Override
    public void fling(float startX, float startY, float velocityX, float velocityY) {
        mFlingRunnable = new FlingRunnable(mImageView.getContext());
        mFlingRunnable.fling((int)getImageViewWidth(mImageView), (int)getImageViewHeight(mImageView), (int)velocityX, (int)velocityY);
        mImageView.post(mFlingRunnable);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.mOnClickListener = listener;
    }
}
