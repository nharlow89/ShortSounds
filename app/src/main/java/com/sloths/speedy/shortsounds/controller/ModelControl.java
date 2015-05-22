package com.sloths.speedy.shortsounds.controller;

import android.util.Log;
import android.widget.SeekBar;

import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.File;

/**
 * Created by nj on 5/15/15.
 */

/**
 * A ModelControl is a controller that ties the backend to the front end. A
 * ModelControl is a SINGLETON.
 */
public class ModelControl implements PlaybackListener {
    public static final String TAG = "ModelControl";
    private AudioPlayer mAudioPlayer;
    private AudioRecorder mAudioRecorder;
    private int seekBarPosition;
    private static ModelControl instance = null;
    private SeekBar mGlobalSeekBar;


    /**
     * Private Constructor for a ModelControl
     */
    private ModelControl() {
        seekBarPosition = 0;
    }

    /**
     * Provides a singleton instance of a ModelControl
     * @return ModelControl singleton
     */
    public static ModelControl instance() {
        if (instance == null)
            instance = new ModelControl();
        return instance;
    }

    //returns true ifPlaying and non null;
    @Override
    public boolean onPlayToggle() {
        boolean isPlaying = mAudioPlayer != null && mAudioPlayer.isPlayingAll();
        if ( isPlaying ) {
            mAudioPlayer.pauseAll();
        } else {
            mAudioPlayer.playAll(seekBarPosition);
        }
        return isPlaying;
    }

    @Override
    public void onRecordStart() {
//        main.onRecordStart();
        if (mAudioPlayer != null)
            mAudioPlayer.playAll( 0 );  // Play from the beginning
        // Setup the MediaRecorder
        mAudioRecorder.start();
    }

    @Override
    public ShortSound onRecordStop( ShortSound mActiveShortSound ) {
//        main.onRecordStop( null );
        File recordedFile = mAudioRecorder.end();
        Log.d("DEBUG", "endRecording() recordedFile: " + recordedFile.getAbsolutePath());

        boolean isNewShortSound = mActiveShortSound == null;
        if ( isNewShortSound ) {
            // Case 1. There is no active ShortSound, create one and continue.
            // Create the new ShortSound and add it the list.
            mActiveShortSound = new ShortSound();
            mAudioPlayer = new AudioPlayer( mActiveShortSound );
        } else {
            mAudioPlayer.stopAll();
        }
        Log.d("DEBUG", "Finished Recording new track to ShortSound[" + mActiveShortSound.getId() + "]");
        // Create the new ShortSoundTrack (that this will record to)
        ShortSoundTrack newTrack = new ShortSoundTrack( recordedFile, mActiveShortSound.getId() );
        mActiveShortSound.addTrack(newTrack);
        mAudioPlayer.addTrack(newTrack);
        mAudioRecorder.reset();  // Have to reset for the next recording
        if ( isNewShortSound )
            return mActiveShortSound;
        return null;
    }

    @Override
    public void updateCurrentPosition(int position) {
        this.seekBarPosition = position;
        if (mAudioPlayer != null) {
            if (!mAudioRecorder.isRecording()) {
                mAudioPlayer.stopAll();
            }
            boolean isOkToPlayAllWithNewPosition = mAudioPlayer.isPlayingAll() && !mAudioRecorder.isRecording();
            if (isOkToPlayAllWithNewPosition) {
                // TODO: This scenario is super buggy
                //mAudioPlayer.stopAll();
                //mAudioPlayer.playAll(this.seekBarPosition);
            }
        }
    }

    public void stopAllFromPlaying() {
        if (mAudioPlayer != null) mAudioPlayer.stopAll();
    }

    public void notifySeekBarOfChangeInPos(int position) {
        this.seekBarPosition = position;
        mGlobalSeekBar.setProgress(position);
    }

    public void setGlobalSeekBar(SeekBar sb) {
        mGlobalSeekBar = sb;
    }

    @Override
    public void muteEffect(Effect.Type effect, int track) {
        Log.i(TAG, "mute effect on track " + track);
    }

    @Override
    public void turnOnEffect(Effect.Type effect, int track) {
        Log.i(TAG, "turn on effect on track " + track);
    }

    @Override
    public void saveShortSoundTrack(int track) {

    }

    public void setmAudioPlayer(AudioPlayer mAudioPlayer) {
        this.mAudioPlayer = mAudioPlayer;
    }

    public void setmAudioRecorder(AudioRecorder mAudioRecorder) {
        this.mAudioRecorder = mAudioRecorder;
    }

    public boolean isRecording() {
        return mAudioRecorder.isRecording();
    }

    //TODO check for track solo
    public boolean isTrackSolo(int track) {
        return mAudioPlayer.isTrackSolo(track);
    }

    //TODO implement solo track
    public void soloTrack(int track) {
        mAudioPlayer.soloTrack(track);
    }

    public void volumeChanged(int track, float volume) { mAudioPlayer.volumeChanged(track, volume); }

    public void endOfTrack() {
        seekBarPosition = 0;
        mGlobalSeekBar.setProgress(0);
        onPlayToggle();
        // TODO: Update Play Button
    }
}
