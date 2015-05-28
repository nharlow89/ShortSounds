package com.sloths.speedy.shortsounds.model;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This class represents the model of a ShortSound. A ShortSound essentially consists
 * of a list of audio tracks (modeled by ShortSoundTrack) and a title. Keep in mind that
 * any ShortSound models created are going to be synced with the database (ie creating or
 * modifying a ShortSound will create/update an entry in the database).
 */
public class ShortSound {
    public static final String DEBUG_TAG = "SHORT_SOUNDS";
    private static final String DEFAULT_TITLE = "Untitled";
    private List<ShortSoundTrack> tracks;
    private String title;
    private long id;
    private static ShortSoundSQLHelper sqlHelper = ShortSoundSQLHelper.getInstance();

    /**
     * Create a new empty ShortSound.
     * @postcondition The newly created ShortSound will be saved in the Database
     */
    public ShortSound() {
        Log.d("DB_TEST", "ShortSound:constructor()");
        this.title = DEFAULT_TITLE;  // Default
        this.tracks = new ArrayList<ShortSoundTrack>();  // Initially no tracks
        this.id = sqlHelper.insertShortSound(this);  // Add ShortSound to the DB
        Log.d("DB_TEST", "Inserted ShortSound: " + this.toString() );
        repInvariant();
    }

    /**
     * Decodes a short sound (retrieved from the database) and converts into an actual
     * ShortSound object.
     * @param map Key-value pairs corresponding to the fields that make up a ShortSound
     * @throws AssertionError if an expected key is not found
     */
    public ShortSound( HashMap<String, String> map ) {
        if ( !map.containsKey( sqlHelper.KEY_ID ) ) throw new AssertionError("Error decoding ShortSound, missing " + sqlHelper.KEY_ID + " field.");
        if ( !map.containsKey( sqlHelper.KEY_TITLE ) ) throw new AssertionError("Error decoding ShortSound, missing " + sqlHelper.KEY_TITLE + " field.");
        this.id = Long.parseLong( map.get( sqlHelper.KEY_ID ) );
        this.title = map.get(sqlHelper.KEY_TITLE);
        this.tracks = new ArrayList<ShortSoundTrack>();
        repInvariant();
    }

    /**
     * Fetch all available ShortSounds stored in the database.
     * @return List<ShortSound>
     */
    public static List<ShortSound> getAll() {
        return sqlHelper.queryAllShortSounds();
    }

    /**
     * Fetch a single ShortSound by id
     * @return ShortSound
     */
    public static ShortSound getById( long id ) {
        return sqlHelper.queryShortSoundById(id);
    }

    /**
     * Generate audio file (with all compiled tracks)
     */
    public File generateAudioFile() throws IOException {
        AudioMixer mixer = new AudioMixer( this );
        File mixedAudioFile = mixer.generateAudioFile();
        return mixedAudioFile;
    }

    /**
     * Returns the longest ShortSoundTrack in this ShortSound, or null if ShortSound
     * contains no ShortSoundTracks
     * @return ShortSoundTrack the longest ShortSoundTrack in this ShortSound, or null
     * if ShortSound contains no ShortSoundTracks
     */
    public ShortSoundTrack getLongestTrack() {
        if (tracks.size() < 1) return null;
        ShortSoundTrack longestTrack = tracks.get(0);
        for(ShortSoundTrack sst : tracks ) {
            if(longestTrack.getLengthInBytes() < sst.getLengthInBytes()) {
                longestTrack = sst;
            }
        }
        return longestTrack;
    }

    /**
     * Get the tracks for this ShortSound.
     * @return A list of the ShortSoundTracks associated with this ShortSound.
     */
    public List<ShortSoundTrack> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    /**
     * Add a track to the ShortSound.
     * @postcondition track will be the last element in the list of tracks.
     */
    public void addTrack( ShortSoundTrack track ) {
        this.tracks.add(track);  // Add track to list
    }

    /**
     * Remove a track from this ShortSound.
     * @postcondition track will be removed from this ShortSound, the database, and from disk.
     */
    public void removeTrack( ShortSoundTrack track ) {
        this.tracks.remove(track);
        track.delete();
        repInvariant();
    }

    public String getTrackName(int track) {
        return tracks.get(track).getTitle();
    }

    /**
     *
     * @param effect
     * @param position
     * @return
     */
    public boolean isEffectOn(Effect.Type effect, int position) {
        return tracks.get(position).isEffectChecked(effect);
    }

    public int getSize() {
        return tracks.size();
    }


    /**
     * Remove a ShortSound, including all of its tracks
     * @postcondition this will be null, and this ShortSound will be removed
     * from the database, and from disk
     */
    public void removeShortSound() {
        for( ShortSoundTrack track: this.tracks ) {
            track.delete();
        }
        sqlHelper.removeShortSound(this);
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

     /* Getter for Duration
     * Gets the duration of the longest of the tracks in the shortSound
     * @return Duration of the longest track in milliseconds
     */
    public int getDuration() {
        // TODO? or remove
        return 0;
    }


    /**
     * Returns a readable string containing the title of this ShortSound
     * and the titles of all it's tracks.
     * @return String
     */
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

    /**
     *
     * @param o the object to compare
     * @return true if this ShortSound is equal to the given ShortSound
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ShortSound))
            return false;
        ShortSound ss = (ShortSound) o;
        if ((ss.tracks.size() != tracks.size()) || (id != ss.id) || (!title.equals(ss.title)))
            return false;
        for (int i = 0; i < tracks.size(); i++) {
            if (!tracks.get(i).equals(ss.tracks.get(i)))
                return false;
        }
        return true;
    }

    /**
     * @return the hashcode of this ShortSound
     */
    @Override
    public int hashCode() {
        return (int) (title.hashCode() * (id + 13) * 17);
    }

    /**
     * Get the id associated with this ShortSound.
     * @return long
     */
    public long getId() {
        return id;
    }

    /**
     * This is the representation invariant of a ShortSound model. A key part of this
     * is making sure the ShortSoundTracks associated with this ShortSound have
     * corresponding ids.
     */
    private void repInvariant() {
        if ( this.title == null || !(this.title instanceof String) ) throw new AssertionError("Invalid title");
        if ( this.id < 1 ) throw new AssertionError("Invalid id: " + this.id );
        for (int i = 0; i < this.tracks.size(); i++) {
            ShortSoundTrack track = this.tracks.get( i );
            if ( !( track instanceof  ShortSoundTrack ) ) throw new AssertionError("List of tracks contains an invalid object!");
            if ( track.getParentId() != this.id ) throw new AssertionError("ShortSoundTrack does not belong to this ShortSound!");
        }
    }


}
