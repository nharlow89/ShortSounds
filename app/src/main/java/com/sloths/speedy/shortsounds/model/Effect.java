package com.sloths.speedy.shortsounds.model;

/**
 * The abstract class that represents how the backend models for
 * the effects should be.  It also has a method for getting
 * if the effect is enabled.
 */
public abstract class Effect {

    public enum Type { EQ, REVERB }

    protected android.media.audiofx.AudioEffect effect;

    /**
     * Turns on the effect
     */
    public abstract void enable();
    /**
     * Turns off the effect
     */
    public abstract void disable();

    /**
     * Get the resource id associated with this effect.
     */
    public int getEffectId() {
        return effect.getId();
    }

    /**
     * Whether or not the effect is enabled.
     */
    public abstract boolean getEnabled();

    /**
     * Gets the title string
     * @return the title
     */
    public abstract String getTitleString();

    /**
     * This method is used for creating a String encoding of an effect object that
     * can then be inserted into the database to save the effect state.
     * @return A String-encoded representation of the callee effect.
     */
    public abstract String encodeParameters();

    /**
     * Release the effect for cleanup.
     */
    public void release() {
        if (this.effect != null) {
            this.effect.release();
            effect = null;
        }
    }
}
