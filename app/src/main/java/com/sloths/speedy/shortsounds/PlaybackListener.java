package com.sloths.speedy.shortsounds;

import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Created by nj on 5/15/15.
 */
public interface PlaybackListener {

    boolean onPlayToggle();
    void onRecordStart();
    ShortSound onRecordStop( ShortSound sound );
    void soloTrack(int track);
    boolean isTrackSolo(int track);
    void updateCurrentPosition(int position);
    void muteEffect(Effect.Type effect, int track);
    void turnOnEffect(Effect.Type effect, int track);
}
