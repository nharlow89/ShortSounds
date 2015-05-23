package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.audiofx.EnvironmentalReverb;
import android.util.Log;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class ReverbEffect extends Effect {
    // these INDEX constants are for accessing the values encoded in the string after
    // it has been split by "."

    private static final int DEFAULT_DECAY = 1;
    private static final int DEFAULT_REFLECTION_DELAY = 150;
    private static final int DEFAULT_REFLECTION_LEVEL = 5000;
    private static final int DEFAULT_DENSITY = 500;
    private static final String ON = "ON";
    private static final float DEFAULT_X = 0.5f;
    private static final float DEFAULT_Y = 0.5f;
    private static final String TAG = "REVERB-EFFECT";

    private PointF pointVal;
    private boolean isActive;



    // Constructor used when loading a track from a recorded file
    public ReverbEffect() {
        // Sets up default reverb until track player sets it
//        this.effect = new EnvironmentalReverb(0, 0);
        this.pointVal = new PointF(DEFAULT_X, DEFAULT_Y);
        isActive = false;
//        effect.setEnabled( false );
        repInvariant();
    }

    // Constructor used when loading an effect from the database
    public ReverbEffect(String effectVals) {
        this();  // Setup from other constructor.
        if ( !effectVals.equals( "NULL" ) ) {
            // Parse string from DB to get point vals & on/off
            // Stored in DB as "ON/OFF:float,float,float,float"
            String[] params = effectVals.split(":");
            isActive = params[0].equals(ON);
//            effect.setEnabled( isActive );
            String[] pointVals = params[1].split(",");
            pointVal = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
        }
        repInvariant();
    }


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

    private void setEffectProperties() {
        Log.d(TAG, "Setting reverb effect properties");
        EnvironmentalReverb.Settings revSettings = convertParamsToSettings();
        EnvironmentalReverb reverb = (EnvironmentalReverb) effect;
        reverb.setProperties(revSettings);
    }

    private EnvironmentalReverb.Settings convertParamsToSettings() {
        EnvironmentalReverb.Settings revSettings = new EnvironmentalReverb.Settings();
        // Constant settings
        revSettings.reverbLevel = (short) 2000;
        revSettings.density = (short) 500;
        revSettings.diffusion = (short) 0;
        revSettings.roomLevel = (short) 0; // master volume of reverb
        revSettings.roomHFLevel = (short) 0; // controls a low-pass filter that will reduce the level of the high-frequency
        revSettings.decayHFRatio =  1000; // The valid range is [100, 2000]. A ratio of 1000 indicates that all frequencies decay at the same rate.

        // Dynamic x settings
        revSettings.reverbDelay =  (int) (pointVal.x * (float)100); // how long for reverb to kick in (ms) [0, 100]
        revSettings.reflectionsDelay = (int) (pointVal.x * (float)300); // size of room (ms) int [0, 300]
        revSettings.decayTime = (int) (pointVal.x * (float)20000); // time for reverb to die out (ms) int [100, 20000]
        // Dynamic y settings
        float yVal = 1.0f - pointVal.y;
        revSettings.reflectionsLevel = (short) (yVal * (float) 1000);// volume of early reflections short [-9000, 1000]
        Log.d("Reverb", "rev x settings are set as " + revSettings.reverbDelay +","+revSettings.reflectionsDelay+","+revSettings.decayTime);
        Log.d("Reverb", "rev y settings are set as " + revSettings.reflectionsLevel);
        return revSettings;
    }

    /**
     * This encodes the paramaters according to "ON/OFF:x,y" or "NULL"
     * It is used when storing the reverb effect parameters in the databse
     * @return
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
        Log.d(TAG, "Reverb string is: " + retVal);
        return retVal;
    }

    /**
     * Enables the effect
     */
    public void enable() {
        Log.d("Reverb", "Enabled Reverb effect");
        effect.setEnabled(true);
        isActive = true;
    }

    /**
     * Disables the effect
     */
    public void disable() {
        Log.d("Reverb", "Disabled Reverb effect");
        effect.setEnabled(false);
        isActive = false;
    }

    /**
     * Gets the title for the reverb effect
     * @return
     */
    public String getTitleString() {
        return "Reverb";
    }

    /**
     * For getting the point value stored to be loaded into UI
     * @return
     */
    public PointF[] getPointVal() {
        if (pointVal == null) {
            return null;
        }
        return new PointF[]{pointVal};
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
        if (pointVal == null) throw new AssertionError("Invalid point value");
    }
}
