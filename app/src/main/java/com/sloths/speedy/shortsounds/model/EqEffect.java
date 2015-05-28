package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.audiofx.Equalizer;
import android.util.Log;

/**
 * Equalizer Effect. This class provides a wrapper around the existing Android Equalizer
 * effect class. It holds two main pieces: The point values for the UI, & the actual
 * Equalizer AudioEffect.  It also holds the conversion function for converting
 * between these point values and the Equalizer AudioEffect values.
 */
public class EqEffect extends Effect {
    private static final String ON = "ON";
    public static final float DEFAULT_Y = 0;
    public static final float DEFAULT_X1 = 0.3f;
    public static final float DEFAULT_X2 = 0.7f;
    private static final String TAG = "EQ-EFFECT";
    private static final String EQUALIZER = "Equalizer";
    private PointF[] eqPoints;
    private boolean isActive;

    /**
     *  Constructor used when loading a track from a recorded file
     */
    public EqEffect() {
        // Sets up default equalizer until set by track player
        this.effect = new Equalizer( 0, 0 );
        PointF lo = new PointF(DEFAULT_X1, DEFAULT_Y);
        PointF hi = new PointF(DEFAULT_X2, DEFAULT_Y);
        this.eqPoints = new PointF[]{lo, hi};
        effect.setEnabled( false );
        isActive = false;
        repInvariant();
    }

    /**
     *  Constructor used when loading an effect from the database
     *  Stored in DB as "ON/OFF:float,float,float,float" or "NULL" w/o values
     * @param effectVals
     */
    public EqEffect(String effectVals) {
        this();
        if ( !effectVals.equals( "NULL" ) ) {
            // Parse string from DB to get point vals & on/off
            String[] params = effectVals.split(":");
            isActive = params[0].equals(ON);
            String[] pointVals = params[1].split(",");
            eqPoints = new PointF[2];
            eqPoints[0] = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
            eqPoints[1] = new PointF(new Float(pointVals[2]), new Float(pointVals[3]));
        }
        repInvariant();
    }

    /**
     * Setup the actual Android effect object. Note: this was a work-around we had to use due to
     * the fact that Equalizer needed the AudioTrack session id in order to be instantiated.
     * @param audioSessionId
     */
    public void setupEqEffect( int audioSessionId ) {
        Log.d( TAG, "Attaching EQ to track id [" + audioSessionId + "]" );
        try {
            this.effect = new Equalizer(0, audioSessionId);
            setEffectProperties();
            this.effect.setEnabled( isActive );
            Log.d(TAG, "Enabled [" + effect.getEnabled() + "]");
        } catch( Exception e ) {
            Log.e(TAG, "Error creating the Equalizer");
            e.printStackTrace();
        }
    }

    /**
     * Set the properties of the Equalizer effect
     */
    private void setEffectProperties() {
        Equalizer eq = (Equalizer) this.effect;
        short[] levelRange = eq.getBandLevelRange();

        // these are right triangles approximating the area under the EQ curve
        GraphShape g1 = new GraphShape(Math.max((float) (eqPoints[0].x - 0.125), 0f), 0,
                eqPoints[0].x, eqPoints[0].y);
        GraphShape g2 = new GraphShape(eqPoints[0].x, eqPoints[0].y,
                Math.min((float) (eqPoints[0].x + 0.125), 1f), 0);
        GraphShape g3 = new GraphShape(Math.max((float) (eqPoints[1].x - 0.125), 0f), 0,
                eqPoints[1].x, eqPoints[1].y);
        GraphShape g4 = new GraphShape(eqPoints[1].x, eqPoints[1].y,
                Math.min((float) (eqPoints[1].x + 0.125), 1f), 0);
        for (short i = 0; i < eq.getNumberOfBands(); i++) {
            float[] range = {((float) i) / eq.getNumberOfBands(), ((float) (i + 1)) / eq.getNumberOfBands()};
            float bandVal = 0;
            bandVal += g1.getArea(range[0], range[1]);
            bandVal += g2.getArea(range[0], range[1]);
            bandVal += g3.getArea(range[0], range[1]);
            bandVal += g4.getArea(range[0], range[1]);

            bandVal -= g1.getIntersectArea(g3, range[0], range[1]);
            bandVal -= g1.getIntersectArea(g4, range[0], range[1]);
            bandVal -= g2.getIntersectArea(g3, range[0], range[1]);
            bandVal -= g2.getIntersectArea(g4, range[0], range[1]);
            // now scale these 0-1 values to the millibel level range of the Equalizer
            if (levelRange[0] < 0 && levelRange[1] > 0) {
                if (bandVal > 0) {
                    bandVal *= Math.abs(levelRange[1]);
                } else {
                    bandVal *= Math.abs(levelRange[0]);
                }
            } else {
                float mid = (levelRange[0] + levelRange[1]) / 2;
                float width = (levelRange[1] - levelRange[0]) / 2;
                bandVal = (bandVal * width) + mid;
            }
            eq.setBandLevel(i, (short) (bandVal * eq.getNumberOfBands()));
        }
    }

