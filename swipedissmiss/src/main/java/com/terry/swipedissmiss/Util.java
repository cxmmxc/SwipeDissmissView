package com.terry.swipedissmiss;

import android.widget.ImageView;

/**
 * Author:ChenXinming
 * Date:2019/05/10
 * Email:chenxinming@antelop.cloud
 * Description:
 */
public class Util {
    public static boolean isSupportScaleType(ImageView.ScaleType scaleType) {
        if (scaleType == null) {
            return false;
        }
        switch (scaleType) {
            case MATRIX:
            throw new IllegalStateException("Unsupport ScaleType.MATRIX");
        }
        return true;
    }
}
