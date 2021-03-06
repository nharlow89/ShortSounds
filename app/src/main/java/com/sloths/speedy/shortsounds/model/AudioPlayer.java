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
    public static final String TAG = "AudioPlayer";
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

    /**
     * Create a new Audio Player
     * @param ss The ShortSound to create the audio player for
     */
    public AudioPlayer( ShortSound ss ) {
        Log.d(TAG, "Creating new AudioPlayer");
        trackPlayers = new HashMap<>();
        tracks = new ArrayList<>();
        mModelControl = ModelControl.instance();
        mCurrentShortSound = ss;
        mLongestTrack = ss.getLongestTrack();
        for ( ShortSoundTrack track : ss.getTracks() ) {
            TrackPlayer tp = new TrackPlayer(track, this);
            trackPlayers.put(track, tp);
            tracks.add(track);
            if (track == mLongestTrack) {
                mLongestTrackPlayer = tp;
            }
        }
        playerState = PlayerState.STOPPED_ALL;
    }

    /**
     * Teardown and cleanup any resources that were in use by this AudioPlayer.
     */
    public void destroy() {
        Log.d(TAG, "Destroying AudioPlayer.");
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
        Log.d(TAG, "Play all tracks starting at ["+position+"%]");
        if (mCurrentShortSound.getLongestTrack() == null) {
            return;
        }
        long longestTrackMaxByteOffset = mCurrentShortSound.getLongestTrack().getLengthInBytes();
        long longestBytePosition = (longestTrackMaxByteOffset * position) / 100 ;
        //if (longestBytePosition % 2 == 1) longestBytePosition--;
        Log.d(TAG, "longestTrackMaxByteOffset ["+longestTrackMaxByteOffset+"]");
        Log.d(TAG, "longestBytePosition ["+longestBytePosition+"]");

        for ( Map.Entry<ShortSoundTrack, TrackPlayer> entry: trackPlayers.entrySet() ) {
            entry.getValue().stop();

            if (position == 0) {
                Log.d(TAG,"play position 0");
                entry.getValue().play(position);
            } else {
                ShortSoundTrack currentTrack = entry.getKey();
                long currentTrackMaxByteOffset = currentTrack.getLengthInBytes();
                boolean isPlayable = currentTrackMaxByteOffset >= longestBytePosition;
                if ( isPlayable ) {
                    // play with a normalized position
                    Log.d(TAG, "DIV " + longestBytePosition + "/" + currentTrackMaxByteOffset);
                    double normalizedPosition = ((double)longestBytePosition / (double)currentTrackMaxByteOffset) * 100.0 ;
                    Log.d(TAG, "normalized position ["+(int)Math.round(normalizedPosition)+"]");
                    entry.getValue().play((int)Math.round(normalizedPosition));
                }
            }
        }
        playerState = PlayerState.PLAYING_ALL;
    }


    /**
     * Notifies the model control of the track position
     * @param notifier the TrackPlayer that will notify  the model
     * @param position the position to notify the model of
     */
    public void notifyModelControlOfTrackPosition(TrackPlayer notifier, int position) {
        if (notifier == mLongestTrackPlayer) {
            Log.d("DEBUG", "mLongestTrack is " + mLongestTrackPlayer);
            Log.d("DEBUG", "notifier is " + notifier);
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
     * Determines whether or not all the tracks are being played
     * @return Boolean value for whether All tracks are being played.
     */
    public boolean isPlayingAll() {
        return playerState == PlayerState.PLAYING_ALL;
    }

    /**
     * Determines whether solo is enabled
     * @param track to specify which track in the ShortSound
     * @return true is solo enabled false otherwise
     */
    public boolean isTrackSolo(int track) {
        return tracks.get(track).isSolo();
    }


    /**
     * Solo's the track at the given position.  Solo by definition sets all track volumes to
     * zero for which solo is not enabled.
     * @param track to specifiy which track in the ShortSound
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void soloTrack(int track) {
        tracks.get(track).toggleSolo();
        boolean atLeastOneSoloTrack = false;
        for (int i = 0; i < tracks.size(); i++)
            atLeastOneSoloTrack = atLeastOneSoloTrack || tracks.get(i).isSolo();
        for (int i = 0; i < tracks.size(); i++) {
            ShortSoundTrack sst = tracks.get(i);
            TrackPlayer tp = trackPlayers.get(sst);
            if (!sst.isSolo() && atLeastOneSoloTrack)
                tp.audioTrack.setVolume(0.0f);
            else
                tp.audioTrack.setVolume(sst.getVolume());
        }
    }

    /**
     * changes the volume of a short sound on the given track.
     * @param track To specify which track in the ShortSound
     * @param volume desired volume level
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
     * Add an additional ShortSoundTrack to this AudioPlayer.
     * @param track The track to add
     */
    public void addTrack( ShortSoundTrack track ) {
        tracks.add(track);
        TrackPlayer tp = new TrackPlayer(track, this);
        if (mLongestTrackPlayer == null) {
            mLongestTrackPlayer = tp;
        }
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
        tracks.remove(track);

        boolean removedTrackWasLongestTrack = mLongestTrack.getLengthInBytes() == track.getLengthInBytes();
        if(removedTrackWasLongestTrack) {
            updateLongestTrack();
            Log.d("DEBUG", "mLongestTrack SET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            for(ShortSoundTrack sst : trackPlayers.keySet()) {
                Log.d("DEBUG", "sst.getLengthInBytes =" + sst.getLengthInBytes() );
                Log.d("DEBUG", "mLongestTrack.getLengthInBytes() =" + mLongestTrack.getLengthInBytes());
                if(sst.getLengthInBytes() == mLongestTrack.getLengthInBytes()) {
                    Log.d("DEBUG", "mLongestTrackPlayer SET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    mLongestTrackPlayer = trackPlayers.get(sst);
                }
            }
        }
    }

    /**
     * Updates mLongestTrack to be accurate. Helper Method for removeTrack.
     */
    private void updateLongestTrack() {
        if (tracks.size() == 0) {
            mLongestTrack = null;
            mLongestTrackPlayer = null;
            return;
        } else {
            mLongestTrack = tracks.get(0);
            for ( ShortSoundTrack sst : tracks ) {
                if (sst.getLengthInBytes() >= mLongestTrack.getLengthInBytes()) {
                    mLongestTrack = sst;
                }
            }
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

        /**
         * Creates a track player
         * @param track The track to create a track player on
         * @param parent The AudioPlayer that plays the ShortSound the track
         *               is associated with
         */
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
            audioTrack.setPositionNotificationPeriod(ShortSoundTrack.SAMPLE_RATE);
            audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    Log.d(TAG, "PlaybackListener");
                    // TODO: update seekbar

                }
            });
            audioTrackBuffer = new byte[ShortSoundTrack.BUFFER_SIZE * 2];
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
                audioTrack.setVolume(track.isSolo() ? 0.0f : track.getVolume());
            trackState = TrackState.STOPPED;
            this.file = new File( ShortSoundTrack.STORAGE_PATH, track.getFileName() );
            this.trackLength = file.length();
            attachEffects();
        }

        /**
         * Cleanup any resources tied to this TrackPlayer.
         */
        public void destroy() {
            Log.d(TAG, "Destroy TrackPlayer associated with Track["+track.getId()+"]");
            // Cleanup any effect objects
            track.releaseEffects();
            // Take care of the input stream.
            if ( audioInputStream != null ) {
                try {
                    audioInputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "Failed closing an existing AudioInputStream.");
                    e.printStackTrace();
                }
            }
            // Take care of the thread
            if ( audioThread != null )
                audioThread.interrupt();
            // Take care of the AudioTrack
            if ( audioTrack != null )
                audioTrack.release();
        }

        /**
         * Private helper to attach all the effects associated with this track.
         */
        private void attachEffects () {
            // Reverb
            ReverbEffect reverb = track.getmReverbEffect();
            reverb.setupReverbEffect( audioTrack.getAudioSessionId() );

            // Equalizer
            EqEffect eq = track.getmEqEffect();
            eq.setupEqEffect( audioTrack.getAudioSessionId() );

            // Important: set the volume of the effect.
            float maxVolume = AudioTrack.getMaxVolume();
            int result = audioTrack.setAuxEffectSendLevel(maxVolume);
            if ( result != AudioTrack.SUCCESS )
                Log.e(TAG, "ERROR: unable to set the effect volume");
        }

        /**
         * Sets the input stream
         * @param position The position currently seeked to in the track
         */
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
         * @param position The posision currently seeked to in the track
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
            Log.d(TAG, "Play track [" + track.getId() + "] from new thread.");
        }

        /**
         * Stop playing this track and reset its position to the beginning of the audio file.
         */
        public void stop() {
            if ( trackState == TrackState.PLAYING || trackState == TrackState.PAUSED ) {
                Log.d(TAG, "Stop track["+track.getId()+"].");
                audioTrack.flush();  // Clear the playback buffer and set Playback position to 0
                trackState = TrackState.STOPPED;
                try {
                    audioInputStream.close();
                    audioThread.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.w(TAG, "Tried to stop track[" + track.getId() + "] but was already stopped");
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
         * @return true if playing, false otherwise
         */
        public boolean isPlaying() {
            return trackState == TrackState.PLAYING;
        }

        /**
         * This class represents an audio task
         */
        private class AudioTask implements Runnable {

            /**
             * Runs a thread to play an audio track
             */
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Running thread to play ShortSoundTrack["+track.getId()+"]");
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
                        Log.d(TAG, "Reached end of track["+track.getId()+"]");
                        stop();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Notifies the audio player of the current position in the track
         */
        private void notifyAudioPlayerOfPosition() {
            int currentPos = getCurrentTrackPosition();
            mAudioPlayerListener.notifyModelControlOfTrackPosition(this,currentPos);
        }
    }

    /**
     * This interface is a listener for whether or not the playback is complete
     */
    public interface PlaybackCompleteListener {
        /**
         * Stops playback
         */
        void playbackComplete();
    }

    /**
     * Gets a track
     * @param pos The position of the track in the ShortSound
     * @return The track associated with the position
     */
    public ShortSoundTrack getTrack(int pos) {
        return tracks.get( pos );
    }
}
