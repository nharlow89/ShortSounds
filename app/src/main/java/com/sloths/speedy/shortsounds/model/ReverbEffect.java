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

    private PointF pointVal;


    // Constructor used when loading a track from a recorded file
    public ReverbEffect() {
        Log.d("effects", "ReverbEffect initialized from scratch");
        this.active = false;
        this.pointVal = null;
        //initAudioEffect();
    }

    // Constructor used when loading an effect from the database
    public ReverbEffect(String effectVals) {
        if ( effectVals == "NULL" ) {
            // Default values
            this.active = false;
            this.pointVal = null;
        } else {
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
        }
    }

    /**
     * Set the audio source that this effect is related to. This is required for the effect to show
     * up on a given audio playback source.
     * @param audioSessionId
     */
    public void setAudioSource( int audioSessionId ) {
        effect = new EnvironmentalReverb(0, audioSessionId );
    }


    // Stored in Track table for effect values & being on / off
    public String encodeParameters() {
        if (pointVal == null) {
            return "NULL";
        }
        String retVal = "REVERB:";
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

    @Override
    public void prepare() {
        if (effect == null) {
//            initAudioEffect();
        }
    }

    public void enable() {
//        initAudioEffect();
        effect.setEnabled(true);
    }

    public void disable() {
        effect.setEnabled(false);
        effect.release();
        effect = null;
    }

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
}
