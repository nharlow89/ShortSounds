package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
    private PointF left;
    private PointF right;
    private PointGroup lo;
    private PointGroup hi;
    private List<PointF> path;



    private PointInit currentGroup;
    private boolean firstDraw;


    public Fx_EQCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        firstDraw = true;

        // Path for eq Line
        linePath = new Path();
        linePaint = new Paint();
        pointPaint =  new Paint();

        Log.i(TAG, "width = " + getMeasuredWidth());
        Log.i(TAG, "height = " + getMeasuredHeight());

        // Initialize touch locations for points
        currentGroup = PointInit.NONE;
    }

    public void resetPoints() {
        left = new PointF(0, getMeasuredHeight() / 2);
        right = new PointF(getMeasuredWidth(), getMeasuredHeight() / 2);
        lo = new PointGroup(PointInit.LO, getMeasuredWidth(), getMeasuredHeight());
        hi = new PointGroup(PointInit.HI, getMeasuredWidth(), getMeasuredHeight());
    }

    void setStyles() {
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

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstDraw) {
            // Draw the line between initial points
            setStyles();
            resetPoints();
            setPath();
            firstDraw = false;
        }

        // draw the line between points
        canvas.drawPath(linePath, linePaint);
        // draw the two points
        if (lo.enabled)
            pointPaint.setColor(Color.RED);
            canvas.drawPoint(lo.cp.x, lo.cp.y, pointPaint);
        if (hi.enabled)
            pointPaint.setColor(Color.BLUE);
            canvas.drawPoint(hi.cp.x, hi.cp.y, pointPaint);
    }

    // Private helper for drawing the line between all the points
    private void setPath() {
        updatePath();
        linePath.reset();
        for (int i = 0; i < path.size(); i++){
            if (i >= 0){
                PointF point = path.get(i);
                if (i == 0) {
                    PointF next = path.get(i + 1);
                    point.dx = ((next.x - point.x) / 3);
                    point.dy = ((next.y - point.y) / 3);
                } else if (i == path.size() - 1) {
                    PointF prev =path.get(i - 1);
                    point.dx = ((point.x - prev.x) / 3);
                    point.dy = ((point.y - prev.y) / 3);
                } else {
                    PointF next = path.get(i + 1);
                    PointF prev = path.get(i - 1);
                    point.dx = ((next.x - prev.x) / 5);
                    point.dy = ((next.y - prev.y) / 5);
                }
            }
        }

        PointF prev = path.get(0);
        PointF point;
        linePath.moveTo(prev.x, prev.y);
        for(int i = 1; i < path.size(); i++){
            point = path.get(i);
            linePath.cubicTo(prev.x + prev.dx, prev.y + prev.dy,
                             point.x - point.dx, point.y - point.dy, point.x, point.y);
            prev = point;
        }
    }

    private void updatePath() {
        PointGroup lg = lo;
        PointGroup rg = hi;

        if (lo.cp.x > hi.cp.x) {
            lg = hi;
            rg = lo;
        }
        PointF p = new PointF((int) ((lg.rp.x + rg.lp.x) / 2), (int) lg.rp.y);
        path = Arrays.asList(left, lg.lp, lg.cp, p, rg.cp, rg.rp, right);
    }


    // This is where we will handle controlling points
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
                    setPath();
                    invalidate();
                } else if (currentGroup == PointInit.HI) {
                    hi.set(x, y);
                    setPath();
                    invalidate();
                }
                break;

            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                currentGroup = PointInit.NONE;
                break;
        }
        return true;
    }


    private class PointF implements Comparable<PointF> {
        float x, y;
        float dx, dy;


        public PointF(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(PointF o) {
            return (int) (x - o.x);
        }
    }

    class PointGroup {

        public final int BANDWIDTH;
        public final float RECTSIZE;
        private PointInit init;
        boolean enabled;
        RectF rect;
        private PointF lp;
        private PointF cp;
        private PointF rp;

        PointGroup(PointInit init, int width, int height) {
            BANDWIDTH = width / 8;
            RECTSIZE = BANDWIDTH / 2;
            this.init = init;
            int x = width / 4;
            int y = height / 2;
            if (init == PointInit.HI)
                x *= 3;
            lp = new PointF(x - BANDWIDTH, y);
            cp = new PointF(x, y);
            rp = new PointF(x + BANDWIDTH, y);

            rect = new RectF(cp.x - RECTSIZE, cp.y - RECTSIZE,
                             cp.x + RECTSIZE, cp.y + RECTSIZE);
            enabled = true;
        }

        void set(float x, float y) {
            if (x > BANDWIDTH / 2 && x < getMeasuredWidth() - BANDWIDTH / 2) {
                lp.x = x - BANDWIDTH;
                cp.x = x;
                rp.x = x + BANDWIDTH;
            }

            if (y < getMeasuredHeight() - RECTSIZE / 2 && y > RECTSIZE / 2)
                cp.y = y;

            rect.set(cp.x - RECTSIZE, cp.y - RECTSIZE,
                    cp.x + RECTSIZE, cp.y + RECTSIZE);;
        }
    }
}