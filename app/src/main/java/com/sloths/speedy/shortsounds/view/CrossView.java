package com.sloths.speedy.shortsounds.view;


        import android.animation.ValueAnimator;
        import android.annotation.TargetApi;
        import android.content.Context;
        import android.content.res.TypedArray;
        import android.graphics.Canvas;
        import android.graphics.Color;
        import android.graphics.Paint;
        import android.graphics.Path;
        import android.graphics.PathMeasure;
        import android.graphics.RectF;
        import android.os.Build;
        import android.os.Bundle;
        import android.os.Parcel;
        import android.os.Parcelable;
        import android.util.AttributeSet;
        import android.view.View;
        import android.view.animation.AccelerateDecelerateInterpolator;

        import com.sloths.speedy.shortsounds.R;


/**
 * This class enables the cross view to enable an app.
 * @author Nick Jones
 */


public class CrossView extends View {
    /**
     * Flag to denote the "plus" configuration
     */
    public static final int FLAG_STATE_PLUS = 0;
    /**
     * Flag to denote the "cross" configuration
     */
    public static final int FLAG_STATE_CROSS = 1;

    private static final float ARC_TOP_START = 225;
    private static final float ARC_TOP_ANGLE = 45f;
    private static final float ARC_BOTTOM_START = 45f;
    private static final float ARC_BOTTOM_ANGLE = 45f;
    private static final float ARC_LEFT_START = 315f;
    private static final float ARC_LEFT_ANGLE = -135f; // sweep backwards
    private static final float ARC_RIGHT_START = 135f;
    private static final float ARC_RIGHT_ANGLE = -135f; // sweep backwards

    private static final long ANIMATION_DURATION_MS = 300l;

    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final float DEFAULT_STROKE_WIDTH = 10f;

    // Arcs that define the set of all points between which the two lines are drawn
    // Names (top, bottom, etc) are from the reference point of the "plus" configuration.
    private Path mArcTop;
    private Path mArcBottom;
    private Path mArcLeft;
    private Path mArcRight;

    // Pre-compute arc lengths when layout changes
    private float mArcLengthTop;
    private float mArcLengthBottom;
    private float mArcLengthLeft;
    private float mArcLengthRight;

    private Paint mPaint;
    private Paint mBackPaint;
    private int mColor = DEFAULT_COLOR;
    private RectF mRect;
    private PathMeasure mPathMeasure;

    private float[] mFromXY;
    private float[] mToXY;

    /**
     * Internal state flag for the drawn appearance, plus or cross.
     * The default starting position is "plus". This represents the real configuration, whereas
     * {@code mPercent} holds the frame-by-frame position when animating between
     * the states.
     */
    private int mState = FLAG_STATE_CROSS;

    /**
     * The percent value upon the arcs that line endpoints should be found
     * when drawing.
     */
    private float mPercent = 1f;

    /**
     * Creates a new CrossView
     * @param context the context of the app
     */
    public CrossView(Context context) {
        super(context);
    }

    /**
     * Creates a new CrossView
     * @param context the context of the app
     * @param attrs the attributes of the crossview
     */
    public CrossView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        readXmlAttributes(context, attrs);
    }

    /**
     * Creates a new CrossView
     * @param context The context of the app
     * @param attrs the attributes of the CrossView
     * @param defStyleAttr The style attirbutes of the CrossView
     */
    public CrossView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        String s = "color";
