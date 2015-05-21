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

import com.sloths.speedy.shortsounds.controller.ReverbEffectController;

import java.util.Random;

/**
 * Created by shampson on 5/7/15.
 */
public class Fx_ReverbCanvas extends View {


    private static final String TAG = "EffectCanvas";
    private Path xAxis;
    private Path yAxis;
    private Path linePath;
    private EchoGraphics echoG;
    private Paint linePaint;
    private Paint pointPaint;
    private PointF left;
    private PointF right;

    private ControlPoint point;
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
    private ReverbEffectController controller;

    public Fx_ReverbCanvas(Context c, AttributeSet attrs) {
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

    /**
     * Sets up styles on initialization
     */
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
            firstDraw = false;
        }

        echoG.draw(canvas);
        linePaint.setColor(Color.BLUE);
        canvas.drawPath(linePath, linePaint);
        linePaint.setColor(Color.BLUE);
        canvas.drawPath(xAxis, linePaint);
        canvas.drawPath(yAxis, linePaint);
        canvas.drawPoint(point.x, point.y, pointPaint);
    }

    // Helper method for intially drawing out the y & x axis
    private void setUpGraph() {
        YMAX = MARGIN;
        YMIN = getMeasuredHeight() - MARGIN;
        XMIN = MARGIN;
        XMAX = getMeasuredWidth() - MARGIN;

        xAxis.moveTo(MARGIN, YMIN);
        yAxis.moveTo(MARGIN, YMIN);
        xAxis.lineTo(XMAX, YMIN);
        yAxis.lineTo(MARGIN, YMAX);

        left.x = XMIN;
        right.x = XMAX;

        point = new ControlPoint();
        setLine();
        echoG = new EchoGraphics();
        echoG.setEchos();
    }

    /**
     * This method shapes the line that will be drawn to represent
     * the reverb effect.  It is a parabolic looking curve, and
     * mostly depends on the location of the controlled point.
     */
    private void setLine() {
        float h0 = YMIN - (Math.abs(point.y - YMIN) * 3f  / 4);
        float deltaH = 16f * XMIN / point.x;
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
        linePath.quadTo(right.x / 2, point.y, right.x, right.y);
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
                    point.set(x, y);
                    setLine();
                    echoG.setEchos();
                    invalidate();
                    controlEffect();
                }
                break;
            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                if (currentTouch == TOUCH_POINT) {
                    point.set(x, y);
                    invalidate();
                    currentTouch = NONE;
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
//            Log.d("REVERBCANVAS", "Updating rev controller to point: ("+getValue().x+", "+getValue().y+")");
            controller.updateEffectValues(getValue());
        }
    }

    /**
     * Resets the point value to its original state
     */
    public void resetPoint() {
        setUpGraph();
        invalidate();
    }

    /**
     * Sets the controller the for the backend
     * @param controller
     */
    public void setController(ReverbEffectController controller) {
        this.controller = controller;
    }

    /**
     * Gets the value of the point that controls the UI
     * @return
     */
    public PointF getValue() {
        float percentX = point.x / (float) getMeasuredWidth();
        float percentY = point.y / (float) getMeasuredHeight();
        return new PointF(percentX, percentY);
    }

    /**
     * Sets the value for the point that controls the UI
     * @param values
     */
    public void setValue(PointF[] values) {
        // Data coming from database
        if (point == null) {
            // View being setup for the first time
            setUpGraph();
        }
        float x = values[0].x * getMeasuredWidth();
        float y = values[0].y * getMeasuredHeight();
        point.set(x, y);
        setLine();
        echoG.setEchos();
        invalidate();
    }

    /**
     * This control point encapsulates the point being used to
     * control the UI.
     */
    class ControlPoint {
        private final float RECTSIZE;
        private float x;
        private float y;

        ControlPoint() {
            RECTSIZE = getMeasuredWidth() / 16f;
            // Initialize x & y at default locations
            // These will be set by effect values w/ backend connected
            x = getMeasuredWidth() / 2;
            y = getMeasuredHeight() / 2;

            // Initialize touch locations for points
            pointTouch = new RectF(x - RECTSIZE, y - RECTSIZE,
                    x + RECTSIZE, y + RECTSIZE);
        }

        /**
         * Sets new values for the point
         * @param x
         * @param y
         */
        void set(float x, float y) {
            if (x > XMIN + 4 * MARGIN && x < XMAX - 4 * MARGIN)
                this.x = x;
            if (y  < YMIN - 4 * MARGIN && y  > YMAX + 4 * MARGIN)
                this.y = y;

            // Initialize touch locations for points
            pointTouch.set(this.x - RECTSIZE, this.y - RECTSIZE,
                    this.x + RECTSIZE, this.y + RECTSIZE);
        }
    }
    /**
     * A class used for the echo lines.  These lines represent the length
     * between echos on a reverb and the amount of echo.
     */
    class EchoGraphics {
        final int ECHO_SIZE = 40;
        PointF[] echos;
        Path[] arr;
        int[] spacing;
        Paint p;

        EchoGraphics() {
            Random r = new Random();
            echos = new PointF[ECHO_SIZE];
            spacing = new int[ECHO_SIZE];
            arr = new Path[ECHO_SIZE];
            for (int i = 0; i < ECHO_SIZE; i++) {
                arr[i] = new Path();
            }

            final int H = YMIN - YMAX;
            final int W =  XMAX - XMIN;
            double heightScalar = 1.0 / ECHO_SIZE;

            int x = (XMIN + r.nextInt(W / ECHO_SIZE));
            int y;
            for (int i = 0; i < ECHO_SIZE; i++) {
                y = (int) (YMAX + (heightScalar * H) + (H / 6 - r.nextInt(H / 3)));
                heightScalar += (1.0 / ECHO_SIZE);
                echos[i] = new PointF(x, y);
                x += 1.5 * W / ECHO_SIZE - r.nextInt(W / ECHO_SIZE);
            }

            setUpPaint();
        }

        /**
         * Sets up the style for echo points
         */
        private void setUpPaint() {
            // Paint specs for line
            p = new Paint();
            p.setAntiAlias(true);
            p.setColor(Color.YELLOW);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(5f);
        }

        /**
         * Sets the echos according to the point location
         */
        void setEchos() {
            float xScal = point.x / XMAX;
            float yScal = Math.abs(point.y - YMIN) / (YMIN - YMAX);

            for (int i = 0; i < ECHO_SIZE; i++) {
                arr[i].reset();
                arr[i].moveTo(XMIN + xScal * echos[i].x, YMIN);
                arr[i].lineTo(XMIN + xScal * echos[i].x, YMIN - yScal * Math.abs(echos[i].y - YMIN));
            }
        }

        /**
         * Draws the echos
         * @param canvas
         */
        void draw(Canvas canvas) {
            for (int i = 0; i < ECHO_SIZE; i++) {
                canvas.drawPath(arr[i], p);
            }
        }
    }
}