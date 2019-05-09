package com.terry.swipedissmiss;

import android.content.Context;
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

    public SwipeDissmissView(Context context) {
        this(context, null);
    }

    public SwipeDissmissView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDissmissView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAttacher = new Attacher(this);
    }

    public void setImageView(@DrawableRes int resId){
        Drawable drawable = getContext().getResources().getDrawable(resId);
        mAttacher.setImageView(drawable);
        setScaleType(ScaleType.MATRIX);
    }

}
