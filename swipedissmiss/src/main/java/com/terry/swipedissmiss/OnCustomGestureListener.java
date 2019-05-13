package com.terry.swipedissmiss;

/**
 * Author:ChenXinming
 * Date:2019/05/13
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public interface OnCustomGestureListener {
    void onDrag(float downX, float downY, float pointX, float pointY, float dx, float dy);

    void onScale(float scaleFactor, float focusX, float focusY);

    void fling(float startX, float startY, float velocityX, float velocityY);
}
