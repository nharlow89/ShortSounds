package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shampson on 5/7/15.
 */
public class ReverbCanvas extends View {


    private static final String TAG = "EffectCanvas";
    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path xAxis;
    private Path yAxis;
    Context context;
    private Paint linePaint;
    private Paint pointPaint;
    private float echo;
    private float decay;
    private final float RECTSIZE = 10f;
    private final int NONE = -1, TOUCH_POINT = 0;
    private int currentTouch;
    private RectF pointTouch;
    private boolean firstDraw;

    // Debug
//    private Paint debugPaint;
//    private List<PointF> debugPoints;

    private float mX, mY;

    public ReverbCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        firstDraw = true;

        // Path for x & y axis
        xAxis = new Path();
        yAxis = new Path();

        // Paint specs for line
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.WHITE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(7f);

        // Paint specs for points
        pointPaint =  new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.GREEN);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(30f);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);

        // Initialize echo & decay at default locations
        // These will be set by effect values w/ backend connected
        echo = 325;
        decay = 250;

        // Initialize touch locations for points
        pointTouch = new RectF(echo - RECTSIZE, decay - RECTSIZE,
                                echo + RECTSIZE, decay + RECTSIZE);
        currentTouch = NONE;
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstDraw) {
            // Draw the line between initial points
            drawAxis();
            firstDraw = false;
        }
        canvas.drawPath(xAxis, linePaint);
        canvas.drawPath(yAxis, linePaint);
        canvas.drawPoint(echo, decay, pointPaint);
    }

    // Helper method for intially drawing out the x & y axis
    private void drawAxis() {
        xAxis.moveTo(10, 600);
        yAxis.moveTo(10, 600);

        xAxis.lineTo(650, 600);
        yAxis.lineTo(10, 10);
    }

    // This is where we will handle controlling points
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            // First see which point user is touching
            case MotionEvent.ACTION_DOWN:
                if (pointTouch.contains(x, y)) {
                    currentTouch = TOUCH_POINT;
                }
                return true;
            // Drag points
            case MotionEvent.ACTION_MOVE:
                if (currentTouch == TOUCH_POINT) {
                        echo = x;
                        decay = y;
                        invalidate();
                        return true;
                }
                return false;
            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                if (currentTouch == TOUCH_POINT) {
                        echo = x;
                        decay = y;
                        updateTouchArea(echo, decay, x, y);
                        invalidate();
                        currentTouch = NONE;
                        return true;
                }
                return false;
        }
        return true;
    }

    // This is for setting the echo & decay values that will come
    // from the database
    public void setReverbVals(float echo, float decay) {
        this.echo = echo;
        this.decay = decay;
    }

    public List<Float> getReverbVals() {
        List<Float> retList = new ArrayList<Float>();
        retList.add(echo);
        retList.add(decay);
        return retList;
    }


    // Sets point as well as updates touch area
    private void updateTouchArea(float echo, float decay, float x, float y) {
        pointTouch.set(echo - RECTSIZE, decay - RECTSIZE,
                echo + RECTSIZE, decay + RECTSIZE);
    }
}