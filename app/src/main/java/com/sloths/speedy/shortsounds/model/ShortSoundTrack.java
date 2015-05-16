package com.sloths.speedy.shortsounds.model;

import android.content.Context;
import android.graphics.PointF;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.sloths.speedy.shortsounds.view.MainActivity;
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
    public static final String DEBUG_TAG = "SHORT_SOUNDS";
    // AudioTrack Params
    public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
    public static final int SAMPLE_RATE = 48000;  // NOTE: also used for buffer size
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MODE = AudioTrack.MODE_STREAM;
    public static int BUFFER_SIZE = 48000; // Default

    public static final String DEFAULT_TITLE = "Untitled Track";
    private static final String TAG = "Track";
    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();
    private static final Context context = ShortSoundsApplication.getAppContext();
    public static final String STORAGE_PATH = context.getFilesDir().getAbsolutePath();
    private final String fileName;
    private long id;
    private String title;
    private final long parentId;
    private boolean preparingWhilePlayed;
    private MediaState mState;
    private EqEffect mEqEffect;
    private ReverbEffect mReverbEffect;

    public enum EFFECT {
        EQ, REVERB, DISTORTION, BITCRUSH
    }

    /**
     * Create a ShortSoundTrack provided an existing audio file.
     * @param audioFile The recorded audio file.
     * @param shortSoundId The id of the ShortSound that this track belongs to.
     * @postcondition This ShortSoundTrack will be stored in the database and a
     *      copy of the file referenced by filename will be made.
     */
    public ShortSoundTrack( File audioFile, long shortSoundId ) {
        this.title = DEFAULT_TITLE;
        this.parentId = shortSoundId;
        setUpEffects();
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
        this.fileName = "ss" + shortSoundId + "-track" + id + "-modified";
        this.sqlHelper.updateShortSoundTrack( this );  // Had to update with filenames =(
        initFiles( audioFile );
    }

    /**
     * Construct a ShortSoundTrack from data stored in the DB.
     * @param map Key-value pairs corresponding to the fields that make up a ShortSoundTrack.
     * @throws AssertionError if an expected key is not found.
     */
    public ShortSoundTrack( HashMap<String, String> map ) {
        if ( !map.containsKey( sqlHelper.KEY_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_ID + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TRACK_FILENAME_MODIFIED ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TRACK_FILENAME_MODIFIED + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TITLE ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_TITLE + " field.");
        if ( !map.containsKey( sqlHelper.KEY_SHORT_SOUND_ID ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.KEY_SHORT_SOUND_ID + " field.");
        if ( !map.containsKey( sqlHelper.EQ_EFFECT_PARAMS ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.EQ_EFFECT_PARAMS + " field.");
        if ( !map.containsKey( sqlHelper.REVERB_EFFECT_PARAMS ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.REVERB_EFFECT_PARAMS + " field.");

        this.id = Long.parseLong(map.get(sqlHelper.KEY_ID));
        this.fileName = map.get( sqlHelper.KEY_TRACK_FILENAME_MODIFIED );
        this.title = map.get( sqlHelper.KEY_TITLE );
        this.parentId = Long.parseLong( map.get( sqlHelper.KEY_SHORT_SOUND_ID ) );

        String eqParams = map.get( sqlHelper.EQ_EFFECT_PARAMS );
        String reverbParams = map.get( sqlHelper.REVERB_EFFECT_PARAMS);

        loadEffectsFromDB(eqParams, reverbParams);
    }

    private void setUpEffects() {
        this.mEqEffect = new EqEffect();
        this.mReverbEffect = new ReverbEffect();
    }

    /**
     * Loads teh effects given their held String state parameters in the database
     * @param eqParams
     * @param reverbParams
     */
    private void loadEffectsFromDB(String eqParams, String reverbParams) {
        Log.d("ShortSoundTrack", "eq effect params received: "+ eqParams);
        Log.d("ShortSoundTrack", "reverb effect params received: "+ reverbParams);

        if (eqParams == null || eqParams.equals("NULL")) {
            this.mEqEffect = new EqEffect();
        } else {
            this.mEqEffect = new EqEffect(eqParams);
        }

        // Reverb
        if (reverbParams == null || reverbParams.equals("NULL")) {
            this.mReverbEffect = new ReverbEffect();
        } else {
            this.mReverbEffect = new ReverbEffect(reverbParams);
        }
    }

    public void addEffect(EFFECT e) {
        Log.d("effects", "turnOnEffect called");
        switch (e) {
           case EQ:
               Log.d("effects", "EQ toggle switch clicked");
               this.mEqEffect.enable();
               break;
           case REVERB:
               Log.d("effects", "REVERB toggle switch clicked");
               this.mReverbEffect.enable();
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

    /***
     *
     * @param effect
     * @return
     */
    public PointF[] getEffectVals(String effect) {
        if (effect.equals(MainActivity.EQ)) {
            PointF[] points = mEqEffect.getPointVals();
            return points;
        } else {
            // Reverb point being returned
            PointF[] points = mReverbEffect.getPointVal();
            return points;
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
     * Initialize the files for a new ShortSoundTrack.
     * @param audioFile
     */
    private void initFiles( File audioFile ) {
        File file = new File( STORAGE_PATH , this.fileName);
        try {
            copyFile( audioFile, file );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove this ShortSoundTrack's files from memory.
     */
    private void deleteFiles() {
        File file = new File( this.fileName);
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
    public String getFileName() { return this.fileName; }

    /**
     * Get this tracks id.
     * @return id
     */
    public long getId() { return this.id; }

    /**
     * This is the representation invarient of the ShortSoundTrack model.
     * The main thing here is that a ShortSoundTrack becomes invalid if the files
     * associated with the tracks are non-existent.
     */
    private void repInvariant() {
        if ( this.title == null || !(this.title instanceof String) ) throw new AssertionError("Invalid title");
        if ( this.fileName == null || !(this.fileName instanceof String) ) throw new AssertionError("Invalid filename");
        if ( this.id < 1 ) throw new AssertionError("Invalid id: " + this.id);
        // Check that the files are on disk
        File file = new File( this.fileName);
        if ( !file.exists() ) throw new AssertionError("File does not exist: " + file);
    }

    public String getEQEffectString() {
        return mEqEffect.encodeParameters();
    }

    public String getReverbEffectString() {
        return mReverbEffect.encodeParameters();
    }

    public EqEffect getmEqEffect() {
        return mEqEffect;
    }

    public ReverbEffect getmReverbEffect() {
        return mReverbEffect;
    }
}
