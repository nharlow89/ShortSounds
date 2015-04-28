package com.sloths.speedy.shortsounds;

/**
 * Created by caseympfischer on 4/28/15.
 */
public abstract class ShortSoundTrackEffect {
    private boolean active;
    private String title;
    private long id;

    public abstract void enable();

    public abstract void disable();

    public abstract String getTitle();

    public abstract void parseParameters();

    public abstract ShortSoundTrackEffect parseParameters(String parameters);

    public abstract String encodeParameters(ShortSoundTrackEffect effect);
}
