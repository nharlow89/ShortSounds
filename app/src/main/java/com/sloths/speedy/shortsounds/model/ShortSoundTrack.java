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
    public static final int SAMPLE_RATE = 44100;  // NOTE: also used for buffer size
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MODE = AudioTrack.MODE_STREAM;
    private static final String TAG = "ShortSoundTrack";
    public static int BUFFER_SIZE = 44100; // Default
    public static float DEFAULT_VOLUME = 0.8f;

    public static final String DEFAULT_TITLE = "Untitled Track";
    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();
    private static final Context context = ShortSoundsApplication.getAppContext();
    public static final String STORAGE_PATH = context.getFilesDir().getAbsolutePath();
    private final String fileName;
    private long id;
    private String title;
    private final long parentId;
    private EqEffect mEqEffect;
    private ReverbEffect mReverbEffect;
    private float volume;
    private boolean isSolo;
    private long mTrackLength;

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
        this.mEqEffect = new EqEffect();
        this.mReverbEffect = new ReverbEffect();
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
        this.fileName = "ss" + shortSoundId + "-track" + id + "-modified";
        this.volume = 0.8f;
        this.isSolo = false;
          // Had to update with filename =(
        initFiles( audioFile );
        this.sqlHelper.updateShortSoundTrack( this );
        repInvariant();
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
        if ( !map.containsKey( sqlHelper.TRACK_LENGTH) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.TRACK_LENGTH + " field.");
        if ( !map.containsKey( sqlHelper.VOLUME_PARAMS ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.VOLUME_PARAMS + " field.");
        if ( !map.containsKey( sqlHelper.SOLO_PARAMS ) ) throw new AssertionError("Error decoding ShortSoundTrack, missing " + sqlHelper.SOLO_PARAMS + " field.");

        this.id = Long.parseLong(map.get(sqlHelper.KEY_ID));
        this.fileName = map.get(sqlHelper.KEY_TRACK_FILENAME_MODIFIED);
        this.title = map.get(sqlHelper.KEY_TITLE);
        this.parentId = Long.parseLong(map.get(sqlHelper.KEY_SHORT_SOUND_ID));
        this.mEqEffect = new EqEffect( map.get( sqlHelper.EQ_EFFECT_PARAMS ) );
        this.mReverbEffect = new ReverbEffect( map.get( sqlHelper.REVERB_EFFECT_PARAMS ) );
        this.volume = Float.parseFloat(map.get(sqlHelper.VOLUME_PARAMS));
        this.mTrackLength = Long.parseLong(map.get(sqlHelper.TRACK_LENGTH));
        if  (this.mTrackLength == 0) throw new AssertionError("Length can't be 0");
        this.isSolo = map.get(sqlHelper.SOLO_PARAMS).equals("t");
        repInvariant();
    }

    public void addEffect(Effect.Type e) {
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
        repInvariant();
    }

    /***
     *
     * @param effect
     * @return
     */
    public PointF[] getEffectVals(String effect) {
        if (effect.equals(MainActivity.EQ)) {
            PointF[] points = mEqEffect.getPointVals();
            if (points != null) {
                Log.d("Track", "Track EQ effect values pulled: " + points[0].x +", "+points[0].y+") ("+points[1].x+", "+points[1].y+")");
            }
            return points;
        } else {
            // Reverb point being returned
            PointF[] points = mReverbEffect.getPointVal();
            return points;
        }
    }

    public void removeEffect(Effect.Type e) {
        switch (e) {
            case EQ:
                this.mEqEffect.disable();
                break;
            case REVERB:
                this.mReverbEffect.disable();
                break;
            default:
                throw new UnsupportedOperationException("bitcrush and distortion have not been implemented yet");
        }
        repInvariant();
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
        mTrackLength = audioFile.length();
        repInvariant();
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
     * Returns the length of this track in Bytes
     * @return long the length in bytes
     */
    public long getLengthInBytes() { return this.mTrackLength; }

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
        if ( this.mEqEffect == null || !(this.mEqEffect instanceof EqEffect) ) throw new AssertionError("Missing EqEffect");
        if ( this.mReverbEffect == null || !(this.mReverbEffect instanceof ReverbEffect) ) throw new AssertionError("Missing ReverbEffect");
        if ( this.id < 1 ) throw new AssertionError("Invalid id: " + this.id);

        // Check that the files are on disk
//        File file = new File( this.fileName);
//        if ( !file.exists() ) throw new AssertionError("File does not exist: " + file);
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

    public ReverbEffect getmReverbEffect() { return mReverbEffect; }

    public float getVolume() { return volume; }
    public void setTrackVolume(float volume) {
        if (volume >= 0.0f && volume <= 1.0f)
            this.volume = volume;
    }

    public void saveShortSoundTrack() {
        Log.d(TAG, "Saving ShortSoundTrack to DB");
        sqlHelper.updateShortSoundTrack(this);
    }
    public String getSQLSolo() { if (isSolo) return "t"; return "f";}
    public boolean isSolo() { return isSolo; }
    public void toggleSolo() { isSolo = !isSolo; }

    public void setEffectToggle(Effect.Type effect, boolean enable) {
        if (effect.equals(Effect.Type.EQ)) {
            // EQ
            if (enable) {
                mEqEffect.enable();
            } else {
                mEqEffect.disable();
            }
        } else {
            // REVERB
            if (enable) {
                mReverbEffect.enable();
            } else {
                mReverbEffect.disable();
            }
        }
        sqlHelper.updateShortSoundTrack( this );
    }

    public boolean isEffectChecked(Effect.Type effect) {
        if (effect.equals(Effect.Type.EQ)) {
            return mEqEffect.getEnabled();
        } else if (effect.equals(Effect.Type.REVERB)) {
            //REVERB
            return mReverbEffect.getEnabled();
        } else {
            // All other effects
            return false;
        }
    }

    public void releaseEffects() {
        Log.d(DEBUG_TAG, "Release effects associated with track["+this.id+"]");
        mReverbEffect.release();
        mEqEffect.release();
    }
}
