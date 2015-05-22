package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.audiofx.Equalizer;
import android.util.Log;

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
        PointF lo = new PointF(DEFAULT_X1, DEFAULT_Y);
        PointF hi = new PointF(DEFAULT_X2, DEFAULT_Y);
        this.eqPoints = new PointF[]{lo, hi};
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
//            this.effect.setEnabled( isActive );
//            Log.d(TAG, "Loaded eq from DB: " +effectVals);
//            Log.d(TAG, "Is active? " + isActive);
//            this.effect.setEnabled( isActive );
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
     * Set the properties of the Equalizer effect class.
     */
    private void setEffectProperties() {
        short bandLevels[] = convertParamsToSettings();
        Equalizer eq = (Equalizer) this.effect;
        for (int i = 0; i < eq.getNumberOfBands(); i++) {
            Log.d(TAG, "set band["+i+"] to level[-1500], previous level["+eq.getBandLevel((short)i)+"]");
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
        return bands;
    }

    // Stored in Track table for effect params and on off
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
        Log.d(TAG, "EQ string is: " + retVal);
        return retVal;
    }

    /**
     * Enable the effect.
     */
    public void enable() {
        if ( this.effect == null ) {
            Log.e(TAG, "Error trying to enable EQ effect that is null");
        } else {
            Log.d(TAG, "Enabled EQ effect");
            this.effect.setEnabled(true);
            isActive = true;
        }
    }

    /**
     * Disable the effect.
     */
    public void disable() {
        if ( this.effect == null ) {
            Log.e(TAG, "Error trying to disable EQ effect that is null");
        } else {
            Log.d(TAG, "Disabled EQ effect");
            this.effect.setEnabled(false);
            isActive = false;
        }
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
