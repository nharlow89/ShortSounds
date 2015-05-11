package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shampson on 5/7/15.
 */
public class ReverbCanvas extends View {


    private static final String TAG = "EffectCanvas";
    private Path xAxis;
    private Path yAxis;
    private Path linePath;
    private Paint linePaint;
    private Paint pointPaint;
    private PointF left;
    private PointF right;

    private PointControl point;
    private int YMIN, YMAX, XMIN,XMAX;

    private final int MARGIN = 10;
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
        firstDraw = true;

        // Path for y & x axis
        xAxis = new Path();
        yAxis = new Path();
        linePath = new Path();

        linePaint = new Paint();
        pointPaint =  new Paint();

        currentTouch = NONE;

        left = new PointF();
        right = new PointF();
    }

    void setStyles() {
        // Paint specs for line
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLUE);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(Math.max(getMeasuredWidth() / 50f, 20f));

        // Paint specs for points
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(getMeasuredWidth() / 16f);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);
    }


    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstDraw) {
            // Draw the line between initial points
            setStyles();
            setUpGraph();
            firstDraw = false;
        }
        canvas.drawPath(xAxis, linePaint);
        canvas.drawPath(yAxis, linePaint);
        canvas.drawPoint(point.x, point.y, pointPaint);
        canvas.drawPoint(left.x, left.y, pointPaint);
        canvas.drawPoint(right.x, right.y, pointPaint);
        canvas.drawPath(linePath, linePaint);
    }

    // Helper method for intially drawing out the y & x axis
    private void setUpGraph() {
        YMAX = getMeasuredHeight() / 4;
        YMIN = getMeasuredHeight() * 3 / 4;
        XMIN = MARGIN;
        XMAX = getMeasuredWidth() - MARGIN;

        xAxis.moveTo(MARGIN, YMIN);
        yAxis.moveTo(MARGIN, YMIN);
        xAxis.lineTo(XMAX, YMIN);
        yAxis.lineTo(MARGIN, YMAX);

        left.x = XMIN;
        right.x = XMAX;

        point = new PointControl();
        setLine();
    }

    private void setLine() {
        float h0 = YMIN - (Math.abs(point.y - YMIN) * 3f  / 4);
        float deltaH = 16f * XMIN / point.x;
//        Log.d(TAG, "xmin / point.x: " + (float)XMIN / point.x);
//        Log.d(TAG, "delta H: " + deltaH);
        float h1 = h0 + (XMAX - XMIN) * deltaH;

        if (h1 > YMIN) {
            h1 = YMIN;
            right.x = (YMIN - h0) / deltaH;
        } else {
            right.x = XMAX;
        }

        left.y = h0;
        right.y = h1;

        linePath.reset();
        linePath.moveTo(left.x, left.y);
        linePath.quadTo(point.x, point.y , right.x, right.y);
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
                break;
            // Drag points
            case MotionEvent.ACTION_MOVE:
                if (currentTouch == TOUCH_POINT) {
                    point.update(x, y);
                    setLine();
                    invalidate();
                }
                break;
            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                if (currentTouch == TOUCH_POINT) {
                    point.update(x, y);
                        invalidate();
                        currentTouch = NONE;
                }
                break;
        }
        return true;
    }

//    // This is for setting the x & y values that will come
//    // from the database
//    public void setReverbVals(float x, float y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    public List<Float> getReverbVals() {
//        List<Float> retList = new ArrayList<Float>();
//        retList.add(x);
//        retList.add(y);
//        return retList;
//    }
//


    class PointControl {
        private final float RECTSIZE;
        private float x;
        private float y;

        PointControl() {
            RECTSIZE = getMeasuredWidth() / 16f;
            // Initialize x & y at default locations
            // These will be set by effect values w/ backend connected
            x = getMeasuredWidth() / 2;
            y = getMeasuredHeight() / 2;

            // Initialize touch locations for points
            pointTouch = new RectF(x - RECTSIZE, y - RECTSIZE,
                                   x + RECTSIZE, y + RECTSIZE);
        }

        void update(float x, float y) {
            if (x > XMIN + 4 * MARGIN && x < XMAX - 4 * MARGIN)
                this.x = x;
            if (y  < YMIN - 4 * MARGIN && y  > YMAX + 4 * MARGIN)
                this.y = y;

            // Initialize touch locations for points
            pointTouch.set(this.x - RECTSIZE, this.y - RECTSIZE,
                    this.x + RECTSIZE, this.y + RECTSIZE);
        }

    }
}