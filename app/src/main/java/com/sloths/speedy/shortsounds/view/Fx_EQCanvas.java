package com.sloths.speedy.shortsounds.view;

import android.content.Context;
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

import com.sloths.speedy.shortsounds.controller.EQEffectController;
import com.sloths.speedy.shortsounds.model.EqEffect;

import java.util.Arrays;
import java.util.List;

/**
 * This class is for displaying a canvas view for the equalizer effect
 * Created by shampson on 5/7/15.
 */
public class Fx_EQCanvas extends View {


    private static final String TAG = "EffectCanvas";

    public enum PointInit {LO, HI, NONE};

    private Path linePath;
    private Paint linePaint;
    private Paint pointPaint;
    private EQPointF left;
    private EQPointF right;
    private PointGroup lo;
    private PointGroup hi;
    private List<EQPointF> path;
    private EQEffectController controller;



    private PointInit currentGroup;
    private boolean firstDraw;

    /**
     * Constructor for setting up the canvas
     * @param c
     * @param attrs
     */
    public Fx_EQCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        firstDraw = true;

        // Path for eq Line
        linePath = new Path();
        linePaint = new Paint();
        pointPaint =  new Paint();

        // Initialize touch locations for points
        currentGroup = PointInit.NONE;

        repInvariant();
    }

    /**
     * This resets the points to their initial states
     * for the view to be reset
     */
    private void resetPoints() {
        left = new EQPointF(0, getMeasuredHeight() / 2);
        right = new EQPointF(getMeasuredWidth(), getMeasuredHeight() / 2);
        lo = new PointGroup(PointInit.LO);
        hi = new PointGroup(PointInit.HI);
        setPath();
        invalidate();
    }

    /**
     * Sets the paint styles for the line & points
     */
    private void setStyles() {
        // Paint specs for line
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeWidth(Math.max(getMeasuredWidth() / 50f, 20f));

        // Paint specs for points
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.RED);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeWidth(getMeasuredWidth() / 12f);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /** On draw is used to actually draw on the canvas
    * It's main purpose is to draw the new two points and line
    */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstDraw) {
            // Draw the line between initial points
            setStyles();
            setPath();
            firstDraw = false;
        }

        // draw the line between points
        canvas.drawPath(linePath, linePaint);
        // draw the two points
        if (lo.enabled) {
            pointPaint.setColor(Color.RED);
            canvas.drawPoint(lo.centerPoint.x, lo.centerPoint.y, pointPaint);
        }
        if (hi.enabled) {
            pointPaint.setColor(Color.BLUE);
            canvas.drawPoint(hi.centerPoint.x, hi.centerPoint.y, pointPaint);
        }
    }

    /**
     * This sets the path to be a curved line ready to be drawn
     */
    private void setPath() {
        updatePath();
        linePath.reset();
        // Set dx/dy values for help w/ curve
        for (int i = 0; i < path.size(); i++){
            if (i >= 0){
                EQPointF point = path.get(i);
                if (i == 0) {
                    EQPointF next = path.get(i + 1);
                    point.dx = ((next.x - point.x) / 3);
                    point.dy = ((next.y - point.y) / 3);
                } else if (i == path.size() - 1) {
                    EQPointF prev =path.get(i - 1);
                    point.dx = ((point.x - prev.x) / 3);
                    point.dy = ((point.y - prev.y) / 3);
                } else {
                    EQPointF next = path.get(i + 1);
                    EQPointF prev = path.get(i - 1);
                    point.dx = ((next.x - prev.x) / 5);
                    point.dy = ((next.y - prev.y) / 5);
                }
            }
        }
        // Set starter point of line
        EQPointF prev = path.get(0);
        EQPointF point;
        linePath.moveTo(prev.x, prev.y);
        // Draws curved line w/ cubic function
        for(int i = 1; i < path.size(); i++){
            point = path.get(i);
            linePath.cubicTo(prev.x + prev.dx, prev.y + prev.dy,
                             point.x - point.dx, point.y - point.dy, point.x, point.y);
            prev = point;
        }
    }

    /**
     * This updates the main points held in the path
     */
    private void updatePath() {
        PointGroup leftGroup = lo;
        PointGroup rightGroup = hi;

        // Used for if the lo & hi points cross
        if (lo.centerPoint.x > hi.centerPoint.x) {
            leftGroup = hi;
            rightGroup = lo;
        }
        EQPointF p = new EQPointF((int) ((leftGroup.rightPoint.x + rightGroup.leftPoint.x) / 2), (int) leftGroup.rightPoint.y);
        path = Arrays.asList(left, leftGroup.leftPoint, leftGroup.centerPoint, p, rightGroup.centerPoint, rightGroup.rightPoint, right);
    }


    /**
     * This is where we will handle controlling points
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        switch (action) {
            // First see which point user is touching
            case MotionEvent.ACTION_DOWN:
                if (currentGroup == PointInit.NONE) {
                    if (lo.rect.contains(x, y)) {
                        currentGroup = PointInit.LO;
                    } else if (hi.rect.contains(x, y)) {
                        currentGroup = PointInit.HI;

                    }
                } else if (!lo.rect.contains(x, y) && !hi.rect.contains(x, y)) {
                    currentGroup = PointInit.NONE;
                }
                break;
            // Drag points
            case MotionEvent.ACTION_MOVE:
                if (currentGroup == PointInit.LO) {
                    lo.set(x, y);
                } else if (currentGroup == PointInit.HI) {
                    hi.set(x, y);
                }
                if (currentGroup == PointInit.LO || currentGroup == PointInit.HI) {
                    setPath();
                    invalidate();
                    controlEffect();
                }
                break;

            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                if (currentGroup != PointInit.NONE) {
                    currentGroup = PointInit.NONE;
                    controlEffect();
                }
                break;
        }
        return true;
    }

    /**
     * Uses the controller to update the backend model
     * This will let the backend change its parameters for controlling
     * the effect
     */
    private void controlEffect() {
        if (controller != null) {
            controller.updateEffectValues(new PointF[]{getPointA(), getPointB()});
        }
    }

    /**
     * Gives the lo center point
     * @return
     */
    public PointF getPointA() {
        return getBand(lo.centerPoint);
    }

    /**
     * Gives the high center point
     * @return
     */
    public PointF getPointB() {
        return getBand(hi.centerPoint);
    }

    /**
     * Helper method for turning the points values in terms of percentages
     * @param point
     * @return
     */
    private PointF getBand(EQPointF point) {
        float midY = (float) getMeasuredHeight() / (float) 2;

        // lo point to return as percentage
        float loXPercent = point.x / (float) getMeasuredWidth();
        float loYPercent = (midY - point.y) / midY;
        return new PointF(loXPercent, loYPercent);
    }

    /**
     * Sets the new values for the points
     * @param points
     */
    public void setValues(PointF[] points) {
        // Nothing pulled from model --> Set to default
        if (points == null || points.length == 0 ||
                (points[0].x == EqEffect.DEFAULT_X1 && points[1].x == EqEffect.DEFAULT_X2)) {
            resetPoints();
            return;
        }
        // View being pulled for the first time -- Asign values from DB
        if (lo == null || hi == null) {
            left = new EQPointF(0, getMeasuredHeight() / 2);
            right = new EQPointF(getMeasuredWidth(), getMeasuredHeight() / 2);
            lo = new PointGroup(PointInit.LO);
            hi = new PointGroup(PointInit.HI);
        }

        float width = getMeasuredWidth();
        float height = getMeasuredHeight() / (float) 2;
        lo.set(points[0].x * width, height - points[0].y * height);
        hi.set(points[1].x * width, height - points[1].y * height);

        setPath();
        invalidate();
    }

    /**
     * Sets the controller to be used for changing backend model values
     * @param controller
     */
    public void setController(EQEffectController controller) {
        this.controller = controller;
    }

    /**
     * An eq point class used for comparing the two lo/hi points
     */
    private class EQPointF implements Comparable<EQPointF> {
        float x, y;
        float dx, dy;

        public EQPointF(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Comparing function to see which point has a higher x value
         * @param value
         * @return
         */
        @Override
        public int compareTo(EQPointF value) {
            return (int) (x - value.x);
        }
    }

    /**
     * This Point Group class encompasses the two main points used (lo / hi)
     * It contains the point, its two points that it holds to help w/ drawing
     * the curve & the rectangle around the point for touching/dragging the point
     */
    class PointGroup {

        private static final int RECT_DIVIDER = 2;
        public final int BANDWIDTH;
        public final int BANDWIDTH_DIVIDER = 8;
        public final int WIDTH_DIVIDER = 4;
        public final float RECTSIZE;
        private PointInit init;
        boolean enabled;
        RectF rect;
        private EQPointF leftPoint;
        private EQPointF centerPoint;
        private EQPointF rightPoint;

        PointGroup(PointInit init) {
            BANDWIDTH = getMeasuredWidth() / BANDWIDTH_DIVIDER;
            RECTSIZE = BANDWIDTH / RECT_DIVIDER;
            this.init = init;
            int x = getMeasuredWidth() / WIDTH_DIVIDER;
            int y = getMeasuredHeight() / RECT_DIVIDER;
            if (init == PointInit.HI)
                x *= 3;
            leftPoint = new EQPointF(x - BANDWIDTH, y);
            centerPoint = new EQPointF(x, y);
            rightPoint = new EQPointF(x + BANDWIDTH, y);

            rect = new RectF(centerPoint.x - RECTSIZE, centerPoint.y - RECTSIZE,
                    centerPoint.x + RECTSIZE, centerPoint.y + RECTSIZE);
            enabled = true;
        }

        /**
         * Sets the points to new values
         * @param x
         * @param y
         */
        void set(float x, float y) {
            if (x > BANDWIDTH / RECT_DIVIDER && x < getMeasuredWidth() - BANDWIDTH / RECT_DIVIDER) {
                leftPoint.x = x - BANDWIDTH;
                centerPoint.x = x;
                rightPoint.x = x + BANDWIDTH;
            }

            if (y < getMeasuredHeight() - RECTSIZE / RECT_DIVIDER && y > RECTSIZE / RECT_DIVIDER) {
                centerPoint.y = y;
            }

            rect.set(centerPoint.x - RECTSIZE, centerPoint.y - RECTSIZE,
                    centerPoint.x + RECTSIZE, centerPoint.y + RECTSIZE);;
        }
    }

    /**
     * A representation of an EQ canvas, which holds  a path
     * and paint
     */
    private void repInvariant() {
        if (linePath == null) {
            throw new AssertionError("Invalid path value");
        }
        if (linePaint == null || pointPaint == null) {
            throw new AssertionError("Invalid paint value");
        }
    }
}