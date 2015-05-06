package com.sloths.speedy.shortsounds.model;

/**
 * This class helps for keeping track of the state of a MediaPlayer. Because the MediaPlayer
 * class itself does not have a way of determining it's state we have to use this helper.
 */
public class MediaState {
    public static final int INITIALIZED = 0;
    public static final int PREPARED = 1;
    public static final int PREPARING = 2;
    public static final int STARTED = 3;
    public static final int STOPPED = 4;

    // TODO: add states as needed.

    public int currentState;

    public MediaState() {
        currentState = INITIALIZED;
    }
}
