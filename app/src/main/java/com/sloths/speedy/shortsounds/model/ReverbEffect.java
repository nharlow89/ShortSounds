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

    private PointF pointVal;


    // Constructor used when loading a track from a recorded file
    public ReverbEffect() {
        this.pointVal = new PointF(DEFAULT_X, DEFAULT_Y);
        effect = new EnvironmentalReverb( 0, 0 );
        effect.setEnabled( false );
        setEffectProperties();  // Setup initial properties.
        repInvariant();
    }

    // Constructor used when loading an effect from the database
    public ReverbEffect(String effectVals) {
        this();  // Setup from other constructor.
        boolean active = false;
        if ( !effectVals.equals( "NULL" ) ) {
            // Parse string from DB to get point vals & on/off
            // Stored in DB as "ON/OFF:float,float,float,float"
            String[] params = effectVals.split(":");
            active = params[0].equals(ON);
            String[] pointVals = params[1].split(",");
            pointVal = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
            setEffectProperties();
        }
        effect.setEnabled( active );
        repInvariant();
    }

    private void setEffectProperties() {
        EnvironmentalReverb.Settings eReverb = convertParamsToSettings();
        // TODO: remove after conversion function is implemented
        eReverb.decayHFRatio = (short) 1000;
        eReverb.decayTime = 10000;
        eReverb.density = (short) 1000;
        eReverb.diffusion = (short) 1000;
        eReverb.reverbLevel = (short) 1000;
        eReverb.reflectionsDelay = 100;
        EnvironmentalReverb reverb = (EnvironmentalReverb) effect;
        reverb.setProperties( eReverb );
//        enable();
    }

    private EnvironmentalReverb.Settings convertParamsToSettings() {
        return new EnvironmentalReverb.Settings(); // TODO
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
        if ( getEnabled() ) {
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
     * Enables the effect
     */
    public void enable() {
        effect.setEnabled(true);
    }

    /**
     * Disables the effect
     */
    public void disable() {
        effect.setEnabled(false);
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
