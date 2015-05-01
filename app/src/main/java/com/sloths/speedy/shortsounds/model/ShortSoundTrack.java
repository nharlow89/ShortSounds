package com.sloths.speedy.shortsounds.model;

import java.io.File;
import java.util.HashMap;

/**
 * Created by neilharlow on 4/17/15.
 */
public class ShortSoundTrack {

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
    public ShortSoundTrack( String filename ) {
        this.title = DEFAULT_TITLE;
        this.originalFile = filename;
        this.file = filename + "-ss";  // May need to change?
    }

    /**
     * Construct a ShortSoundTrack from data stored in the DB.
     * @param map A map from DB column names to their respective values for this ShortSound
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
     * Remove this ShortSoundTrack's files from memory (both the original and
     * any modified)
     */
    public void deleteFiles() {
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
}


/*
    Record a sound.
    Create ShortSoundTrack.
    Add ShortSoundTrack to the ShortSound.
    Save ShortSoundTrack to disk
    Update DB to associate the ShortSoundTrack with the ShortSound
 */