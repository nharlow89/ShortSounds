package com.sloths.speedy.shortsounds.model;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
        this.fileName = "ss" + shortSoundId + "-track" + id + "-modified";
        this.sqlHelper.updateShortSoundTrack(this);  // Had to update with filenames =(
        initFiles(audioFile);
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
        this.id = Long.parseLong(map.get(sqlHelper.KEY_ID));
        this.fileName = map.get( sqlHelper.KEY_TRACK_FILENAME_MODIFIED );
        this.title = map.get( sqlHelper.KEY_TITLE );
        this.parentId = Long.parseLong( map.get( sqlHelper.KEY_SHORT_SOUND_ID ) );
    }

    public void addEffect() {
        // TODO
    }

    public void removeEffect() {
        // TODO
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
}
