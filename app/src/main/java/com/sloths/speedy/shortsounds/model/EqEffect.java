package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.audiofx.Equalizer;
import android.util.Log;

import java.util.Arrays;

/**
 * Equalizer Effect. This class provides a wrapper around the existing Android Equalizer
 * effect class.
 */
public class EqEffect extends Effect {
    // these INDEX constants are for accessing the values encoded in the string after
    // it has been split by "."
    private static final int INDEX_ACTIVE = 1;
    private static final int INDEX_BAND_LEVELS = 2;
    private static final int NUM_BANDS = 2;
    private static final String ON = "ON";
    public static final float DEFAULT_Y = 0;
    public static final float DEFAULT_X1 = 0.3f;
    public static final float DEFAULT_X2 = 0.7f;
    private static final String TAG = "EQ-EFFECT";
    private PointF[] eqPoints;
    private boolean isActive;

    // Constructor used when loading a track from a recorded file
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

    // Constructor used when loading an effect from the database
    public EqEffect(String effectVals) {
        this();
        if ( !effectVals.equals( "NULL" ) ) {
            // Parse string from DB to get point vals & on/off
            // Stored in DB as "ON/OFF:float,float,float,float"
            String[] params = effectVals.split(":");
            isActive = params[0].equals(ON);
            this.effect.setEnabled( isActive );
            Log.d(TAG, "Loaded eq from DB: " +effectVals);
            Log.d(TAG, "Is active? " + isActive);
            this.effect.setEnabled( isActive );
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
        Log.d("REVERB", "Setting up eq to track #" +audioSessionId);
        this.effect = new Equalizer( 0, audioSessionId );
        setEffectProperties();
        this.effect.setEnabled( isActive );
        Log.d("EQEffect", "Enabled? " + effect.getEnabled());
    }

    /**
     * Set the properties of the Equalizer effect class.
     */
    private void setEffectProperties() {
        Log.d("EQEFFECT", "Setting eq params");
        short bandLevels[] = convertParamsToSettings();
        Equalizer eq = (Equalizer) this.effect;
        for (int i = 0; i < eq.getNumberOfBands(); i++) {
            Log.d("EFFECTS", "set band["+i+"] to level[-1500], previous level["+eq.getBandLevel((short)i)+"]");
            int[] range = eq.getBandFreqRange((short) i);
            eq.setBandLevel( (short)i, bandLevels[i] );
        }
    }

    /**
     * Convert the EqEffect parameters into band levels that are understood by the effects engine.
     * @return an array containing the corresponding band levels
     */
    private short[] convertParamsToSettings() {
        // TODO Update this based upon point (conversion function)
        short bands[] = new short[5];
        bands[0] = (short)-1500;
        bands[1] = (short)-1500;
        bands[2] = (short)-1500;
        bands[3] = (short)-1500;
        bands[4] = (short)-1500;

        // these are the x values of all eq points (including non-visible guide points),
        // in order of ascending x
        float[] allPointsXVals = {
                Math.max((float) (eqPoints[0].x - 0.125), 0f),
                eqPoints[0].x,
                Math.min((float) (eqPoints[0].x + 0.125), 1f),
                Math.max((float) (eqPoints[1].x - 0.125), 0f),
                eqPoints[1].x,
                Math.min((float) (eqPoints[1].x + 0.125), 1f)
        };
        Arrays.sort(allPointsXVals);

        // TODO: hook this part up to the GraphShape business
        for (short i = 0; i < bands.length; i++) {
            // We're modeling the EQ settings as triangles whose peaks/troughs are
            // the eqPoints.  For each band, we calculate the area under that triangle
            // and set the band level (height) such that the band's area equals the area
            // under that curve.
            //int[] bounds = eq.getBandFreqRange(i);
            //short triangle1Width = Math.max(0, )
        }
        return bands;
    }

    // used for calculating band levels.
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

        private float getYatX(float x) {
            return this.y1 - (this.slope * (this.x1 - x));
        }
        private int getSign() {
            if (y1 > 0 || y2 > 0 || (y1 == 0 && y2 == 0)) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    // Stored in Track table for effect params and on off
    public String encodeParameters() {
        if (eqPoints == null) {
            return "NULL";
        }
        String retVal = new String();
        if ( this.effect.getEnabled() ) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        retVal += ":";
        retVal += eqPoints[0].x + ",";
        retVal += eqPoints[0].y + ",";
        retVal += eqPoints[1].x + ",";
        retVal += eqPoints[1].y;
        Log.d(TAG, "EQ string is: " + retVal);
        return retVal;
    }

    /**
     * Enable the effect.
     */
    public void enable() {
        Log.d("EQ", "Enabled EQ effect");
        this.effect.setEnabled(true);
        isActive = true;
    }

    /**
     * Disable the effect.
     */
    public void disable() {
        Log.d("EQ", "Disabled EQ effect");
        this.effect.setEnabled(false);
        isActive = false;
    }

    public String getTitleString() {
        return "Equalizer";
    }

    /**
     * For getting point values to be used in UI
     * @return
     */
    public PointF[] getPointVals() {
//        Log.d("EqEfect", "Returning eq effect values...");
        return eqPoints;
    }

    /**
     * For setting the point values coming from UI
     * @param points
     */
    public void setPointVals(PointF[] points) {
//        Log.d("EqEfect", "Setting eq effect values to...");
        if (eqPoints == null) {
            this.eqPoints = new PointF[2];
        }
        this.eqPoints[0] = points[0];
        this.eqPoints[1] = points[1];
//        Log.d("EqEffect", eqPoints[0].x + ", " + eqPoints[0].y + "),(" + eqPoints[1].x + ", " + eqPoints[1].y + ")");
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
        //
    }
}
