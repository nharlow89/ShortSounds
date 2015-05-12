package com.sloths.speedy.shortsounds.model;

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
    //private static final int DEFAULT_BAND_LEVEL = 1; // TODO: this is not really a good default...

    private short[] bandLevels;

    public String encodeParameters() {
        String retVal = "EQ:";
        if (active) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        for (int i = 0; i < bandLevels.length; i++) {
            retVal += "." + bandLevels[i];
        }
        return retVal;
    }

    public static Effect parseParameters(String[] parameters, MediaPlayer player) {
        EqEffect retVal = new EqEffect(parameters, player);
        return retVal;
    }

    private EqEffect(String[] parameters, MediaPlayer player) {
        this.player = player;
        effect = new Equalizer(0, player.getAudioSessionId());
        this.active = parameters[INDEX_ACTIVE].equals("ON");
        for (int i = 0; i < NUM_BANDS; i++) {
            this.bandLevels[i] = Short.parseShort(parameters[INDEX_BAND_LEVELS + i]);
        }
        initAudioEffect();
    }

    private void initAudioEffect() {
        for (int i = 0; i < bandLevels.length; i++){
            ((Equalizer)effect).setBandLevel((short)i, bandLevels[i]);
        }
    }

    public EqEffect(MediaPlayer player) {
        this.active = false;
        this.player = player;
        effect = new Equalizer(0, player.getAudioSessionId());
        bandLevels = new short[NUM_BANDS];
        for (int i = 0; i < NUM_BANDS; i++) {
            bandLevels[i] = ((Equalizer)effect).getBandLevelRange()[1];
        }
        initAudioEffect();
    }

    public void enable() {
        effect.setEnabled(true);
    }

    public void disable() {
        effect.setEnabled(false);
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
}
