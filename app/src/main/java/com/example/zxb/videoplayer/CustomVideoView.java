package com.example.zxb.videoplayer;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by zouxiaobang on 17-9-18.
 */

public class CustomVideoView extends VideoView {

    int defaultWidth = 1000;
    int defaultHeight = 1000;

    public CustomVideoView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
//        defaultWidth = context.getResources().getDisplayMetrics().widthPixels;
//        defaultHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getDefaultSize(defaultWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);

        if (defaultWidth > 0 && defaultHeight > 0) {
            if (defaultWidth * height > width * defaultHeight) {
                height = width * defaultHeight / defaultWidth;
            } else if (defaultWidth * height < width * defaultHeight) {
                width = height * defaultWidth / defaultHeight;
            } else {

            }
        }

        setMeasuredDimension(width, height);
    }

    public void setWidthAndHeight(int width, int height){
        this.defaultWidth = width;
        this.defaultHeight = height;
        requestLayout();
    }
}
