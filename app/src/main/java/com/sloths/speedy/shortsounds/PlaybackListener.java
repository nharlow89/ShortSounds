package com.sloths.speedy.shortsounds;

import com.sloths.speedy.shortsounds.model.ShortSound;

/**
 * Created by nj on 5/15/15.
 */
public interface PlaybackListener {

    public boolean onPlayToggle();
    public void onRecordStart();
    public ShortSound onRecordStop( ShortSound sound );
    public void soloOn();
    public void soloOff();
    public void updateCurrentPosition(int position);
}
