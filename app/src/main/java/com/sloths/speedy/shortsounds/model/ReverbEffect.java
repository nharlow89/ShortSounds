package com.sloths.speedy.shortsounds.model;

import android.media.MediaPlayer;
import android.media.audiofx.EnvironmentalReverb;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.Effect;

/**
 * Created by caseympfischer on 4/28/15.
 */
public class ReverbEffect extends Effect {
    // these INDEX constants are for accessing the values encoded in the string after
    // it has been split by "."
    private static final int INDEX_ACTIVE = 1;
    private static final int INDEX_DECAY = 2;
    private static final int INDEX_REFLECTION_DELAY = 3;
    private static final int INDEX_REFLECTION_LEVEL = 4;
    private static final int INDEX_DENSITY = 5;

    private static final int DEFAULT_DECAY = 1;
    private static final int DEFAULT_REFLECTION_DELAY = 150;
    private static final int DEFAULT_REFLECTION_LEVEL = 5000;
    private static final int DEFAULT_DENSITY = 500;

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

    public static Effect parseParameters(String[] parameters, MediaPlayer player) {
        ReverbEffect retVal = new ReverbEffect(player,
                parameters[INDEX_ACTIVE].equals("ON"),
                Integer.parseInt(parameters[INDEX_DECAY]),
                Integer.parseInt(parameters[INDEX_REFLECTION_DELAY]),
                Integer.parseInt(parameters[INDEX_REFLECTION_LEVEL]),
                Integer.parseInt(parameters[INDEX_DENSITY]));
        return retVal;
    }

    private ReverbEffect(MediaPlayer player, boolean active, int decay, int reflectDelay,
                         int reflectLevel, int density) {
        Log.d("effects", "ReverbEffect initialized from loaded state");
        this.player = player;
        this.active = active;
        this.decay = decay;
        this.reflectLevel = reflectLevel;
        this.reflectDelay = reflectDelay;
        this.density = density;
        //initAudioEffect();
    }

    public ReverbEffect(MediaPlayer player) {
        Log.d("effects", "ReverbEffect initialized from scratch");
        this.player = player;
        this.active = false;
        this.decay = DEFAULT_DECAY;
        this.reflectLevel = DEFAULT_REFLECTION_LEVEL;
        this.reflectDelay = DEFAULT_REFLECTION_DELAY;
        this.density = DEFAULT_DENSITY;
        //initAudioEffect();
    }

    private void initAudioEffect() {
        this.effect = new EnvironmentalReverb(0, player.getAudioSessionId());
        ((EnvironmentalReverb)effect).setDecayTime(this.decay);
        ((EnvironmentalReverb)effect).setReflectionsLevel((short) this.reflectLevel);
        ((EnvironmentalReverb)effect).setReflectionsDelay((short) this.reflectDelay);
        ((EnvironmentalReverb)effect).setDensity((short)this.density);
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

    public String getTitleString() {
        return "Reverb";
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
