package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public abstract class Effect {
    protected boolean active;
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
    public abstract String getTitleString();

    /**
     * This method is used for loading an effect from the string encoded in the
     * database. It parses the given string and returns the Effect
     * object that the string represents.
     * @param parameters The String as taken from the database
     * @return An instance of a subclass of Effect, representing a saved effect state
     */
    public static Effect parseParameters(String parameters) {
        return null;
    }

    /**
     * This method is used for creating a String encoding of an effect object that
     * can then be inserted into the database to save the effect state.
     * @return A String-encoded representation of the callee effect.
     */
    public abstract String encodeParameters();
}
