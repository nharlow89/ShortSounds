package com.sloths.speedy.shortsounds.model;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.sloths.speedy.shortsounds.view.ShortSoundsApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * This class represents the model of a ShortSoundTrack. A ShortSoundTrack essentially
 * keeps track of an audio file along with any effects that may have been applied to that
 * file. A ShortSoundTrack should belong to a single ShortSound at any given time.
 */
public class ShortSoundTrack {
    /**
     * Please note that the internal state of a ShortSoundTrack attempts to follow the state
     * machine found here in the MediaPlayer class: http://developer.android.com/reference/android/media/MediaPlayer.html
     */

    public static final String AUDIO_FORMAT = "";  // TODO: format/encoding?
    public static final int TRACK_LENGTH = 30;  // Track length in seconds
    public static final int BUFFER_SIZE = 2000;  // TODO: make buffer size with respect to TRACK_LENGTH
    public static final String DEFAULT_TITLE = "Untitled Track";

    private static final String TAG = "Track";
    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();
    private final String originalFile;
    private final String file;
    private long id;
    private String title;
    private final long parentId;
    private MediaPlayer player;
    private MediaState mState;
    private EqEffect mEqEffect;
    private ReverbEffect mReverbEffect;
    public enum EFFECT {
        EQ, REVERB, DISTORTION, BITCRUSH
    }

    /**
     * Create a ShortSoundTrack provided an existing audio file.
     * @param shortSoundId The id of the ShortSound that this track belongs to.
     * @postcondition This ShortSoundTrack will be stored in the database and a
     *      copy of the file referenced by filename will be made.
     */
    public ShortSoundTrack( File audioFile, long shortSoundId ) {
        this.title = DEFAULT_TITLE;
        this.parentId = shortSoundId;
        // TODO: create a copy of the original file that will be our "working" copy
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
        this.originalFile = "ss" + shortSoundId + "-track" + id;
        this.file = originalFile + "-modified";
        this.sqlHelper.updateShortSoundTrack( this );  // Had to update with filenames =(
        initFiles( audioFile );
        setUpMediaPlayer();
        setUpEffects();
    }

