package com.terry.swipe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.terry.swipedissmiss.SwipeDissmissView;

public class MainActivity extends AppCompatActivity {

    SwipeDissmissView mSwipeDissmissView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwipeDissmissView = findViewById(R.id.imageview);
        mSwipeDissmissView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mSwipeDissmissView.setImageView(R.drawable.test01);
    }
}
