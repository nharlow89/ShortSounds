package com.sloths.speedy.shortsounds.model;

import android.annotation.TargetApi;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

import com.sloths.speedy.shortsounds.controller.ModelControl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AudioPlayer class handles all the playing of ShortSounds/ShortSoundTracks.
 * This player is capable of playing multiple tracks or a single track.
 */
public class AudioPlayer {
    public static final String DEBUG_TAG = "SHORT_SOUNDS";
    private static final String TAG = "AudioPlayer";
    public Float MAX_VOLUME = 1.0f;
    public Float MIN_VOLUME = 0.0f;


    public static enum PlayerState { PLAYING_ALL, STOPPED_ALL, PAUSED_ALL };
    private static enum TrackState { PLAYING, PAUSED, STOPPED };
    private PlayerState playerState;

    private Map<ShortSoundTrack, TrackPlayer> trackPlayers;
    private List<ShortSoundTrack> tracks;
    private ShortSound mCurrentShortSound;
    private ShortSoundTrack mLongestTrack;
    private TrackPlayer mLongestTrackPlayer;
    private ModelControl mModelControl;

    public AudioPlayer( ShortSound ss ) {
        trackPlayers = new HashMap<>();

        tracks = new ArrayList<>();
        for ( ShortSoundTrack track : ss.getTracks() ) {
            trackPlayers.put(track, new TrackPlayer(track, this));
            tracks.add(track);
        }
        mModelControl = ModelControl.instance();
        mCurrentShortSound = ss;
        mLongestTrack = ss.getLongestTrack();
        for ( ShortSoundTrack sst : ss.getTracks() ) {
            TrackPlayer tp = new TrackPlayer(sst, this);
            trackPlayers.put(sst, tp);
            if (sst == mLongestTrack) {
                mLongestTrackPlayer = tp;
            }
        }
        playerState = PlayerState.STOPPED_ALL;
    }

    /**
     * Teardown and cleanup any resources that were in use by this AudioPlayer.
     */
    public void destroy() {
        Log.d(DEBUG_TAG, "Destroying AudioPlayer.");
        for ( Map.Entry<ShortSoundTrack, TrackPlayer> entry : trackPlayers.entrySet() ) {
            TrackPlayer trackPlayer = entry.getValue();
            trackPlayer.destroy();
        }
    }

    /**
     * Play all tracks that are in this AudioPlayer, starting from a given point
     * (an integer percentage of the total length).
     */
    public void playAll( int position ) {
        Log.d(DEBUG_TAG, "Play all tracks starting at ["+position+"%]");
        if (mCurrentShortSound.getLongestTrack() == null) {
            return;
        }
        long longestTrackMaxByteOffset = mCurrentShortSound.getLongestTrack().getLengthInBytes();
        long longestBytePosition = (longestTrackMaxByteOffset * position) / 100 ;
        //if (longestBytePosition % 2 == 1) longestBytePosition--;
        Log.d(DEBUG_TAG, "longestTrackMaxByteOffset ["+longestTrackMaxByteOffset+"]");
        Log.d(DEBUG_TAG, "longestBytePosition ["+longestBytePosition+"]");

        for ( Map.Entry<ShortSoundTrack, TrackPlayer> entry: trackPlayers.entrySet() ) {
            entry.getValue().stop();

            if (position == 0) {
                entry.getValue().play(position);
            } else {
                ShortSoundTrack currentTrack = entry.getKey();
                long currentTrackMaxByteOffset = currentTrack.getLengthInBytes();
                boolean isPlayable = currentTrackMaxByteOffset >= longestBytePosition;
                if ( isPlayable ) {
                    // play with a normalized position
                    Log.d(DEBUG_TAG, "DIV " + longestBytePosition + "/" + currentTrackMaxByteOffset);
                    double normalizedPosition = ((double)longestBytePosition / (double)currentTrackMaxByteOffset) * 100.0 ;
                    Log.d(DEBUG_TAG, "normalized position ["+(int)Math.round(normalizedPosition)+"]");
                    entry.getValue().play((int)Math.round(normalizedPosition));
                }
            }
        }
        playerState = PlayerState.PLAYING_ALL;
    }

    private void notifyEndOfTrack(TrackPlayer notifier) {
        if (notifier == mLongestTrackPlayer) {
            mModelControl.endOfTrack();
        }
    }


