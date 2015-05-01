package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class ReverbEffect extends ShortSoundTrackEffect {
    private static final int ACTIVE = 1;
    private static final int DECAY = 2;
    private static final int REFLECTION_DELAY = 3;
    private static final int REFLECTION_LEVEL = 4;
    private static final int DENSITY = 5;

    private int decay;
    private int reflectDelay;
    private int reflectLevel;
    private int density;

    public String encodeParameters() {
        String retVal = "REVERB:";
        if (active) {
            retVal += "ON";
        } else {
            retVal += "OFF";
        }
        return retVal + "," + decay + "," + reflectDelay + "," + reflectLevel + "," + density;
    }

    public static ShortSoundTrackEffect parseParameters(String[] parameters) {
        ReverbEffect retVal = new ReverbEffect(parameters[ACTIVE].equals("ON"),
                Integer.parseInt(parameters[DECAY]),
                Integer.parseInt(parameters[REFLECTION_DELAY]),
                Integer.parseInt(parameters[REFLECTION_LEVEL]),
                Integer.parseInt(parameters[DENSITY]));
        return retVal;
    }

    private ReverbEffect(boolean active, int decay, int reflectDelay, int reflectLevel, int density) {
        this.active = active;
        this.decay = decay;
        this.reflectLevel = reflectLevel;
        this.reflectDelay = reflectDelay;
        this.density = density;
    }

    /**
     * Sets the reverb decay time in milliseconds
     * @param time The delay time in milliseconds
     */
    public void setDecay(int time) {
        this.decay = time;
    }

    /**
     * Gets the decay time setting
     * @return The decay time in milliseconds TODO: is milliseconds correct?
     */
    public int getDecay() {
        return decay;
    }

    /**
     * Sets the delay and level of reverb
     * @param delay The time delay in milliseconds between reflections
     * @param level The amplitude level of the reflections
     */
    public void setReflections(int delay, int level) {
        this.reflectDelay = delay;
        this.reflectLevel = level;
    }

    /**
     * Gets the delay time between reflections
     * @return Delay time in milliseconds
     */
    public int getReflectionDelay() {
        return reflectDelay;
    }

    /**
     * Gets the amplitude level of reflections
     * @return Current reflection level value
     */
    public int getReflectionLevel() {
        return reflectLevel;
    }

    /**
     * Sets the reverb density level
     * @param density The new density level
     */
    public void setDensity(int density) {
        this.density = density;
    }

    /**
     * Gets the current density setting
     * @return The current density level
     */
    public int getDensity() {
        return density;
    }
}
