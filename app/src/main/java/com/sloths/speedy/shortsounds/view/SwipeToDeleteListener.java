package com.sloths.speedy.shortsounds.view;

/**
 * Simple interface for event handlers in the TrackSwipeListener.
 */
public interface SwipeToDeleteListener {

    /**
     * Handles the event when a track is deleted
     */
    public void onTrackDelete();

    /**
     * Handles the event when a track title is edited
     */
    public void onEditTrackTitle();
}
