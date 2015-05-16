package com.sloths.speedy.shortsounds;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

/**
 * Created by nj on 5/15/15.
 */
public interface PlaybackListener {

    public boolean onPlayToggle();
    public void onRecordStart();
    public ShortSound onRecordStop( ShortSound sound );
    public void soloOn(int track);
    public void soloOff(int track);
    public void updateCurrentPosition(int position);

    public void muteEffect(ShortSoundTrack.EFFECT effect, int track);

    public void turnOnEffect(ShortSoundTrack.EFFECT effect, int track);
}
