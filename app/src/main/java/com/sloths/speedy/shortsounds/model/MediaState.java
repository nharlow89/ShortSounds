package com.sloths.speedy.shortsounds.model;

/**
 * This enum helps for keeping track of the state of a MediaPlayer. Because the MediaPlayer
 * class itself does not have a way of determining it's state we have to use this helper.
 */
public enum MediaState {
    INITIALIZED,
    PREPARED,
    PREPARING,
    STARTED,
    STOPPED
}
