package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for displaying a canvas view for the equalizer effect
 * Created by shampson on 5/7/15.
 */
public class EQCanvas extends View {


    private static final String TAG = "EffectCanvas";
    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path linePath;
    Context context;
    private Paint linePaint;
    private Paint pointPaint;
    private PointF basePoint;
    private PointF lowPoint;
    private PointF midPoint;
    private PointF highPoint;
    private PointF endPoint;
    private PointF[] allPoints;
    private final float RECTSIZE = 10f;
    private final int NONE = -1, TOUCH_BASE = 0, TOUCH_LOW = 1,
                      TOUCH_MID = 2, TOUCH_HIGH = 3, TOUCH_END = 4;
    private int currentTouch;
    private RectF basePointTouch;
    private RectF lowPointTouch;
    private RectF midPointTouch;
    private RectF highPointTouch;
    private RectF endPointTouch;
    private boolean firstDraw;

    // Debug
    private Paint debugPaint;
    private List<PointF> debugPoints;

    private float mX, mY;

    public EQCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        firstDraw = true;

        // Path for eq Line
        linePath = new Path();

        // Paint specs for line
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(7f);

        // Paint specs for points
        pointPaint =  new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(20f);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);

        // Debug paint
        debugPaint = new Paint();
        debugPaint.setAntiAlias(true);
        debugPaint.setColor(Color.MAGENTA);
        debugPaint.setStyle(Paint.Style.STROKE);
        debugPaint.setStrokeWidth(15f);
        debugPaint.setStrokeCap(Paint.Cap.ROUND);

        // Initialize 2 points at random location
        // These will be set by effect values w/ backend connected
        allPoints = new PointF[5];
        basePoint = new PointF(10, 450);
        allPoints[0] = basePoint;
        lowPoint = new PointF(158, 450);
        allPoints[1] = lowPoint;
        midPoint = new PointF(325, 450);
        allPoints[2] = midPoint;
        highPoint = new PointF(490, 450);
        allPoints[3] = highPoint;
        endPoint = new PointF(655, 450);
        allPoints[4] = endPoint;

        // Initialize touch locations for points
        basePointTouch = new RectF(basePoint.x - RECTSIZE, basePoint.y - RECTSIZE,
                                    basePoint.x + RECTSIZE, basePoint.y + RECTSIZE);
        lowPointTouch = new RectF(lowPoint.x - RECTSIZE, lowPoint.y - RECTSIZE,
                                    lowPoint.x + RECTSIZE, lowPoint.y + RECTSIZE);
        midPointTouch = new RectF(midPoint.x - RECTSIZE, midPoint.y - RECTSIZE,
                                    midPoint.x + RECTSIZE, midPoint.y + RECTSIZE);
        highPointTouch = new RectF(highPoint.x - RECTSIZE, highPoint.y - RECTSIZE,
                                    highPoint.x + RECTSIZE, highPoint.y + RECTSIZE);
        endPointTouch = new RectF(endPoint.x - RECTSIZE, endPoint.y - RECTSIZE,
                                    endPoint.x + RECTSIZE, endPoint.y + RECTSIZE);
        currentTouch = NONE;

        debugPoints = new ArrayList<PointF>();
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
            drawPath();
            firstDraw = false;
        }

        // draw the line between points
        canvas.drawPath(linePath, linePaint);
        // draw the two points
        canvas.drawPoint(basePoint.x, basePoint.y, pointPaint);
        canvas.drawPoint(lowPoint.x, lowPoint.y, pointPaint);
        canvas.drawPoint(midPoint.x, midPoint.y, pointPaint);
        canvas.drawPoint(highPoint.x, highPoint.y, pointPaint);
        canvas.drawPoint(endPoint.x, endPoint.y, pointPaint);


        if (debugPoints.size() != 0) {
            for (int i = 0; i < debugPoints.size(); i++) {
                PointF point = debugPoints.get(i);
                canvas.drawPoint(point.x, point.y, debugPaint);
            }
        }
    }

    // Private helper for drawing the line between all the points
    private void drawPath() {
        linePath.reset();
        PointF prevPoint = allPoints[0];
        float lastX = prevPoint.x;
        float lastY = prevPoint.y;
        linePath.moveTo(lastX, lastY);
        for (int i = 0; i < allPoints.length; i++){
            if (i >= 0){
                PointF point = allPoints[i];

                if (i == 0) {
                    PointF next = allPoints[i + 1];
                    point.dx = ((next.x - point.x) / 3);
                    point.dy = ((next.y - point.y) / 3);
                } else if (i == allPoints.length - 1) {
                    PointF prev = allPoints[i - 1];
                    point.dx = ((point.x - prev.x) / 3);
                    point.dy = ((point.y - prev.y) / 3);
                } else {
                    PointF next = allPoints[i + 1];
                    PointF prev = allPoints[i - 1];
                    point.dx = ((next.x - prev.x) / 5);
                    point.dy = ((next.y - prev.y) / 5);
                }
            }
        }

        debugPoints.clear();
        boolean first = true;
        for(int i = 0; i < allPoints.length; i++){
            PointF point = allPoints[i];
            if(first){
                first = false;
                linePath.moveTo(point.x, point.y);
            }
            else{
                PointF prev = allPoints[i - 1];
                PointF debugPoint1 = new PointF((int) (prev.x + prev.dx) , (int) (prev.y + prev.dy));
                PointF debugPoint2 = new PointF((int) (point.x - point.dx), (int) (point.y - point.dy));
                debugPoints.add(debugPoint1);
                debugPoints.add(debugPoint2);
                linePath.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy, point.x, point.y);
            }
        }
    }

    // This is where we will handle controlling points
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            // First see which point user is touching
            case MotionEvent.ACTION_DOWN:
                if (basePointTouch.contains(x, y)) {
                    currentTouch = TOUCH_BASE;
                } else if (lowPointTouch.contains(x, y)) {
                    currentTouch = TOUCH_LOW;
                    Log.d(TAG, "User touched low point");
                } else if (midPointTouch.contains(x, y)) {
                    currentTouch = TOUCH_MID;
                    Log.d(TAG, "User touched mid point");
                } else if (highPointTouch.contains(x, y)) {
                    currentTouch = TOUCH_HIGH;
                    Log.d(TAG, "User touched high point");
                } else if (endPointTouch.contains(x, y)) {
                    currentTouch = TOUCH_END;
                } else {
                    return false;
                }
                return true;
            // Drag points
            case MotionEvent.ACTION_MOVE:
                switch (currentTouch) {
                    case TOUCH_BASE:
                        setPoint(basePoint, y);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_LOW:
                        setPoint(lowPoint, y);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_MID:
                        setPoint(midPoint, y);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_HIGH:
                        setPoint(highPoint, y);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_END:
                        setPoint(endPoint, y);
                        drawPath();
                        invalidate();
                        return true;
                }
                return false;
            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                switch (currentTouch) {
                    case TOUCH_BASE:
                        setPointUpdateArea(basePoint, y, basePointTouch);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_LOW:
                        setPointUpdateArea(lowPoint, y, lowPointTouch);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_MID:
                        setPointUpdateArea(midPoint, y, midPointTouch);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_HIGH:
                        setPointUpdateArea(highPoint, y, highPointTouch);
                        drawPath();
                        invalidate();
                        return true;
                    case TOUCH_END:
                        setPointUpdateArea(endPoint, y, endPointTouch);
                        drawPath();
                        invalidate();
                        return true;
                    }
                return false;
        }
        return true;
    }

    public void setEQVals(List<PointF> points) {
        if (points.size() != 5) {
            // User did not put enough EQ values in
            return;
        }
        basePoint = points.get(0);
        lowPoint = points.get(1);
        midPoint = points.get(2);
        highPoint = points.get(3);
        endPoint = points.get(4);
    }

    public List<PointF> getEQVals() {
        List<PointF> retList = new ArrayList<PointF>();
        retList.add(basePoint);
        retList.add(lowPoint);
        retList.add(midPoint);
        retList.add(highPoint);
        retList.add(endPoint);
        return retList;
    }

    // Sets point as well as updates touch area
    private void setPointUpdateArea(PointF point, float y, RectF rect) {
        setPoint(point, y);
        rect.set(point.x - RECTSIZE, point.y - RECTSIZE,
                point.x + RECTSIZE, point.y + RECTSIZE);
        currentTouch = NONE;
    }

    // sets the point & invalidates
    private void setPoint(PointF point, float y) {
        point.y = y;
    }

    class PointF {
        float x, y;
        float dx, dy;


        public PointF(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}