    public void notifyModelControlOfTrackPosition(TrackPlayer notifier, int position) {
        if (notifier == mLongestTrackPlayer) {
            mModelControl.notifySeekBarOfChangeInPos(position);
        }
    }


    /**
     * Stop playing all tracks in this AudioPlayer. When a track is stopped it's position
     * is reset.
     */
    public void stopAll() {
        for ( Map.Entry<ShortSoundTrack, TrackPlayer> entry : trackPlayers.entrySet() ) {
            entry.getValue().stop();
        }
        playerState = PlayerState.STOPPED_ALL;
    }

    /**
     * Pause all tracks in this AudioPlayer. When a track is paused it's current position
     * is saved and the audio buffer remains in memory.
     */
    public void pauseAll() {
        for ( Map.Entry<ShortSoundTrack, TrackPlayer> entry : trackPlayers.entrySet() ) {
            entry.getValue().pause();
        }
        playerState = PlayerState.PAUSED_ALL;
    }

    /**
     * @return Boolean value for whether All tracks are being played.
     */
    public boolean isPlayingAll() {
        return playerState == PlayerState.PLAYING_ALL;
    }

    /**
     *
     * @param track position
     * @return true is solo enabled false otherwise
     */
    public boolean isTrackSolo(int track) {
        return tracks.get(track).isSolo();
    }


    /**
     * Solo's the track at the given position.  Solo by definition sets all track volumes to
     * zero for which solo is not enabled.
     * @param track position
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void soloTrack(int track) {
        tracks.get(track).toggleSolo();
        for (int i = 0; i < tracks.size(); i++) {
            ShortSoundTrack sst = tracks.get(i);
            TrackPlayer tp = trackPlayers.get(sst);
            if (!sst.isSolo())
                tp.audioTrack.setVolume(0.0f);
            else
                tp.audioTrack.setVolume(sst.getVolume());
        }
    }

    /**
     * changes the volume of a short sound on the given track.
     * @param track position
     * @param volume level
     */

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void volumeChanged(int track, float volume) {
        ShortSoundTrack sst = tracks.get(track);
        sst.setTrackVolume(volume);
        if (!sst.isSolo())
            trackPlayers.get(sst).audioTrack.setVolume(sst.getVolume());
        else
            trackPlayers.get(sst).audioTrack.setVolume(0.0f);
    }

    /**
     * Play a single track within this AudioPlayer.
     * @param track
     */
    public void playTrack( ShortSoundTrack track, int position ) {
        pauseAll();
        TrackPlayer targetPlayer = trackPlayers.get( track );
        targetPlayer.play(position);
    }

    /**
     * Pause a single track.
     * @param track
     */
    public void pauseTrack( ShortSoundTrack track ) {
        TrackPlayer targetPlayer = trackPlayers.get( track );
        targetPlayer.pause();
    }

    /**
     * Stop a single track.
     * @param track
     */
    public void stopTrack( ShortSoundTrack track ) {
        TrackPlayer targetPlayer = trackPlayers.get( track );
        targetPlayer.stop();
    }

    /**
     * Determine whether a given track is playing or not.
     * @param track Target ShortSoundTrack
     * @return true if playing, otherwise false
     */
    public boolean isPlayingTrack( ShortSoundTrack track ) {
        TrackPlayer player = trackPlayers.get( track );
        return player.trackState == TrackState.PLAYING;
    }

    /**
     * Add an additional ShortSoundTrack to this AudioPlayer.
     * @param track
     */
    public void addTrack( ShortSoundTrack track ) {
        tracks.add(track);
        TrackPlayer tp = new TrackPlayer(track, this);
        trackPlayers.put(track, tp);
        if(mLongestTrack != null && track.getLengthInBytes() >= mLongestTrack.getLengthInBytes()) {
            mLongestTrack = track;
            mLongestTrackPlayer = tp;
        } else if (mLongestTrack == null) {
            mLongestTrack = track;
        }
    }

    /**
     * Removes ShortSoundTrack from this AudioPlayer
     * @param track the ShortSoundTrack to be removed
     */
    public void removeTrack( ShortSoundTrack track ) {
        trackPlayers.remove(track);
        if(mLongestTrack == track) {
            // TODO: find new longest track
        }
    }

