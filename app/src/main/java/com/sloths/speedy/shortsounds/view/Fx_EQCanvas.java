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
        left = new EQPointF(0, getMeasuredHeight() / 2);
        right = new EQPointF(getMeasuredWidth(), getMeasuredHeight() / 2);
        lo = new PointGroup(PointInit.LO);
        hi = new PointGroup(PointInit.HI);
        setPath();
        invalidate();
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

        EQPointF prev = path.get(0);
        EQPointF point;
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
        EQPointF p = new EQPointF((int) ((lg.rp.x + rg.lp.x) / 2), (int) lg.rp.y);
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
                controlEffect();
                break;

            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                currentGroup = PointInit.NONE;
                controlEffect();
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
            Log.d("EQCANVAS", "Updating eq controller to points: (" + getBandA().x +", "+getBandA().y+") ("+getBandB().x+", "+getBandB().y+")");
            controller.updateEffectValues(new PointF[]{getBandA(), getBandB()});
        }
    }


    private class EQPointF implements Comparable<EQPointF> {
        float x, y;
        float dx, dy;


        public EQPointF(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(EQPointF o) {
            return (int) (x - o.x);
        }
    }

    class PointGroup {

        public final int BANDWIDTH;
        public final float RECTSIZE;
        private PointInit init;
        boolean enabled;
        RectF rect;
        private EQPointF lp;
        private EQPointF cp;
        private EQPointF rp;

        PointGroup(PointInit init) {
            BANDWIDTH = getMeasuredWidth() / 8;
            RECTSIZE = BANDWIDTH / 2;
            this.init = init;
            int x = getMeasuredWidth() / 4;
            int y = getMeasuredHeight() / 2;
            if (init == PointInit.HI)
                x *= 3;
            lp = new EQPointF(x - BANDWIDTH, y);
            cp = new EQPointF(x, y);
            rp = new EQPointF(x + BANDWIDTH, y);

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
    public PointF getBandA() {
        return getBand(lo.cp);
    }

    public PointF getBandB() {
        return getBand(hi.cp);
    }

    private PointF getBand(EQPointF point) {
        float midY = (float) getMeasuredHeight() / (float) 2;

        // lo point to return as percentage
        float loXPercent = point.x / (float) getMeasuredWidth();
        float loYPercent = (midY - point.y) / midY;
        return new PointF(loXPercent, loYPercent);
    }

    public void setValues(PointF[] points) {
        if (points == null || points.length == 0 ||
                (points[0].x == EqEffect.DEFAULT_X1 && points[1].x == EqEffect.DEFAULT_X2)) {
            // Nothing pulled from model --> Set to default
            resetPoints();
            return;
        }
        if (lo == null || hi == null) {
            // View being pulled for the first time
            resetPoints();
        }

        float width = getMeasuredWidth();
        float height = getMeasuredHeight() / (float) 2;
        lo.set(points[0].x * width, height - points[0].y * height);
        hi.set(points[1].x * width, height - points[1].y * height);
        setPath();
        invalidate();
    }

    public void setController(EQEffectController controller) {
        this.controller = controller;
    }

}