package com.terry.swipedissmiss;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * 作者：Terry.CHen
 * 创建日期：2019/05/09
 * 邮箱：herewinner@163.com
 * 描述：TODO
 */
public class Attacher implements View.OnLayoutChangeListener, View.OnTouchListener {

    private ImageView mImageView;

    public Attacher(ImageView imageView) {
        this.mImageView = imageView;
        mImageView.setOnTouchListener(this);
        mImageView.addOnLayoutChangeListener(this);
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public void setImageView(Drawable drawable) {
        mImageView.setImageDrawable(drawable);
    }
}