    /**
     * This is used for modeling the approximate area under the curve
     * drawn by the EQ curve
     */
    private class GraphShape {
        private float x1, y1, x2, y2, slope;
        public GraphShape(float x1, float y1, float x2, float y2) {
            // disallow crossing the x axis
            if ((y1 > 0 && y2 < 0) || (y1 < 0 && y2 > 0)) {
                throw new IllegalArgumentException("GraphShapes cannot cross the x axis.");
            }
            // just to be sure we have ascending x values
            if (x1 <= x2) {
                this.x1 = x1;
                this.y1 = y1;
                this.x2 = x2;
                this.y2 = y2;
            } else {
                this.x1 = x2;
                this.y1 = y2;
                this.x2 = x1;
                this.y2 = y1;
            }
            if (this.x1 == this.x2) {
                this.slope = 0;
            } else {
                this.slope = (y2 - y1) / (x2 - x1);
            }
        }

        /**
         * This is used for returning the area under the curve
         * @param lowBound
         * @param highBound
         * @return
         */
        public float getArea(float lowBound, float highBound) {
            if (lowBound > highBound) {
                throw new IllegalArgumentException("lowBound cannot be greater than highBound.");
            }
            float innerX1 = Math.min(Math.max(lowBound, x1), highBound);
            float innerX2 = Math.max(Math.min(highBound, x2), lowBound);
            float innerY1;
            float innerY2;
            if (innerX1 == innerX2) {
                return 0f;
            }
            innerY1 = y1 + (slope * (innerX1 - x1));
            innerY2 = y2 + (slope * (innerX2 - x2));

            // rectangular portion
            float retVal = (innerX2 - innerX1) * innerY1;
            // triangular portion
            retVal += (innerX2 - innerX1) * (innerY2 - innerY1) / 2;
            return retVal;
        }

        /**
         * This is used for finding the area of intersection of the two shapes
         * that represent the curve
         * @param other
         * @param lowBound
         * @param highBound
         * @return
         */
        public float getIntersectArea(GraphShape other, float lowBound, float highBound) {
            if (this.x1 > other.x2 || this.x2 < other.x1
                    || this.getSign() != other.getSign()) {
                return 0f;
            }
            // using y=mx+b form
            float b1 = this.getYatX(0);
            float b2 = other.getYatX(0);
            float intersectX;
            float intersectY;
            if (this.slope == other.slope) {
                // in this case we don't care about the point of intersection
                intersectX = -1;
                intersectY = 0;
            } else {
                // modeling the summed line of the two shapes' outer bounds
                float sumB = b1 + b2;
                float sumSlope = this.slope + other.slope;
                intersectX = -1 * sumB / sumSlope;
                intersectY = this.getYatX(intersectX);
            }
            float innerX1 = Math.min(Math.max(lowBound, Math.max(this.x1, other.x1)), highBound);
            float innerY1 = Math.min(Math.abs(this.getYatX(innerX1)), Math.abs(other.getYatX(innerX1)))
                    * this.getSign();
            float innerX2 = Math.max(Math.min(highBound, Math.min(this.x2, other.x2)), lowBound);
            float innerY2 = Math.min(Math.abs(this.getYatX(innerX2)), Math.abs(other.getYatX(innerX2)))
                    * this.getSign();
            if (intersectX > innerX1 && intersectX < innerX2) {
                GraphShape g1 = new GraphShape(innerX1, innerY1, intersectX, intersectY);
                GraphShape g2 = new GraphShape(intersectX, intersectY, innerX2, innerY2);
                return g1.getArea(lowBound, highBound) + g2.getArea(lowBound, highBound);
            } else {
                GraphShape g = new GraphShape(innerX1, innerY1, innerX2, innerY2);
                return g.getArea(lowBound, highBound);
            }
        }

