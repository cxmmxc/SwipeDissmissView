package com.terry.swipedissmiss;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Author:ChenXinming
 * Date:2019/05/14
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public class CloudTerraceView extends View {

    private int mRadius;

    private static final String TAG_CXM = "cxm";

    public CloudTerraceView(Context context) {
        this(context, null);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.getResources().obtainAttributes(attrs, R.styleable.CloudTerraceView);
        attributes.getColor(R.styleable.CloudTerraceView_circle_bg, context.getResources().getColor(R.color.ciclr_bg));
        initData();
    }

    private void initData() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        Log.i(TAG_CXM, "widthMode = " + widthMode + ", width = " + widthSize + ", heigtMode = " + heightMode + " height = " + heightSize + " --- EXACTLY = " + MeasureSpec.EXACTLY + " ---AT_MOST = " + MeasureSpec.AT_MOST + "----UNSPECIFIED = " + MeasureSpec.UNSPECIFIED);
        int width = 0,height = 0;
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
    }
}
