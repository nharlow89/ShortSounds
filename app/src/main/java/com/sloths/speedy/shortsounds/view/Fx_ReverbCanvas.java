package com.sloths.speedy.shortsounds.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.sloths.speedy.shortsounds.controller.ReverbEffectController;
import java.util.Random;

/**
 * This class is for displaying a canvas view for the reverb effect
 */
public class Fx_ReverbCanvas extends View {

    private static final String TAG = "EffectCanvas";
    private Path xAxis;
    private Path yAxis;
    private Path linePath;
    private EchoGraphics echoLines;
    private Paint linePaint;
    private Paint pointPaint;
    private PointF left;
    private PointF right;
    private ControlPoint point;
    private int Y_MIN, Y_MAX, X_MIN, X_MAX;
    private final int MARGIN = 10;
    private final int NONE = -1, TOUCH_POINT = 0;
    private int currentTouch;
    private RectF pointTouch;
    private boolean firstDraw;
    private ReverbEffectController controller;

    /**
     * Constructor for setting up the canvas
     * @param c The context of where to make the canvas
     * @param attrs The attributes of the canvas
     */
    public Fx_ReverbCanvas(Context c, AttributeSet attrs) {
        super(c, attrs);
        firstDraw = true;

        xAxis = new Path();
        yAxis = new Path();
        linePath = new Path();

        linePaint = new Paint();
        pointPaint =  new Paint();

        currentTouch = NONE;

        left = new PointF();
        right = new PointF();

        repInvariant();
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


    /**
     * On draw is used to actually draw on the canvas
     * It's main purpose is to draw the updated point, line &
     * the echo lines.
     * @param canvas the Canvas to draw on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (firstDraw) {
            // Draw the line between initial points
            setStyles();
            firstDraw = false;
        }

        echoLines.draw(canvas);
        linePaint.setColor(Color.BLUE);
        canvas.drawPath(linePath, linePaint);
        canvas.drawPath(xAxis, linePaint);
        canvas.drawPath(yAxis, linePaint);
        canvas.drawPoint(point.x, point.y, pointPaint);
    }

    /**
     * Helper method for intially drawing out the y & x axis
     */
    private void setUpGraph() {
        Y_MAX = MARGIN;
        Y_MIN = getMeasuredHeight() - MARGIN;
        X_MIN = MARGIN;
        X_MAX = getMeasuredWidth() - MARGIN;

        xAxis.moveTo(MARGIN, Y_MIN);
        yAxis.moveTo(MARGIN, Y_MIN);
        xAxis.lineTo(X_MAX, Y_MIN);
        yAxis.lineTo(MARGIN, Y_MAX);

        left.x = X_MIN;
        right.x = X_MAX;

        point = new ControlPoint();
        setLine();
        echoLines = new EchoGraphics();
        echoLines.setEchos();
    }

    /**
     * This method shapes the line that will be drawn to represent
     * the reverb effect.  It is a parabolic looking curve, and
     * mostly depends on the location of the controlled point.
     */
    private void setLine() {
        float height0 = Y_MIN - (Math.abs(point.y - Y_MIN) * 3f  / 4);
        float deltaHeight = 16f * X_MIN / point.x;
        float height1 = height0 + (X_MAX - X_MIN) * deltaHeight;

        if (height1 > Y_MIN) {
            height1 = Y_MIN;
            right.x = (Y_MIN - height0) / deltaHeight;
        } else {
            right.x = X_MAX;
        }

        left.y = height0;
        right.y = height1;

        linePath.reset();
        linePath.moveTo(left.x, left.y);
        linePath.quadTo(right.x / 2, point.y, right.x, right.y);
    }

    /**
     * This handles touching and controlling points
     * @param event The motion event (i.e. touching the screen, moving, or lifting)
     * @return true if successful, false otherwise
     */
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
                    controlGraphicsAndEffect(x, y);
                }
                break;
            // If user lifts up --> set new touch area & draw line & point
            case MotionEvent.ACTION_UP:
                if (currentTouch == TOUCH_POINT) {
                    controlGraphicsAndEffect(x, y);
                    currentTouch = NONE;
                }
                break;
        }
        return true;
    }

    /**
     * Updates the point & line graphics.
     * Uses the controller to update the backend model
     * This will let the backend change its parameters for controlling
     * the effect
     * @param x The x value of the point
     * @param y the y value of the point
     */
    private void controlGraphicsAndEffect(float x, float y) {
        controlGraphics(x, y);
        if (controller != null) {
            controller.updateEffectValues(getValue());
        }
    }

    /**
     * Controls the graphics
     * @param x the x value of the point
     * @param y the y value of the point
     */
    private void controlGraphics(float x, float y) {
        point.set(x, y);
        setLine();
        echoLines.setEchos();
        invalidate();
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
     * @param controller The controller that will be set to control the back end
     */
    public void setController(ReverbEffectController controller) {
        this.controller = controller;
    }

    /**
     * Gets the value of the point that controls the UI
     * @return PointF, the value of the point that controls the UI
     */
    public PointF getValue() {
        float percentX = point.x / (float) getMeasuredWidth();
        float percentY = point.y / (float) getMeasuredHeight();
        return new PointF(percentX, percentY);
    }

    /**
     * Sets the value for the point that controls the UI
     * @param value the point that controls the UI
     */
    public void setValue(PointF value) {
        // Data coming from database
        if (point == null) {
            // View being setup for the first time
            setUpGraph();
        }
        float x = value.x * getMeasuredWidth();
        float y = value.y * getMeasuredHeight();
        controlGraphics(x, y);
    }

    /**
     * This control point encapsulates the point being used to
     * control the UI.
     */
    class ControlPoint {
        private final float RECTSIZE;
        private float x;
        private float y;

        /**
         * Creates a ControlPoint
         */
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
         * @param x The new x value for the point
         * @param y The new y value for the point
         */
        void set(float x, float y) {
            if (x > X_MIN + 4 * MARGIN && x < X_MAX - 4 * MARGIN)
                this.x = x;
            if (y  < Y_MIN - 4 * MARGIN && y  > Y_MAX + 4 * MARGIN)
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

            final int height = Y_MIN - Y_MAX;
            final int width =  X_MAX - X_MIN;
            double heightScalar = 1.0 / ECHO_SIZE;

            int x = (X_MIN + r.nextInt(width / ECHO_SIZE));
            int y;
            for (int i = 0; i < ECHO_SIZE; i++) {
                y = (int) (Y_MAX + (heightScalar * height) + (height / 6 - r.nextInt(height / 3)));
                heightScalar += (1.0 / ECHO_SIZE);
                echos[i] = new PointF(x, y);
                x += 1.5 * width / ECHO_SIZE - r.nextInt(width / ECHO_SIZE);
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
            float xScal = point.x / X_MAX;
            float yScal = Math.abs(point.y - Y_MIN) / (Y_MIN - Y_MAX);

            for (int i = 0; i < ECHO_SIZE; i++) {
                arr[i].reset();
                arr[i].moveTo(X_MIN + xScal * echos[i].x, Y_MIN);
                arr[i].lineTo(X_MIN + xScal * echos[i].x, Y_MIN - yScal * Math.abs(echos[i].y - Y_MIN));
            }
        }

        /**
         * Draws the echos
         * @param canvas The canvas to draw the echoes on
         */
        void draw(Canvas canvas) {
            for (int i = 0; i < ECHO_SIZE; i++) {
                canvas.drawPath(arr[i], p);
            }
        }
    }

    /**
     * A representation of the reverb canvas that holds points
     * axis and paint
     */
    private void repInvariant() {
        if (left == null || right == null) {
            throw new AssertionError("Invalid left point value");
        }
        if (xAxis == null || yAxis == null) {
            throw new AssertionError("Invalid axis value");
        }
        if (linePaint == null || pointPaint == null) {
            throw new AssertionError("Invalid paint value");
        }
    }
}