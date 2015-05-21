package com.sloths.speedy.shortsounds.model;

import android.media.AudioTrack;
import android.util.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The AudioPlayer class handles all the playing of ShortSounds/ShortSoundTracks.
 * This player is capable of playing multiple tracks or a single track.
 */
public class AudioPlayer {
    public static final String DEBUG_TAG = "SHORT_SOUNDS";

    public static enum PlayerState { PLAYING_ALL, STOPPED_ALL, PAUSED_ALL };
    private static enum TrackState { PLAYING, PAUSED, STOPPED };

    private PlayerState playerState;
    private Map<ShortSoundTrack, TrackPlayer> trackPlayers;
    private ShortSound mCurrentShortSound;

    public AudioPlayer( ShortSound ss ) {
        trackPlayers = new HashMap<>();
        mCurrentShortSound = ss;
        for ( ShortSoundTrack track : ss.getTracks() ) {
            trackPlayers.put(track, new TrackPlayer(track));
        }
        playerState = PlayerState.STOPPED_ALL;
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
        if (longestBytePosition % 2 == 1) longestBytePosition--;



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
     * Play a single track within this AudioPlayer.
     * @param track
     */
    public void playTrack( ShortSoundTrack track, int position ) {
        pauseAll();
        TrackPlayer targetPlayer = trackPlayers.get( track );
        targetPlayer.play( position );
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
        trackPlayers.put(track, new TrackPlayer(track));
    }

    /**
     * Removes ShortSoundTrack from this AudioPlayer
     * @param track the ShortSoundTrack to be removed
     */
    public void removeTrack( ShortSoundTrack track ) {
        trackPlayers.remove(track);
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

        public TrackPlayer( ShortSoundTrack track ) {
            this.track = track;
            setupTrack();
        }

        /**
         * Set up an individual ShortSoundTrack. This includes creating the AudioTrack for playback
         * and creating a reference to the actual audio file.
         */
        public void setupTrack() {
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
            // Let the effects know about the audio playback object
//            track.getmEqEffect().setAudioSource( audioTrack.getAudioSessionId() );
            track.getmReverbEffect().setAudioSource( audioTrack.getAudioSessionId() );
            audioTrack.setAuxEffectSendLevel(1.0f);
            // Attach the audio effects (NOT required if passing audioTrack id to the effect constructor).
//            int eqId = track.getmEqEffect().getEffectId();
//            audioTrack.attachAuxEffect( eqId );
//            int reverbId = track.getmReverbEffect().getEffectId();
//            audioTrack.attachAuxEffect( reverbId );
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
                        //Log.d(DEBUG_TAG, "Writing ["+bytesRead+"] bytes to audioTrack ["+track.getId()+"]. PlaybackPosition["+ getCurrentTrackPosition() +"%]");
                        audioTrack.write( audioTrackBuffer, 0, bytesRead );
                    }
                    if ( trackState == TrackState.PLAYING ) {
                        // We reached the end of the track
                        Log.d(DEBUG_TAG, "Reached end of track["+track.getId()+"]");
                        stop();
                        // TODO notify the audio player that we have finished playback on this track.
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface PlaybackCompleteListener {
        public void playbackComplete();
    }
}
