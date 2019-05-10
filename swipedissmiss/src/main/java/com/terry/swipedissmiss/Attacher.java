package com.terry.swipedissmiss;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;


/**
 * 作者：Terry.CHen
 * 创建日期：2019/05/09
 * 邮箱：herewinner@163.com
 * 描述：TODO
 */
public class Attacher implements View.OnLayoutChangeListener, View.OnTouchListener {

    private boolean mZoomEnabled = true;

    private ImageView mImageView;

    private OnPhotoTapListener mPhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private Matrix mSuppMatrix = new Matrix();
    private Matrix mBaseMatrix = new Matrix();
    private Matrix mDrawableMatrix = new Matrix();
    private OnMatrixChangeListener mMatrixChangeListener;
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;
    private RectF mDisplayRect = new RectF();


    public Attacher(ImageView imageView) {
        this.mImageView = imageView;
        imageView.setOnTouchListener(this);
        imageView.addOnLayoutChangeListener(this);
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
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
        Log.w("cxm", "scaleType --- " + mScaleType);
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
        return false;
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
            }
        }

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
}
