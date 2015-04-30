package com.sloths.speedy.shortsounds;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
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
        repInvariant();
    }

    /**
     * Decodes a short sound retrieved form the database and converts into an actual
     * ShortSound object.
     * @param map
     * @return
     */
    public ShortSound( HashMap<String, String> map ) {
        this.id = Long.parseLong( map.get( sqlHelper.KEY_ID ) );
        this.title = map.get( sqlHelper.KEY_TITLE );
        repInvariant();
    }

    /**
     * Fetch all available ShortSounds (used on app start)
     */
    public static List<ShortSound> getAll() {
        return sqlHelper.queryAllShortSounds();
    }

    /**
     * Generate audio file (with all compiled tracks)
     */
    public void generateAudioFile() {
        // TODO
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
        long new_id = this.sqlHelper.insertShortSoundTrack( track, this.id );
        track.setId( new_id );  // Update the ShortSoundTrack with db id
    }

    /**
     * Remove a track from this ShortSound.
     */
    public void removeTrack( ShortSoundTrack track ) {
        this.tracks.remove(track);
        sqlHelper.removeShortSoundTrack( track );
        track.deleteFiles();
        repInvariant();
    }

    /**
     * Specifically set the list of tracks associated with this ShortSound.
     * Should <b>only</b> be used when populating a ShortSound from the DB.
     * @param tracks
     */
    public void setTracks( List<ShortSoundTrack> tracks ) {
        this.tracks = tracks;
        repInvariant();
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
        repInvariant();
    }

    @Override
    public String toString() {
        String s = this.title + "[" + this.id + "]{";
        String tracksString = "";
        for (int i = 0; i < this.tracks.size(); i++) {
            tracksString += this.tracks.get(i);
            if ( i != this.tracks.size() - 1 )
                tracksString += ",";
        }
        return s + tracksString + "}";
    }

    public long getId() {
        return id;
    }

    private void repInvariant() {
        assert( this.title != null && this.title instanceof String );
        assert( this.id > 0 );
        assert( this.tracks instanceof ArrayList );
    }
}


/* ADD THIS TO THE MAIN ACTIVITY FOR TESTING

Log.e("DB_TEST", "MainActivity:onCreate()");
ShortSound ss = new ShortSound();
ss.setTitle( "The Best Song Ever" );

ss.addTrack( new ShortSoundTrack( new byte[ShortSoundTrack.BUFFER_SIZE] ) );

ShortSound.getAll();
 */