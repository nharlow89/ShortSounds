package com.sloths.speedy.shortsounds;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ShortSound {

    /**
     * Local vars
     */
    private static final String DEFAULT_TITLE = "Untitled";
    private List<ShortSoundTrack> tracks;
    private String title;
    private int id;
    private static SQLiteOpenHelper sqlHelper =
            new ShortSoundSQLHelper( ShortSoundsApplication.getAppContext() );

    /**
     * Constructor: Create a new empty ShortSound
     */
    public ShortSound() {
        title = DEFAULT_TITLE;  // Default
        tracks = new ArrayList<ShortSoundTrack>();  // Initially no tracks
    }

    /**
     * Fetch all available ShortSounds (used on app start)
     */
    public static List<ShortSound> getAll() {
        return null;
    }

    /**
     * Generate audio file (with all compiled tracks)
     */
    public void generateAudioFile() {

    }

    /**
     * Get the tracks for this ShortSound
     */
    public List<ShortSoundTrack> getTracks() {
        return null;
    }

    /**
     * Add a track to the ShortSound
     */
    public void addTrack( ShortSoundTrack track ) {
        // Store the file to disk
        // Add a new Track entry in DB for this ShortSound
    }

    /**
     * Remove a track from this ShortSound
     */
    public void removeTrack() {

    }

    /**
     * Getter for title
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for title
     * @param new_title Updated title
     */
    public void setTitle( String new_title ) {
        this.title = new_title;
    }
}
