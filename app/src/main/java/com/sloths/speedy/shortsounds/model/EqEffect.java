package com.sloths.speedy.shortsounds.model;

import android.graphics.PointF;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class EqEffect extends Effect {
    // these INDEX constants are for accessing the values encoded in the string after
    // it has been split by "."
    private static final int INDEX_ACTIVE = 1;
    private static final int INDEX_BAND_LEVELS = 2;
    private static final int NUM_BANDS = 2;
    private static final String ON = "ON";

    private PointF[] eqPoints;
    // TODO: Change defaulted band levels to band levels generated by conversion from points
    private short[] bandLevels;

    // Constructor used when loading a track from a recorded file
    public EqEffect(MediaPlayer player) {
        this.active = false;
        this.player = player;
        this.eqPoints = null;
        effect = new Equalizer(0, player.getAudioSessionId());
        bandLevels = new short[NUM_BANDS];
        for (int i = 0; i < NUM_BANDS; i++) {
            bandLevels[i] = ((Equalizer)effect).getBandLevelRange()[1];
        }
        //initAudioEffect();
    }

    // Constructor used when loading an effect from the database
    public EqEffect(MediaPlayer player, String effectVals) {
        this.player = player;
        effect = new Equalizer(0, player.getAudioSessionId());
        bandLevels = new short[NUM_BANDS];
        for (int i = 0; i < NUM_BANDS; i++) {
            bandLevels[i] = ((Equalizer)effect).getBandLevelRange()[1];
        }

        // Parse string from DB to get point vals & on/off
        // Stored in DB as "ON/OFF:float,float,float,float"
        String[] params = effectVals.split(":");
        if (params[0].equals(ON)) {
            this.active = true;
        } else {
            this.active = false;
        }
        String[] pointVals = params[1].split(",");
        eqPoints = new PointF[2];
        eqPoints[0] = new PointF(new Float(pointVals[0]), new Float(pointVals[1]));
        eqPoints[1] = new PointF(new Float(pointVals[2]), new Float(pointVals[3]));
    }


    // Stored in Track table for effect params and on off
    public String encodeParameters() {
        if (eqPoints == null) {
            return "NULL";
        }
        String retVal = new String();
        if (active) {
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

    private void initAudioEffect() {
        for (int i = 0; i < bandLevels.length; i++){
            ((Equalizer)effect).setBandLevel((short)i, bandLevels[i]);
        }
    }

    @Override
    public void prepare() {
        if (effect == null) {
            initAudioEffect();
        }
    }

    public void enable() {
        initAudioEffect();
        effect.setEnabled(true);
    }

    public void disable() {
        effect.setEnabled(false);
        effect.release();
        effect = null;
    }

    /**
     * This method sets the amplitude for a given frequency band
     * @param band The band to adjust
     * @param level The new level for that band
     */
    public void setBandLevel(int band, short level) {
        if (band < 0 || band > bandLevels.length) {
            throw new IllegalArgumentException("Illegal band index argument: " + band);
        }
        bandLevels[band] = level;
    }

    /**
     * This method gets the level for a particular frequency band
     * @param band The band in question
     * @return The amplitude level of that band
     */
    public short getBandLevel(int band) {
        if (band < 0 || band > bandLevels.length) {
            throw new IllegalArgumentException("Illegal band index argument: " + band);
        }
        return bandLevels[band];
    }

    public String getTitleString() {
        return "Equalizer";
    }

    /**
     * For getting point values to be used in UI
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
            eqPoints = new PointF[2];
        }
        eqPoints[0] = points[0];
        eqPoints[1] = points[1];
    }
}
