package com.sloths.speedy.shortsounds;

import java.io.File;
import java.util.HashMap;

/**
 * Created by neilharlow on 4/17/15.
 */
public class ShortSoundTrack {

    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();

    public static final String AUDIO_FORMAT = "";  // TODO: format/encoding?
    public static final int TRACK_LENGTH = 30;  // Track length in seconds
    public static final int BUFFER_SIZE = 2000;  // TODO: make buffer size with respect to TRACK_LENGTH
    public static final String DEFAULT_TITLE = "Untitled Track";

    private final String originalFile;
    private final String file;
    private long id;
    private String title;


    /**
     * Constructor for a ShortSoundTrack.
     * @param filename The filename of the recorded audio file.
     */
    public ShortSoundTrack( String filename, long shortSoundId ) {
        this.title = DEFAULT_TITLE;
        this.originalFile = filename;
        this.file = filename + "-ss";  // May need to change?
        this.id = this.sqlHelper.insertShortSoundTrack( this, shortSoundId );  // Save to the db
    }

    /**
     * Construct a ShortSoundTrack from data stored in the DB.
     * @param map
     */
    public ShortSoundTrack( HashMap<String, String> map ) {
        this.id = Long.parseLong( map.get( ShortSoundSQLHelper.KEY_ID ) );
        this.file = map.get( ShortSoundSQLHelper.KEY_TRACK_FILENAME_MODIFIED );
        this.originalFile = map.get( ShortSoundSQLHelper.KEY_TRACK_FILENAME_ORIGINAL );
        this.title = map.get( ShortSoundSQLHelper.KEY_TITLE );
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

    public void setId( long id ) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return this.title;
    }

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
