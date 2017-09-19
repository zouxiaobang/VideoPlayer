package com.example.zxb.videoplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by zouxiaobang on 17-9-19.
 */

public class CircleProgress extends View {
    private int mRadius = 100;
    private int mProgressRadius = 80;
    private Paint mCirclePaint = new Paint();
    private Paint mHintPaint = new Paint();
    private Paint mProgressPaint = new Paint();
    private int mWidth, mHeight;
    private int mProgress = 0;
    private Bitmap mBitmap;

    public CircleProgress(Context context) {
        super(context);
        init(context);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        setBackgroundColor(Color.parseColor("#44000000"));

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.parseColor("#bb000000"));
        paint.setStyle(Paint.Style.FILL);
        mCirclePaint = paint;

        Paint hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setAntiAlias(true);
        hintPaint.setDither(true);
        hintPaint.setColor(Color.parseColor("#ACACAC"));
        hintPaint.setStyle(Paint.Style.STROKE);
        hintPaint.setStrokeWidth(10);
        mHintPaint = hintPaint;

        Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setAntiAlias(true);
        progressPaint.setDither(true);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(10);
        progressPaint.setColor(Color.parseColor("#5f78ff"));
        mProgressPaint = progressPaint;

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sound);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        canvas.drawCircle(mWidth/2, mHeight/2, mRadius, mCirclePaint);
        canvas.drawCircle(mWidth/2, mHeight/2, mProgressRadius, mHintPaint);
        RectF rectF = new RectF(mWidth/2-mProgressRadius, mHeight/2-mProgressRadius,
                mWidth/2+mProgressRadius, mHeight/2+mProgressRadius);
        canvas.drawArc(rectF, -90, mProgress, false, mProgressPaint);

        RectF rectF1 = new RectF(mWidth/2-30, mHeight/2-30,
                mWidth/2+30, mHeight/2+30);
        canvas.drawBitmap(mBitmap, null, rectF1, mProgressPaint);
    }

    public void setProgress(int progress){
        this.mProgress = progress;
        invalidate();
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap = bitmap;
        invalidate();
    }
}
