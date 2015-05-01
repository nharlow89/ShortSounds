package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class EqEffect extends ShortSoundTrackEffect {
    private EffectName title;

    public EqEffect() {
        this.title = EffectName.EqEffect;
    }

    public String encodeParameters() {
        return null;
    }

    /**
     * This method sets the amplitude for a given frequency band
     * @param band The band to adjust
     * @param level The new level for that band
     */
    public void setBandLevel(short band, short level) {

    }

    /**
     * This method gets the level for a particular frequency band
     * @param band The band in question
     * @return The amplitude level of that band
     */
    public short getBandLevel(short band) {
        return 0;
    }

    public String getTitleString() {
        return "Equalizer";
    }
}
