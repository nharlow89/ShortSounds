package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public abstract class ShortSoundTrackEffect {
    private boolean active;
    private EffectName title;
    private long id;
    public enum EffectName {EqEffect, Reverb};
    public android.media.audiofx.AudioEffect effect;

    /**
     * Turns on the effect
     */
    public void enable() {
        active = true;
    }

    /**
     * Turns off the effect
     */
    public void disable() {
        active = false;
    }

    /**
     * Returns the name of the effect
     * @return The name of the effect
     */
    public EffectName getTitle() {
        return title;
    }

    public String getTitleString() {
        if (title == EffectName.EqEffect) {
            return "Equalizer";
        } else {
            return "Reverb";
        }
    }

    /**
     * This method is used for loading an effect from the string encoded in the
     * database. It parses the given string and returns the ShortSoundTrackEffect
     * object that the string represents.
     * @param parameters The String as taken from the database
     * @return An instance of a subclass of ShortSoundTrackEffect, representing a saved effect state
     */
    public static ShortSoundTrackEffect parseParameters(String parameters) {
        return null;
    }

    /**
     * This method is used for creating a String encoding of an effect object that
     * can then be inserted into the database to save the effect state.
     * @return A String-encoded representation of the callee effect.
     */
    public abstract String encodeParameters();
}