//        readXmlAttributes(context, attrs);
    }

    /**
     * Reads the XML attributes for the CrossView
     * @param context the context of the app
     * @param attrs the desired attributes
     */
    private void readXmlAttributes(Context context, AttributeSet attrs) {
        // Size will be used for width and height of the icon, plus the space in between
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CrossView, 0, 0);
        try {
            mColor = a.getColor(android.R.color.black, DEFAULT_COLOR);
        } finally {
            a.recycle();
        }
    }

    /**
     * Draws the CrossView
     * @param canvas the canvas on which to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        determineColor();
        setPointFromPercent(mArcTop, mArcLengthTop, mPercent, mFromXY);
        setPointFromPercent(mArcBottom, mArcLengthBottom, mPercent, mToXY);

        canvas.drawLine(mFromXY[0], mFromXY[1], mToXY[0], mToXY[1], mBackPaint);
        canvas.drawLine(mFromXY[0], mFromXY[1], mToXY[0], mToXY[1], mPaint);

        setPointFromPercent(mArcLeft, mArcLengthLeft, mPercent, mFromXY);
        setPointFromPercent(mArcRight, mArcLengthRight, mPercent, mToXY);

        canvas.drawLine(mFromXY[0], mFromXY[1], mToXY[0], mToXY[1], mBackPaint);
        canvas.drawLine(mFromXY[0], mFromXY[1], mToXY[0], mToXY[1], mPaint);
    }

    /**
     * Determines the color of the CrossView
     */
    private void determineColor() {
        int red;
        int green;
        if (mState == FLAG_STATE_PLUS) {
            red = (int) (200 * (1 - mPercent));
            green = (int) (200 * mPercent);
        } else {
            red = (int) (200 * mPercent);
            green = (int) (200 * (1 - mPercent));
        }
        setColor(Color.rgb(red, green, 100));
    }

    /**
     * Changes the layout
     * @param changed Whether or not the view changed
     * @param left left point
     * @param top top point
     * @param right right point
     * @param bottom bottom point
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            init();
            invalidate();
        }
    }

    /**
     * Saves the state
     * @return the saved state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        if (parcelable == null) {
            parcelable = new Bundle();
        }

        CrossViewState savedState = new CrossViewState(parcelable);
        savedState.flagState = mState;
        return savedState;
    }

    /**
     * Restores the state
     * @param state the state to restore the CrossView to
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof CrossViewState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        CrossViewState ss = (CrossViewState)state;
        mState = ss.flagState;
        if (mState != FLAG_STATE_PLUS && mState != FLAG_STATE_CROSS) {
            mState = FLAG_STATE_PLUS;
        }

        super.onRestoreInstanceState(ss.getSuperState());
    }

    /**
     * Sets the padding
     * @param left left point
     * @param top top point
     * @param right right point
     * @param bottom bottom point
     */
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        init();
    }

    /**
     * Sets the relative padding
     * @param start start point
     * @param top top point
     * @param end end point
     * @param bottom bottom point
     */
    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        init();
    }

    /**
     * Sets the color
     * @param argb the color to set to in RGB scale
     */
    public void setColor(int argb) {
        mColor = argb;
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.setColor(argb);
        invalidate();
    }

    /**
     * Sets the initial state
     * @param isChecked Whether or not the CrossView is checked
     */
    public void setInitialState(boolean isChecked) {
        if (isChecked)
            mState = FLAG_STATE_PLUS;
        else
            mState = FLAG_STATE_CROSS;
        invalidate();
    }

    /**
     * Tell this view to switch states from cross to plus, or back, using the default animation duration.
     * @return an integer flag that represents the new state after toggling.
     *         This will be either {@link #FLAG_STATE_PLUS} or {@link #FLAG_STATE_CROSS}
     */
    public int toggle() {
        return toggle(ANIMATION_DURATION_MS);
    }

    /**
     * Tell this view to switch states from cross to plus, or back.
     * @param animationDurationMS duration in milliseconds for the toggle animation
     * @return an integer flag that represents the new state after toggling.
     *         This will be either {@link #FLAG_STATE_PLUS} or {@link #FLAG_STATE_CROSS}
     */
    public int toggle(long animationDurationMS) {
        mState = mState == FLAG_STATE_PLUS? FLAG_STATE_CROSS : FLAG_STATE_PLUS;
        // invert percent, because state was just flipped
        mPercent = 1 - mPercent;
        ValueAnimator animator = ValueAnimator.ofFloat(mPercent, 1);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animationDurationMS);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setPercent(animation.getAnimatedFraction());
            }
        });

        animator.start();
        return mState;
    }

    /**
     * Transition to "X"
     */
    public void cross() {
        cross(ANIMATION_DURATION_MS);
    }

    /**
     * Transition to "X" over the given animation duration
     * @param animationDurationMS
     */
    public void cross(long animationDurationMS) {
        if (mState == FLAG_STATE_CROSS) {
            return;
        }
        toggle(animationDurationMS);
    }

    /**
     * Transition to "+"
     */
    public void plus() {
        plus(ANIMATION_DURATION_MS);
    }

    /**
     * Transition to "+" over the given animation duration
     */
    public void plus(long animationDurationMS) {
        if (mState == FLAG_STATE_PLUS) {
            return;
        }
        toggle(animationDurationMS);
    }

    /**
     * Sets the percent
     * @param percent Desired percent
     */
    private void setPercent(float percent) {
        mPercent = percent;
        invalidate();
    }

    /**
     * Perform measurements and pre-calculations.  This should be called any time
     * the view measurements or visuals are changed, such as with a call to {@link #setPadding(int, int, int, int)}
     * or an operating system callback like {@link #onLayout(boolean, int, int, int, int)}.
     */
    private void init() {
        mPaint = new Paint();
        mBackPaint = new Paint();
        mRect = new RectF();
        mRect.left = getPaddingLeft();
        mRect.right = getWidth() - getPaddingRight();
        mRect.top = getPaddingTop();
        mRect.bottom = getHeight() - getPaddingBottom();

        mPathMeasure = new PathMeasure();

        mArcTop = new Path();
        mArcTop.addArc(mRect, ARC_TOP_START, ARC_TOP_ANGLE);
        mPathMeasure.setPath(mArcTop, false);
        mArcLengthTop = mPathMeasure.getLength();

        mArcBottom = new Path();
        mArcBottom.addArc(mRect, ARC_BOTTOM_START, ARC_BOTTOM_ANGLE);
        mPathMeasure.setPath(mArcBottom, false);
        mArcLengthBottom = mPathMeasure.getLength();

        mArcLeft = new Path();
        mArcLeft.addArc(mRect, ARC_LEFT_START, ARC_LEFT_ANGLE);
        mPathMeasure.setPath(mArcLeft, false);
        mArcLengthLeft = mPathMeasure.getLength();

        mArcRight = new Path();
        mArcRight.addArc(mRect, ARC_RIGHT_START, ARC_RIGHT_ANGLE);
        mPathMeasure.setPath(mArcRight, false);
        mArcLengthRight = mPathMeasure.getLength();

        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH);

        mBackPaint.setAntiAlias(true);
        mBackPaint.setColor(getContext().getResources().getColor(R.color.accent_material_dark));
        mBackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mBackPaint.setStrokeCap(Paint.Cap.ROUND);
        mBackPaint.setStrokeWidth(DEFAULT_STROKE_WIDTH * 1.5f);

        mFromXY = new float[]{0f, 0f};
        mToXY = new float[]{0f, 0f};
    }

    /**
     * Given some path and its length, find the point ([x,y]) on that path at
     * the given percentage of length.  Store the result in {@code points}.
     * @param path any path
     * @param length the length of {@code path}
     * @param percent the percentage along the path's length to find a point
     * @param points a float array of length 2, where the coordinates will be stored
     */
    private void setPointFromPercent(Path path, float length, float percent, float[] points) {
        float percentFromState = mState == FLAG_STATE_PLUS ? percent : 1 - percent;
        mPathMeasure.setPath(path, false);
        mPathMeasure.getPosTan(length * percentFromState, points, null);

    }

    /**
     * Determines whether or not the CrossView is on
     * @return true if on, false otherwise
     */
    public boolean isOn() { return mState == FLAG_STATE_CROSS; }

    /**
     * Internal saved state
     */
    static class CrossViewState extends BaseSavedState {
        private int flagState;

        CrossViewState(Parcelable superState) {
            super(superState);
        }

        /**
         * Creates a CrossViewState
         * @param in The state of the CrossView
         */
        private CrossViewState(Parcel in) {
            super(in);
            this.flagState = in.readInt();
        }

        /**
         * Writes to the CrossView state
         * @param out the parcel being written to
         * @param flags Flags for inherited method
         */
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.flagState);
        }

        /**
         * Parcelable Creator
         */
        public static final Parcelable.Creator<CrossViewState> CREATOR =
                new Parcelable.Creator<CrossViewState>() {
                    public CrossViewState createFromParcel(Parcel in) {
                        return new CrossViewState(in);
                    }
                    public CrossViewState[] newArray(int size) {
                        return new CrossViewState[size];
                    }
                };
    }
}
