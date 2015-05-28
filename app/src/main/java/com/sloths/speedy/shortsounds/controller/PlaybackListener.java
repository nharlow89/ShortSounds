package com.sloths.speedy.shortsounds.controller;

import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Interface for a playback listener
 */
public interface PlaybackListener {

    /**
     * Determines whether or not it is currently playing
     * @return true if playing, false otherwise
     */
    boolean onPlayToggle();

    /**
     * Starts recording
     */
    void onRecordStart();

    /**
     * Stops recording
     * @param sound the current active ShortSound
     * @return The active ShortSound if it created a new ShortSound after
     * recording the first track, false otherwise
     */
    ShortSound onRecordStop( ShortSound sound );

    /**
     * Solos a track
     * @param track the track to solo
     */
    void soloTrack(int track);

    /**
     * Determines if track solo
     * @param track track to check
     * @return true if solo, false otherwise
     */
    boolean isTrackSolo(int track);

    /**
     * Changes the volume
     * @param track track to change volume on
     * @param volume desired volume
     */
    void volumeChanged(int track, float volume);

    /**
     * Updates the current position in the track
     * @param position desired position
     */
    void updateCurrentPosition(int position);

    /**
     * Mutes an effect
     * @param effect Effect type to mute
     * @param track Track to mute the effect on
     */
    void muteEffect(Effect.Type effect, int track);

    /**
     * Turns effect on
     * @param effect Effect type to turn on
     * @param track Track on which to apply effect
     */
    void turnOnEffect(Effect.Type effect, int track);
}
