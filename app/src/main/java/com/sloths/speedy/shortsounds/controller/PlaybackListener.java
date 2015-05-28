package com.sloths.speedy.shortsounds.controller;

import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Created by nj on 5/15/15.
 */
public interface PlaybackListener {

    boolean onPlayToggle();
    void onRecordStart();
    void onRecordStop( ShortSound sound );
    void soloTrack(int track);
    boolean isTrackSolo(int track);
    void volumeChanged(int track, float volume);
    void updateCurrentPosition(int position);
    void muteEffect(Effect.Type effect, int track);
    void turnOnEffect(Effect.Type effect, int track);
}