    /**
     * Construct a ShortSoundTrack from data stored in the DB.
     * @param map Key-value pairs corresponding to the fields that make up a ShortSoundTrack.
     * @throws AssertionError if an expected key is not found.
     */
    public ShortSoundTrack( HashMap<String, String> map ) {
        if ( !map.containsKey( sqlHelper.KEY_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_ID + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TRACK_FILENAME_ORIGINAL ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TRACK_FILENAME_ORIGINAL + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TRACK_FILENAME_MODIFIED ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TRACK_FILENAME_MODIFIED + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TITLE ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TITLE + " field.");
        if ( !map.containsKey( sqlHelper.KEY_SHORT_SOUND_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_SHORT_SOUND_ID + " field.");
        this.id = Long.parseLong( map.get( sqlHelper.KEY_ID ) );
        this.file = map.get(sqlHelper.KEY_TRACK_FILENAME_MODIFIED);
        this.originalFile = map.get(sqlHelper.KEY_TRACK_FILENAME_ORIGINAL);
        this.title = map.get( sqlHelper.KEY_TITLE );
        this.parentId = Long.parseLong( map.get( sqlHelper.KEY_SHORT_SOUND_ID ) );
        this.player = new MediaPlayer();
        setUpMediaPlayer();
        setUpEffects();
    }

    private void setUpMediaPlayer() {
        this.player = new MediaPlayer();

        Context context = ShortSoundsApplication.getAppContext();
        String path = context.getFilesDir().getAbsolutePath();
        try {
            Log.d("DEBUG", "setDataSource(" + path + "/" + this.file + ")");
            this.player.setDataSource( path + "/" + this.file );
            mState = MediaState.INITIALIZED;
            this.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mState = MediaState.PREPARED;
                    Log.d(TAG, "prepared track [" + id + "]");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setUpEffects() {
        this.mEqEffect = new EqEffect(player);
        this.mReverbEffect = new ReverbEffect(player);
    }

    /**
     * Play the audio track associated with this ShortSound.
     */
    public void play() {
        if ( mState == MediaState.PREPARED || mState == MediaState.PAUSED ) {
            Log.d(TAG, "play track ["+this.getId()+"]");
            player.start();
            mState = MediaState.STARTED;
        } else if ( mState == MediaState.STOPPED ) {
            try {
                Log.d(TAG, "play stopped track ["+this.getId()+"]");
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop playing this track and reset its position to the beginning of the audio file.
     */
    public void stop() {
        if ( mState == MediaState.STARTED || mState == MediaState.PAUSED ) {
            Log.d(TAG, "stop track ["+this.getId()+"]");
            player.stop();
            player.prepareAsync();
            mState = MediaState.PREPARING;
        }
    }

    public void pause() {
        if ( mState == MediaState.STARTED || player.isPlaying() ) {
            Log.d(TAG, "pause track [" + this.getId() + "]");
            player.pause();
            mState = MediaState.PAUSED;
        }
    }

    /**
     * Prepare this track for playing. Note: must be called after stopping the track or after init
     * of the MediaPlayer.
     */
    public void prepare() {
        if ( mState == MediaState.STOPPED || mState == MediaState.INITIALIZED ) {
            try {
                Log.d(TAG, "prepare track ["+this.getId()+"]");
                player.prepare();
                mState = MediaState.PREPARED;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Release this track from the MediaPlayer when no longer in use.
     */
    public void release() {
        if (player != null) {
            player.release();
        }
        if (mEqEffect != null) {
            mEqEffect.release();
        }
        if (mReverbEffect != null) {
            mReverbEffect.release();
        }
    }

    /**
     * Return whether the current track is playing or not.
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    /**
     * Prepare this track asynchronously.
     */
    public void prepareAsync() {
        if ( player == null )
            setUpMediaPlayer();
        if ( mState == MediaState.INITIALIZED || mState == MediaState.STOPPED ) {
            Log.d(TAG, "prepareAsync track ["+this.getId()+"]");
            player.prepareAsync();
            mState = MediaState.PREPARING;
        }
    }

    /**
     * Set the onCompletionListener for this ShortSoundTrack.
     * @param listener
     */
    public void setOnPlayCompleteListener( MediaPlayer.OnCompletionListener listener ) {
        player.setOnCompletionListener( listener );
    }

    public void addEffect(EFFECT e) {
        Log.d("effects", "addEffect called");
        switch (e) {
           case EQ:
               Log.d("effects", "EQ toggle switch clicked");
               this.mEqEffect.enable();
               //this.player.attachAuxEffect(mEqEffect.getEffectId());
               break;
           case REVERB:
               Log.d("effects", "REVERB toggle switch clicked");
               this.mReverbEffect.enable();
               //this.player.attachAuxEffect(mReverbEffect.getEffectId());
               break;
           case DISTORTION:
               Log.d("effects", "DISTORTION toggle switch clicked");
               //throw new UnsupportedOperationException("bitcrush and distortion have not been implemented yet");
               break;
           case BITCRUSH:
               Log.d("effects", "BITCRUSH toggle switch clicked");
               //throw new UnsupportedOperationException("bitcrush and distortion have not been implemented yet");
               break;
        }
    }

    public void removeEffect(EFFECT e) {
        switch (e) {
            case EQ:
                this.mEqEffect.disable();
            case REVERB:
                this.mReverbEffect.disable();
            default:
                throw new UnsupportedOperationException("bitcrush and distortion have not been implemented yet");
        }
    }

    /**
     * Remove the ShortSoundTrack from the database and delete the corresponding
     * files.
     * @postcondition This track will no longer exist in the database, and its audio files
     *      will be removed.
     */
    public void delete() {
        sqlHelper.removeShortSoundTrack( this );
        deleteFiles();
    }

    /**
     * Initialize the files for a new ShortSoundTrack. This makes a copy of the original audio file
     * into the proper location.
     * @param audioFile
     */
    private void initFiles( File audioFile ) {
        Context context = ShortSoundsApplication.getAppContext();
        String path = context.getFilesDir().getAbsolutePath();
        File originalFile = new File( path, this.originalFile );
        File file = new File( path , this.file );
        try {
            copyFile( audioFile, originalFile );
            copyFile( audioFile, file );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove this ShortSoundTrack's files from memory (both the original and
     * any modified)
     */
    private void deleteFiles() {
        File originalFile = new File( this.originalFile );
        if( originalFile.exists() ) {
            originalFile.delete();
        }
        File file = new File( this.file );
        if( file.exists() ) {
            file.delete();
        }
    }

    private void copyFile(File src, File dst) throws IOException {
        Log.d("DEBUG", "Copy file [" + src.getPath() + "] to ["+ dst.getPath() +"]");
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Get the title of this ShortSoundTrack.
     * @return String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the title of this ShortSoundTrack
     * @return String
     */
    @Override
    public String toString() {
        return this.title;
    }

    /**
     * Get the parentId (ShortSound id) that this track is associated with.
     * @return long
     */
    public long getParentId() {
        return this.parentId;
    }

    /**
     * Get the filename associated with this track.
     * @return filename
     */
    public String getFile() { return this.file; }

    /**
     * Get this tracks id.
     * @return
     */
    public long getId() { return this.id; }

    /**
     * This is the representation invarient of the ShortSoundTrack model.
     * The main thing here is that a ShortSoundTrack becomes invalid if the files
     * associated with the tracks are non-existent.
     */
    private void repInvariant() {
        if ( this.title == null || !(this.title instanceof String) ) throw new AssertionError("Invalid title");
        if ( this.file == null || !(this.file instanceof String) ) throw new AssertionError("Invalid filename");
        if ( this.originalFile == null || !(this.originalFile instanceof String) ) throw new AssertionError("Invalid filename");
        if ( this.id < 1 ) throw new AssertionError("Invalid id: " + this.id);
        // Check that the files are on disk
        File originalFile = new File( this.originalFile );
        if ( !originalFile.exists() ) throw new AssertionError("File does not exist: " + originalFile);
        File file = new File( this.file );
        if ( !file.exists() ) throw new AssertionError("File does not exist: " + file);
    }

    public String getOriginalFile() {
        return originalFile;
    }
}
