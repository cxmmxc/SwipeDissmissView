package com.terry.swipedissmiss;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

/**
 * 作者：Terry.chen
 * 创建日期：2019/05/09
 * 邮箱：herewinner@163.com
 * 描述：仿微信图片的下拉透明消失组件
 */
public class SwipeDissmissView extends AppCompatImageView {

    private Attacher mAttacher;
    private ScaleType pendingScaleType;

    public SwipeDissmissView(Context context) {
        this(context, null);
    }

    public SwipeDissmissView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDissmissView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        super.setScaleType(ScaleType.MATRIX);
        mAttacher = new Attacher(this);
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }
    }

    public void setImageView(Drawable drawable) {
        super.setImageDrawable(drawable);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    public void setImageView(@DrawableRes int drawable) {
        super.setImageResource(drawable);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    public void setImageView(Bitmap drawable) {
        super.setImageBitmap(drawable);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (mAttacher == null) {
            pendingScaleType = scaleType;
        } else {
            mAttacher.setScaleType(scaleType);
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            mAttacher.update();
        }
        return changed;
    }

    public Attacher getAttacher() {
        return mAttacher;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mAttacher.setOnPhotoTapLisener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mAttacher.setOnViewTapListener(listener);
    }

    public void setOnMatrixChangeListener(OnMatrixChangeListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

}