        /**
         * Helper for finding the y value represented as the outer boundary
         * of a shape, but extended.
         * @param x
         * @return
         */
        private float getYatX(float x) {
            return this.y1 - (this.slope * (this.x1 - x));
        }

        /**
         * Gives the y pos/neg of a y value as 1 or -1
         * @return
         */
        private int getSign() {
            if (y1 > 0 || y2 > 0 || (y1 == 0 && y2 == 0)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * This encodes the EQ params for storage in the DB
     * Stored in DB as "ON/OFF:float,float,float,float" or "NULL" w/o values
     * @return
     */
    public String encodeParameters() {
        if (eqPoints == null) {
            return "NULL";
        }
        String retVal = new String();
        if ( this.effect != null && this.effect.getEnabled() ) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        retVal += ":";
        retVal += eqPoints[0].x + ",";
        retVal += eqPoints[0].y + ",";
        retVal += eqPoints[1].x + ",";
        retVal += eqPoints[1].y;
        return retVal;
    }

    /**
     * Enable the actual EQ effect
     */
    @Override
    public void enable() {
        if ( this.effect == null ) {
            //TODO DO NOT ERASE
            //TODO deal with null case
            Log.e(TAG, "Error trying to enable EQ effect that is null");
        } else {
            Log.d(TAG, "Enabled EQ effect");
            this.effect.setEnabled(true);
            isActive = true;
        }
    }

    /**
     * Disable the actual EQ effect
     */
    public void disable() {
        if ( this.effect == null ) {
            //TODO DO NOT ERASE
            //TODO deal with null case
            Log.e(TAG, "Error trying to disable EQ effect that is null");
        } else {
            Log.d(TAG, "Disabled EQ effect");
            this.effect.setEnabled(false);
            isActive = false;
        }
    }

    /**
     * This is used for the UI to get the correct representation title for
     * the effect
     * @return
     */
    public String getTitleString() {
        return EQUALIZER;
    }

    /**
     * For getting point values to be used in UI for EQ band
     * @return
     */
    public PointF[] getPointVals() {
        return eqPoints;
    }

    /**
     * For setting the point values coming from UI
     * @param points
     */
    public void setPointVals(PointF[] points) {
        if (eqPoints == null) {
            this.eqPoints = new PointF[2];
        }
        this.eqPoints[0] = points[0];
        this.eqPoints[1] = points[1];
        // This updates the actual effect's properties
        setEffectProperties();
    }

    /**
     * Resets points for when cancel/back is clicked
     */
    public void resetVals() {
        PointF lo = new PointF(DEFAULT_X1, DEFAULT_Y);
        PointF hi = new PointF(DEFAULT_X2, DEFAULT_Y);
        this.eqPoints = new PointF[]{lo, hi};
        setEffectProperties();
    }

    /**
     * A representation invariant of an EQ effect that holds the points and bands
     * for the EQ UI and EQ effect.
     */
    private void repInvariant() {
        if (eqPoints == null) {
            throw new AssertionError("Invalid point value");
        }
    }
}
