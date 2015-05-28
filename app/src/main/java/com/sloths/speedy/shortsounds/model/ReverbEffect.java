package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.audiofx.EnvironmentalReverb;
import android.util.Log;

/**
 * This is the backend model that represents the Reverb effect.
 * It stores two important pieces: The point value for the UI &
 * the actual EnvironmentalReverb AudioEffect for using the effect
 * with the TrackPlayer. It also holds logic for converting the
 * point value to the values for the EnvironmentalReverb.
 */
public class ReverbEffect extends Effect {

    private static final String ON = "ON";
    private static final float DEFAULT_X = 0.5f;
    private static final float DEFAULT_Y = 0.5f;
    private static final String TAG = "REVERB-EFFECT";
    private static final short DEFAULT_REVERB_LEVEL = 2000;
    private static final short DEFAULT_DIFFUSION = 0;
    private static final short DEFAULT_ROOM_LEVEL = 0;
    private static final short DEFAULT_DECAY_HF = 1000;
    private static final float DEFAULT_REVERB_DELAY = 100;
    private static final float DEFAULT_REFLECTIONS_DELAY = 300;
    private static final float DEFEAULT_DECAY_TIME = 20000;
    private static final float DEFAULT_REFLECTIONS_LEVEL = 1000;
    private static final int DEFAULT_DENSITY = 500;
    private static final String REVERB_TITLE = "Reverb";
    private PointF pointVal;
    private boolean isActive;


    /**
     * Creates a ReverbEffect
     */
    public ReverbEffect() {
        // Sets up default reverb until track player sets it
        this.pointVal = new PointF(DEFAULT_X, DEFAULT_Y);
        isActive = false;
        repInvariant();
    }

    /**
     * Constructor used when loading reverb effect from the database
     *  Stored in DB as "ON/OFF:float,float" or "NULL" w/o a value
     * @param effectVals
     */
    public ReverbEffect(String effectVals) {
        this();  // Setup from other constructor.
        if ( !effectVals.equals( "NULL" ) ) {
            // Parse string from DB to get point vals & on/off
            // Stored in DB as "ON/OFF:float,float,float,float"
            String[] params = effectVals.split(":");
            isActive = params[0].equals(ON);
            String[] pointVals = params[1].split(",");
            pointVal = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
        }
        repInvariant();
    }


    /**
     * This sets up the initial reverb effect
     * @param audioSessionId
     */
    public void setupReverbEffect(int audioSessionId) {
        Log.d(TAG, "Attaching reverb to track id [" + audioSessionId + "]");
        try {
            effect = new EnvironmentalReverb(0, audioSessionId);
            setEffectProperties();
            effect.setEnabled( isActive );
            Log.d(TAG, "Enabled [" + effect.getEnabled() + "]");
        } catch (Exception e) {
            Log.e(TAG, "MAJOR PROBLEM LOADING REVERB LIBRARY");
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Sets the current effect properties to be reflected by the current
     * point value
     */
    private void setEffectProperties() {
        if (effect != null) { //TODO deal with null effect
            EnvironmentalReverb.Settings revSettings = convertParamsToSettings();
            EnvironmentalReverb reverb = (EnvironmentalReverb) effect;
            reverb.setProperties(revSettings);
        }
    }

    /**
     * This grabs the current point value stored for reverb, &
     * does the weight conversions to create the actual settings to
     * be used for the reverb effect.
     * @return The reverb settings
     */
    private EnvironmentalReverb.Settings convertParamsToSettings() {
        EnvironmentalReverb.Settings revSettings = new EnvironmentalReverb.Settings();
        // Constant settings
        revSettings.reverbLevel = DEFAULT_REVERB_LEVEL;
        revSettings.density = DEFAULT_DENSITY;
        revSettings.diffusion = DEFAULT_DIFFUSION;
        revSettings.roomLevel = DEFAULT_ROOM_LEVEL; // master volume of reverb
        revSettings.roomHFLevel = DEFAULT_ROOM_LEVEL; // controls a low-pass filter that will reduce the level of the high-frequency
        revSettings.decayHFRatio =  DEFAULT_DECAY_HF; // The valid range is [100, 2000]. A ratio of 1000 indicates that all frequencies decay at the same rate.

        // Dynamic x settings
        revSettings.reverbDelay =  (int) (pointVal.x * DEFAULT_REVERB_DELAY); // how long for reverb to kick in (ms) [0, 100]
        revSettings.reflectionsDelay = (int) (pointVal.x * DEFAULT_REFLECTIONS_DELAY); // size of room (ms) int [0, 300]
        revSettings.decayTime = (int) (pointVal.x * DEFEAULT_DECAY_TIME); // time for reverb to die out (ms) int [100, 20000]

        // Dynamic y settings
        float yVal = 1.0f - pointVal.y;
        revSettings.reflectionsLevel = (short) (yVal * DEFAULT_REFLECTIONS_LEVEL);// volume of early reflections short [-9000, 1000]

        return revSettings;
    }

    /**
     * This encodes the paramaters according to "ON/OFF:x,y" or "NULL"
     * It is used when storing the reverb effect parameters in the databse
     * @return "ON:x,y" or "OFF:x,y" or "NULL" where the x and y are the x and y
     * values of the pointVal.  "NULL" indicates that pointVal is null
     * "ON:x,y" indicates the the effect is not null and the effects
     * are enabled.  "OFF:x,y" is anything else.
     */
    public String encodeParameters() {
        if (pointVal == null) {
            return "NULL";
        }
        String retVal = new String();
        if ( effect != null && effect.getEnabled() ) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        retVal += ":";
        retVal += pointVal.x + ",";
        retVal += pointVal.y;
        return retVal;
    }

    /**
     * Enables the actual Reverb effect
     */
    public void enable() {
        Log.d("Reverb", "Enabled Reverb effect");
        if (effect != null) {
            effect.setEnabled(true);
        }
        isActive = true;
        //TODO DO NOT ERASE
        //TODO deal with null case
    }

    /**
     * Disables the actual Reverb effect
     */
    public void disable() {
        Log.d("Reverb", "Disabled Reverb effect");
        if (effect != null) {
            effect.setEnabled(false);
        }
        isActive = false;
        //TODO DO NOT ERASE
        //TODO deal with null case
    }

    /**
     * Gets the title for the reverb effect for the UI
     * @return
     */
    public String getTitleString() {
        return REVERB_TITLE;
    }

    /**
     * For getting the point value stored to be loaded into UI
     * @return
     */
    public PointF getPointVal() {
        if (pointVal == null) {
            return null;
        }
        return pointVal;
    }

    /**
     * For setting the point value coming from the UI
     * @param point
     */
    public void setPointVal(PointF point) {
        this.pointVal = point;
        setEffectProperties();  // Update the effect params.
    }

    /**
     * For setting point back to default value if cancel/back is pressed
     */
    public void resetPointVal() {
        this.pointVal = new PointF(DEFAULT_X, DEFAULT_Y);
        setEffectProperties();
    }


    /**
     * A representation invariant of a reverb effect that essentially
     * holds the point value for a reverb effect.
     */
    private void repInvariant() {
        if (pointVal == null) {
            throw new AssertionError("Invalid point value");
        }
    }
}
