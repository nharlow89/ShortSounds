package com.sloths.speedy.shortsounds.model;

import java.io.File;
import java.util.HashMap;

/**
 * This class represents the model of a ShortSoundTrack. A ShortSoundTrack essentially
 * keeps track of an audio file along with any effects that may have been applied to that
 * file. A ShortSoundTrack should belong to a single ShortSound at any given time.
 */
public class ShortSoundTrack {

    public static final String AUDIO_FORMAT = "";  // TODO: format/encoding?
    public static final int TRACK_LENGTH = 30;  // Track length in seconds
    public static final int BUFFER_SIZE = 2000;  // TODO: make buffer size with respect to TRACK_LENGTH
    public static final String DEFAULT_TITLE = "Untitled Track";

    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();
    private final String originalFile;
    private final String file;
    private long id;
    private String title;
    private final long parentId;

    /**
     * Create a ShortSoundTrack provided an existing audio file.
     * @param filename The filename of the recorded audio file.
     * @param shortSoundId The id of the ShortSound that this track belongs to.
     * @postcondition This ShortSoundTrack will be stored in the database and a
     *      copy of the file referenced by filename will be made.
     */
    public ShortSoundTrack( String filename, long shortSoundId ) {
        this.title = DEFAULT_TITLE;
        this.originalFile = filename;
        this.file = filename + "-ss";  // May need to change?
        this.parentId = shortSoundId;
        // TODO: create a copy of the original file that will be our "working" copy
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
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
        this.file = map.get( sqlHelper.KEY_TRACK_FILENAME_MODIFIED );
        this.originalFile = map.get( sqlHelper.KEY_TRACK_FILENAME_ORIGINAL );
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
}