    /**
     * Returns the ShortSound associated with this AudioPlayer
     * @return ShortSound the Shortsound associate with this AudioPlayer
     */
    public ShortSound getCurrentShortSound() {
        return mCurrentShortSound;
    }

    /**
     * Getter for the state of the player
     * Used for testing
     * @return playerState as an Enum
     */
    public PlayerState getPlayerState() {
        return playerState;
    }

    /**
     * TrackPlayer represents an AudioPlayer for a single track. This class helps us keep track
     * of the state of any given ShortSoundTrack and acts as a helper to the AudioPlayer.
     */
    private class TrackPlayer {
        private ShortSoundTrack track;
        private AudioTrack audioTrack;
        private long trackLength;
        private byte[] audioTrackBuffer;
        private TrackState trackState;
        private File file;
        private InputStream audioInputStream;
        private Thread audioThread;
        private long currentTrackPosition;
        private AudioPlayer mAudioPlayerListener;

        public TrackPlayer( ShortSoundTrack track, AudioPlayer parent) {
            this.track = track;
            this.mAudioPlayerListener = parent;
            setupTrack();
        }

        /**
         * Set up an individual ShortSoundTrack. This includes creating the AudioTrack for playback
         * and creating a reference to the actual audio file.
         */
        public void setupTrack() {
            Log.d("AUDIO PLAYER", "Setting up audio track");
            ShortSoundTrack.BUFFER_SIZE = AudioTrack.getMinBufferSize(
                    ShortSoundTrack.SAMPLE_RATE,
                    ShortSoundTrack.CHANNEL_CONFIG,
                    ShortSoundTrack.AUDIO_FORMAT);
            audioTrack = new AudioTrack(
                    ShortSoundTrack.STREAM_TYPE,
                    ShortSoundTrack.SAMPLE_RATE,
                    ShortSoundTrack.CHANNEL_CONFIG,
                    ShortSoundTrack.AUDIO_FORMAT,
                    ShortSoundTrack.BUFFER_SIZE,
                    ShortSoundTrack.MODE);
            audioTrack.setPositionNotificationPeriod( ShortSoundTrack.SAMPLE_RATE );
            audioTrack.setPlaybackPositionUpdateListener( new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {}
                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    Log.d(DEBUG_TAG, "PlaybackListener");
                    // TODO: update seekbar

                }
            });
            audioTrackBuffer = new byte[ShortSoundTrack.BUFFER_SIZE * 2];
            trackState = TrackState.STOPPED;
            this.file = new File( ShortSoundTrack.STORAGE_PATH, track.getFileName() );
            this.trackLength = file.length();
            attachEffects();
        }

        /**
         * Cleanup any resources tied to this TrackPlayer.
         */
        public void destroy() {
            Log.d(DEBUG_TAG, "Destroy TrackPlayer associated with Track["+track.getId()+"]");
            // Take care of the input stream.
            if ( audioInputStream != null ) {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    Log.d(DEBUG_TAG, "Failed closing an existing AudioInputStream.");
                    e.printStackTrace();
                }
            }
            // Take care of the thread
            if ( audioThread != null )
                audioThread.interrupt();
            // Take care of the AudioTrack
            if ( audioTrack != null )
                audioTrack.release();
            // Cleanup any effect objects

        }

        /**
         * Private helper to attach all the effects associated with this track.
         */
        private void attachEffects () {
            // Reverb
            ReverbEffect reverb = track.getmReverbEffect();
            reverb.setupReverbEffect( audioTrack.getAudioSessionId() );
            Log.d("AudioPlayer", "Attached reverb to track id : " + audioTrack.getAudioSessionId());

            // Equalizer
            EqEffect eq = track.getmEqEffect();
            eq.setupEqEffect( audioTrack.getAudioSessionId() );

            // Important: set the volume of the effect.
            float maxVolume = AudioTrack.getMaxVolume();
            int result = audioTrack.setAuxEffectSendLevel(maxVolume);
            if ( result != AudioTrack.SUCCESS )
                Log.e(DEBUG_TAG, "ERROR: unable to set the effect volume");
        }

        private void setInputStream( int position ) {
            if ( audioInputStream != null ) {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream( this.file );
                audioInputStream = new DataInputStream( fileInputStream );
                long bytesToSkip = (long)(trackLength * (position / 100.0));
                if ( (int)bytesToSkip % 2 == 1 ) {
                    bytesToSkip--;
                }
                currentTrackPosition = bytesToSkip;
                audioInputStream.skip( bytesToSkip );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Play the audio track associated with this ShortSound.
         */
        public void play( int position ) {
            Log.d("AudioPlayer", "Playing track-" + audioTrack.getAudioSessionId());
            Log.d("AudioPlayer", "EQ effect is enabled? " + track.getmEqEffect().getEnabled());
            Log.d("AudioPlayer", "Reverb effect is enabled? " + track.getmReverbEffect().getEnabled());
            // If the track is Stopped then we need to reset the input stream so the AudioTrack starts
            // from the beginning again.
            if ( trackState == TrackState.STOPPED ) {
                setInputStream( position );
            }
            if ( audioThread != null ) {
                audioThread.interrupt();
            }
            trackState = TrackState.PLAYING;
            AudioTask task = new AudioTask();
            audioThread = new Thread(task);
            audioThread.start();
            Log.d(DEBUG_TAG, "Play track [" + track.getId() + "] from new thread.");
        }

        /**
         * Stop playing this track and reset its position to the beginning of the audio file.
         */
        public void stop() {
            if ( trackState == TrackState.PLAYING || trackState == TrackState.PAUSED ) {
                Log.d(DEBUG_TAG, "Stop track["+track.getId()+"].");
                audioTrack.flush();  // Clear the playback buffer and set Playback position to 0
                trackState = TrackState.STOPPED;
                try {
                    audioInputStream.close();
                    audioThread.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(DEBUG_TAG, "Tried to stop track["+track.getId()+"] but was already stopped");
            }
        }

        /**
         * Pause this track.
         */
        public void pause() {
            if ( trackState == TrackState.PLAYING ) {
                Log.d(DEBUG_TAG, "Pause track [" + track.getId() + "]");
                audioTrack.pause();
                trackState = TrackState.PAUSED;
//                track.getmEqEffect().disable();  // TODO remove after eq debugging
            } else {
                Log.e(DEBUG_TAG, "Tried to pause track ["+track.getId()+"] in invalid state ["+trackState+"]");
            }
        }

        /**
         * Get the current tracks position [0-100].
         * @return integer value representing the position as a percentage of the overall track length.
         */
        public int getCurrentTrackPosition() {
            return (int)Math.round(currentTrackPosition * 1.0 / trackLength * 100);
        }

        /**
         * Whether or not this ShortSoundTrack is currently playing.
         * @return
         */
        public boolean isPlaying() {
            return trackState == TrackState.PLAYING;
        }

        private class AudioTask implements Runnable {
            @Override
            public void run() {
                try {
                    Log.d(DEBUG_TAG, "Running thread to play ShortSoundTrack["+track.getId()+"]");
                    audioTrack.play();
                    int bytesRead;
                    while( trackState == TrackState.PLAYING && (bytesRead = audioInputStream.read( audioTrackBuffer ) ) != -1 ) {
                        // NOTE: this is blocking, so the next frame will not be loaded until ready.
                        // Look at AudioTrack docs for more info.
                        currentTrackPosition+= bytesRead;
                        notifyAudioPlayerOfPosition();
                        audioTrack.write( audioTrackBuffer, 0, bytesRead );
                    }
                    if ( trackState == TrackState.PLAYING ) {
                        // We reached the end of the track
                        Log.d(DEBUG_TAG, "Reached end of track["+track.getId()+"]");
                        stop();
                        // TODO notify the audio player that we have finished playback on this track.
                        notifyAudioPlayerEndOfTrack();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void notifyAudioPlayerEndOfTrack() {
            mAudioPlayerListener.notifyEndOfTrack(this);
        }

        private void notifyAudioPlayerOfPosition() {
            int currentPos = getCurrentTrackPosition();
            mAudioPlayerListener.notifyModelControlOfTrackPosition(this,currentPos);
        }
    }

    public interface PlaybackCompleteListener {
        public void playbackComplete();
    }

    public ShortSoundTrack getTrack(int pos) {
        return tracks.get( pos );
    }
}
