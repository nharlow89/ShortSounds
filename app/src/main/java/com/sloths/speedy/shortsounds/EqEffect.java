package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class EqEffect extends Effect {
    // these constants are for accessing the values encoded in the string after
    // it has been split by "."
    private static final int ACTIVE = 1;
    private static final int BAND_LEVELS = 2;

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

    /**
     * This method sets the amplitude for a given frequency band
     * @param band The band to adjust
     * @param level The new level for that band
     */
    public void setBandLevel(int band, short level) {
        if (band < 0 || band > bandLevels.length) {
            throw new IllegalArgumentException("Only two EQ frequency bands are permitted.");
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
            throw new IllegalArgumentException("Only two EQ frequency bands are permitted.");
        }
        return bandLevels[band];
    public short getBandLevel(short band) {
        return 0;
    }

    public String getTitleString() {
        return "Equalizer";
    }
}
