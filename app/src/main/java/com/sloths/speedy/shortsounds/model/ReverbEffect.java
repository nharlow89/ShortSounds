package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.audiofx.EnvironmentalReverb;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.Effect;

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
        Log.d("effects", "ReverbEffect initialized from scratch");
        this.active = false;
        this.pointVal = new PointF(DEFAULT_X, DEFAULT_Y);
        //initAudioEffect();
        repInvariant();
    }

    // Constructor used when loading an effect from the database
    public ReverbEffect(String effectVals) {
        // Parse string from DB to get point vals & on/off
        // Stored in DB as "ON/OFF:float,float,float,float"
        String[] params = effectVals.split(":");
        if (params[0].equals(ON)) {
            this.active = true;
        } else {
            this.active = false;
        }
        String[] pointVals = params[1].split(",");
        pointVal = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
        repInvariant();
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
        if (active) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        retVal += ":";
        retVal += pointVal.x + ",";
        retVal += pointVal.y;
        return retVal;
    }

    // TODO: grab converted values from point
//    private void initAudioEffect() {
//        this.effect = new EnvironmentalReverb(0, player.getAudioSessionId());
//        ((EnvironmentalReverb)effect).setDecayTime(DEFAULT_DECAY);
//        ((EnvironmentalReverb)effect).setReflectionsLevel((short) DEFAULT_REFLECTION_LEVEL);
//        ((EnvironmentalReverb)effect).setReflectionsDelay((short) DEFAULT_REFLECTION_DELAY);
//        ((EnvironmentalReverb)effect).setDensity((short)DEFAULT_DENSITY);
//    }

    /***
     * Prepares the effect
     */
    @Override
    public void prepare() {
        if (effect == null) {
//            initAudioEffect();
        }
    }

    /**
     * Enables the effect
     */
    public void enable() {
//        initAudioEffect();
        effect.setEnabled(true);
    }

    /**
     * Disables the effect
     */
    public void disable() {
        effect.setEnabled(false);
        effect.release();
        effect = null;
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
        Log.d("ReverbEffect", "Setting reverb effect values");
        this.pointVal = point;
    }

    /**
     * A representation invariant of a reverb effect that essentially
     * holds the point value for a reverb effect.
     */
    private void repInvariant() {
        if (pointVal == null) throw new AssertionError("Invalid point value");
    }
}
