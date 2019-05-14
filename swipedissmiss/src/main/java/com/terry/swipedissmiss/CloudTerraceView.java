package com.terry.swipedissmiss;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * Author:ChenXinming
 * Date:2019/05/14
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public class CloudTerraceView extends View {
    public CloudTerraceView(Context context) {
        this(context, null);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CloudTerraceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
