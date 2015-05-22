package com.sloths.speedy.shortsounds.controller;

import android.content.Context;
import android.util.Log;
import android.widget.SeekBar;

import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.AudioRecorder;
import com.sloths.speedy.shortsounds.model.Effect;
import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;
import com.sloths.speedy.shortsounds.view.MainActivity;

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
        mAudioPlayer.getTrack( track ).setEffectToggle(effect, false);
        Log.i(TAG, "mute " + effect.toString() +" on track " + track);
    }

    @Override
    public void turnOnEffect(Effect.Type effect, int track) {
        mAudioPlayer.getTrack( track ).setEffectToggle(effect, true);
        Log.i(TAG, "turn on effect on track " + track);

    }

    @Override
    public void saveShortSoundTrack(int track) {

    }

    /**
     * Set the AudioPlayer that this controller will interact with.
     * @param mAudioPlayer
     */
    public void setmAudioPlayer(AudioPlayer mAudioPlayer) {
        // Whenever setting the AudioPlayer, we should check if there is already an existing one.
        // If so, clean up any resources related to that AudioPlayer.
        if ( this.mAudioPlayer != null )
            this.cleanUpTheDirty();
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

    /**
     * This method does what it says, it goes through and cleans up any resources that were
     * in use by the previously active shortsound. This includes AudioTracks, AudioRecorders,
     * and Effect objects.
     */
    private void cleanUpTheDirty() {
        mAudioPlayer.destroy();
    }
}
