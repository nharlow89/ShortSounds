package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class ReverbEffect extends ShortSoundTrackEffect {
    public String encodeParameters() {
        return null;
    }

    /**
     * Sets the reverb decay time in milliseconds
     * @param time The delay time in milliseconds
     */
    public void setDecay(int time) {

    }

    /**
     * Gets the decay time setting
     * @return The decay time in milliseconds TODO: is milliseconds correct?
     */
    public int getDecay() {
        return 0;
    }

    /**
     * Sets the delay and level of reverb
     * @param delay The time delay in milliseconds between reflections
     * @param level The amplitude level of the reflections
     */
    public void setReflections(int delay, int level) {

    }

    /**
     * Gets the delay time between reflections
     * @return Delay time in milliseconds
     */
    public int getReflectionDelay() {
        return 0;
    }

    /**
     * Gets the amplitude level of reflections
     * @return Current reflection level value
     */
    public int getReflectionLevel() {
        return 0;
    }

    /**
     * Sets the reverb density level
     * @param density The new density level
     */
    public void setDensity(int density) {

    }

    /**
     * Gets the current density setting
     * @return The current density level
     */
    public int getDensity() {
        return 0;
    }
}
