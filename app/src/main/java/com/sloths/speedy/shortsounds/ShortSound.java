package com.sloths.speedy.shortsounds;

import android.util.Log;

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
    private long id;
    private static ShortSoundSQLHelper sqlHelper =
            new ShortSoundSQLHelper( ShortSoundsApplication.getAppContext() );

    /**
     * Constructor: Create a new empty ShortSound
     */
    public ShortSound() {
        Log.d("DB_TEST", "ShortSound:constructor()");
        title = DEFAULT_TITLE;  // Default
        tracks = new ArrayList<ShortSoundTrack>();  // Initially no tracks
        this.id = sqlHelper.insertShortSound( this );  // Add ShortSound to the DB
        Log.d("DB_TEST", "Inserted ShortSound: " + this.toString() );
    }

    /**
     * Fetch all available ShortSounds (used on app start)
     */
    public static List<ShortSound> getAll() {
        sqlHelper.queryAllShortSounds();
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
        this.tracks.add( track );  // Add track to list
        this.sqlHelper.insertShortSoundTrack( track, this.id );
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
        sqlHelper.updateShortSound( this );  // Update the DB
    }

    @Override
    public String toString() {
        return this.title + ":" + this.id;
    }

    public long getId() {
        return id;
    }
}


/* ADD THIS TO THE MAIN ACTIVITY FOR TESTING

Log.e("DB_TEST", "MainActivity:onCreate()");
ShortSound ss = new ShortSound();
ss.setTitle( "The Best Song Ever" );

ss.addTrack( new ShortSoundTrack( new byte[ShortSoundTrack.BUFFER_SIZE] ) );

ShortSound.getAll();
 */