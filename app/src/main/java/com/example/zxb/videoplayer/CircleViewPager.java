package com.example.zxb.videoplayer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by zouxiaobang on 17-9-20.
 */

public class CircleViewPager extends View implements ViewPager.OnPageChangeListener {
    private Context mContext;
    private ViewPager mViewPager;

    private int dX,dY;
    private float mWidth;
    private float mPageOffset;
    private int mCurrentPage;
    private int mScrollState;
    private int mRadius;
    private int mCount;
    private Paint mMovePoint, mPagePoint;

    public CircleViewPager(Context context) {
        super(context);
        init(context);
    }

    public CircleViewPager(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleViewPager(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleViewPager(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        mContext = context;

        mRadius = 8;
        dX = 4*mRadius;
        dY = -1;
        mWidth = getResources().getDisplayMetrics().widthPixels;

        Paint pagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pagePaint.setAntiAlias(true);
        pagePaint.setDither(true);
        pagePaint.setColor(getResources().getColor(R.color.colorPagePoint));
        pagePaint.setStyle(Paint.Style.FILL);
        mPagePoint = pagePaint;

        Paint movePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        movePaint.setAntiAlias(true);
        movePaint.setDither(true);
        movePaint.setColor(getResources().getColor(R.color.colorMovePoint));
        movePaint.setStyle(Paint.Style.FILL);
        mMovePoint = movePaint;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        if (mViewPager == null){
            return;
        }
        if (mCount == 0){
            return;
        }

        float firstOffset = (mWidth - (dX*(mCount-1) + 2))/2;
        if (dY != -1){
            for (int i = 0;i < mCount;i ++){
                canvas.drawCircle(firstOffset+(i*dX)+mRadius, dY, mRadius, mPagePoint);
            }
        }

        float x = mCurrentPage*dX+mPageOffset*dX+firstOffset+mRadius;
        if (x > (mCount-1)*dX+firstOffset+mRadius){
            x = (mCount-1)*dX+firstOffset+mRadius - mPageOffset*(mCount-1)*dX;
        } else if (x < firstOffset+mRadius){
            x = (mCount-1)*dX+firstOffset+mRadius + mPageOffset*(mCount-1)*dX;
        }
        canvas.drawCircle(x, dY, mRadius, mMovePoint);

    }

    public void setViewPager(int count, ViewPager viewPager, int dY){
        this.mViewPager = viewPager;
        this.mCount = count;
        this.dY = dY;

        mViewPager.setOnPageChangeListener(this);
        invalidate();
    }

    int lastOffset = 0;
    boolean isLeft = false;
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mCount != 0)
            mCurrentPage = position%mCount;
        mPageOffset = positionOffset;
        if (positionOffsetPixels != 0){
            if (lastOffset >= positionOffsetPixels){
                isLeft = true;
            } else {
                isLeft = false;
            }
            lastOffset = positionOffsetPixels;
        }

        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
//        mCurrentPage = position%mCount;
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE){
            mCurrentPage = position;
            invalidate();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }
}
