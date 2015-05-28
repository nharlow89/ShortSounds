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

    /**
     * Determines whether or not it is currently playing
     * @return true ifPlaying and audio player is non null, false otherwise
     */
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

    /**
     * Starts recording
     */
    @Override
    public void onRecordStart() {
//        main.onRecordStart();
        if (mAudioPlayer != null) {
            Log.d("Debug", "onRecordStart() playALL!!!");
            mAudioPlayer.playAll(0);  // Play from the beginning
        }
        // Setup the MediaRecorder
        mAudioRecorder.start();
    }

    /**
     * Stops recording
     * @param mActiveShortSound the current active ShortSound
     * @return The active ShortSound is this is the first track recorded, else null
     */
    @Override
    public void onRecordStop( ShortSound mActiveShortSound ) {
        File recordedFile = mAudioRecorder.end();
        mAudioPlayer.stopAll();
        // Create the new ShortSoundTrack (that this will record to)
        int nextTrackNum = mActiveShortSound.getNextTrackNumber();
        ShortSoundTrack newTrack = new ShortSoundTrack( recordedFile, mActiveShortSound.getId() );
        mActiveShortSound.addTrack(newTrack);
        mAudioPlayer.addTrack(newTrack);
        mAudioRecorder.reset();  // Have to reset for the next recording
    }

    /**
     * Updates the current position of the seek bar
     * @param position the position of the seek bar
     */
    @Override
    public void updateCurrentPosition(int position) {
        this.seekBarPosition = position;
        if (mAudioPlayer != null) {
            if (!mAudioRecorder.isRecording()) {
                mAudioPlayer.stopAll();
            }
        }
    }

    /**
     * Stops all of the tracks from playing
     */
    public void stopAllFromPlaying() {
        if (mAudioPlayer != null) {
            mAudioPlayer.stopAll();
        }
    }

    /**
     * Notifies the seek bar of a change in position
     * @param position the new position
     */
    public void notifySeekBarOfChangeInPos(int position) {
        this.seekBarPosition = position;
        mGlobalSeekBar.setProgress(position);
    }

    /**
     * Sets the global seek bar
     * @param sb a seek bar to set the global seek bar to
     */
    public void setGlobalSeekBar(SeekBar sb) {
        mGlobalSeekBar = sb;
    }

    /**
     * Mutes the effects
     * @param effect The type of effect to mute
     * @param track the track the effect is being muted on
     */
    @Override
    public void muteEffect(Effect.Type effect, int track) {
        mAudioPlayer.getTrack( track ).setEffectToggle(effect, false);
        Log.i(TAG, "mute " + effect.toString() +" on track " + track);
    }

    /**
     * Turns on an effect
     * @param effect The type of effect to turn on
     * @param track The track where the effect is being turned on
     */
    @Override
    public void turnOnEffect(Effect.Type effect, int track) {
        mAudioPlayer.getTrack( track ).setEffectToggle(effect, true);
        Log.i(TAG, "turn on effect on track " + track);

    }

    /**
     * Set the AudioPlayer that this controller will interact with.
     * @param mAudioPlayer the audio player to interact with the controller
     */
    public void setmAudioPlayer(AudioPlayer mAudioPlayer) {
        // Whenever setting the AudioPlayer, we should check if there is already an existing one.
        // If so, clean up any resources related to that AudioPlayer.
        if ( this.mAudioPlayer != null )
            this.cleanUpTheDirty();
        this.mAudioPlayer = mAudioPlayer;
    }

    /**
     * Sets the audio recorder to the given audio recorder
     * @param mAudioRecorder the audio recorder to set the recorder to
     */
    public void setmAudioRecorder(AudioRecorder mAudioRecorder) {
        this.mAudioRecorder = mAudioRecorder;
    }

    /**
     * Returns whether or not the app is currently recording
     * @return true if currently recording, false otherwise
     */
    public boolean isRecording() {
        return mAudioRecorder.isRecording();
    }

    /**
     * Checks for track solo
     * @param track the current track
     * @return true if track solo, false otherwise
     */
    //TODO check for track solo
    public boolean isTrackSolo(int track) {
        return mAudioPlayer.isTrackSolo(track);
    }

    /**
     * Solo Track
     * @param track the track to solo
     */
    //TODO implement solo track
    public void soloTrack(int track) {
        mAudioPlayer.soloTrack(track);
    }

    /**
     * Changes the volume of a track
     * @param track the track to change the volume on
     * @param volume the desired volume
     */
    public void volumeChanged(int track, float volume) {
        mAudioPlayer.volumeChanged(track, volume);
    }

    /**
     * Goes through and cleans up any resources that were
     * in use by the previously active shortsound.
     * This includes AudioTracks, AudioRecorders,
     * and Effect objects.
     */
    private void cleanUpTheDirty() {
        mAudioPlayer.destroy();
    }

    /**
     * Determines whether or not the audio player is playing
     * @return true if playing, false otherwise
     */
    public boolean isPlaying() {
        return mAudioPlayer.isPlayingAll();
    }

    /**
     * Removes a track
     * @param track track to remove
     */
    public void removeTrack(int track) {
        ShortSoundTrack sst = mAudioPlayer.getTrack(track);
        mAudioPlayer.removeTrack(sst);
    }

}
