package com.sloths.speedy.shortsounds.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sloths.speedy.shortsounds.view.ShortSoundsApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * This is our helper class for interacting with the database. Note that it is
 * a singleton. Also note, this class will seed the database on the first
 * time the application is ran (or anytime the database does not exist).
 */
public class ShortSoundSQLHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static ShortSoundSQLHelper instance = new ShortSoundSQLHelper( ShortSoundsApplication.getAppContext() );

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "ShortSounds.db";
    private static final String TABLE_NAME = "short_sounds";
    private static final String TRACK_TABLE_NAME = "short_sound_tracks";

    // Table columns
    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_UPDATED_AT = "updated_at";
    public static final String KEY_SHORT_SOUND_ID = "short_sound_id";
    public static final String NEXT_TRACK_NUM = "next_track_num";
    public static final String KEY_TRACK_FILENAME_ORIGINAL = "filename_original";
    public static final String KEY_TRACK_FILENAME_MODIFIED = "filename_modified";
    public static final String EQ_EFFECT_PARAMS = "eq_effects";
    public static final String REVERB_EFFECT_PARAMS = "reverb_effects";
    public static final String VOLUME_PARAMS = "volume";
    public static final String SOLO_PARAMS = "solo";
    public static final String TRACK_LENGTH = "track_length";


    // Table Create Query
    private static final String SHORT_SOUND_TABLE_CREATE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    NEXT_TRACK_NUM + " INTEGER," +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String SHORT_SOUND_TRACK_TABLE_CREATE =
                    "CREATE TABLE " + TRACK_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    KEY_SHORT_SOUND_ID + " INTEGER, " +
                    KEY_TRACK_FILENAME_MODIFIED + " TEXT, " +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    VOLUME_PARAMS + " REAL," +
                    SOLO_PARAMS + " TEXT," +
                    EQ_EFFECT_PARAMS + " TEXT DEFAULT 'NULL'," +
                    TRACK_LENGTH + " INTEGER, " +
                    REVERB_EFFECT_PARAMS + " TEXT DEFAULT 'NULL');";

    /**
     * Private constructo to create the database
     * @param context The app context
     */
    private ShortSoundSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    /**
     * Returns an instance of the database
     * If no database exists, creates the database.
     * @return The instance of the database
     */
    public static ShortSoundSQLHelper getInstance() {
        if ( instance == null ) {
            instance = new ShortSoundSQLHelper( ShortSoundsApplication.getAppContext() );
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Might be frowned upon, but had to make this to get around singleton for testing.
     * Basically, the singleton prevented the tests from recreating a new db each test.
     * @return an instance of a new database
     */
    public static ShortSoundSQLHelper getTestInstance( Context context ) {
        instance = new ShortSoundSQLHelper( context );
        return instance;
    }

    /**
     * Get a list of all ShortSounds. This bad boy fetches from the database
     * and instantiates all the objects we need to represent our data.
     * @return List<ShortSound>
     */
    public List<ShortSound> queryAllShortSounds() {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:queryAllShortSounds()");
        List<ShortSound> shortSounds = new ArrayList<ShortSound>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for( int i=0; i< cursor.getColumnCount(); i++ ) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }
                ShortSound ss = new ShortSound( map );
                shortSounds.add( ss );
            } while (cursor.moveToNext());
        }
        cursor.close();
        // We now have a List of ShortSound objects populated from the database.
        for (int i = 0; i < shortSounds.size(); i++) {
            ShortSound ss = shortSounds.get( i );
            List<ShortSoundTrack> tracks = getShortSoundTracks( ss.getId() );
            // We now have all the ShortSoundTracks associated with this ShortSound
            ss.setTracks( tracks );
        }
        return shortSounds;
    }

    /**
     * Get A single ShortSound by id.
     * @param id The id of the ShortSound to find
     * @return ShortSound if one exists with given id, otherwise null
     */
    public ShortSound queryShortSoundById(long id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE id = " + id, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for( int i=0; i< cursor.getColumnCount(); i++ ) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                }
                ShortSound ss = new ShortSound( map );
                // Now we need to populate the tracks
                List<ShortSoundTrack> tracks = getShortSoundTracks( ss.getId() );
                ss.setTracks( tracks );
                cursor.close();
                return ss;
            } while (cursor.moveToNext());
        }
        cursor.close();  // Should hit this statement if there was no result
        return null;
    }

    /**
     * Insert a ShortSound into the DB
     * @param ss The ShortSound to insert into the database
     * @return {long} The id of the new ShortSound
     */
    public long insertShortSound( ShortSound ss ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSound()");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, ss.getTitle() );
        values.put( NEXT_TRACK_NUM, ss.getNextTrackNumber() );
        return db.insert( TABLE_NAME, null, values );  // Returns the new entry id
    }

    /**
     * Update an existing ShortSound.
     * @param ss The ShortSound to update
     */
    public void updateShortSound( ShortSound ss ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        String strFilter = KEY_ID + "=" + ss.getId();  // Query for the specific row with SS id.
        ContentValues args = new ContentValues();
        args.put( KEY_TITLE, ss.getTitle() );
        args.put( KEY_UPDATED_AT, this.getCurrentTimestamp() );
        args.put( NEXT_TRACK_NUM, ss.getNextTrackNumber() );
        db.update( TABLE_NAME, args, strFilter, null );
        Log.d("DB_TEST", "Updated ShortSound: " + ss.toString() );
    }

    /**
     * Remove a ShortSound from the database and removes any associated ShortSoundTracks.
     * @param ss The ShortSound to remove
     */
    public boolean removeShortSound( ShortSound ss ) {
        return db.delete(TABLE_NAME, KEY_ID + "=" + ss.getId(), null) > 0;
    }

    /**
     * Get a list of ShortSoundTracks associated with a given ShortSound.
     * @param shortSoundId The ShortSound to get a list of tracks for
     * @return a list of tracks
     */
    public List<ShortSoundTrack> getShortSoundTracks( long shortSoundId ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        List<ShortSoundTrack> tracks = new ArrayList<ShortSoundTrack>();
        // Query for all tracks related to this ShortSound
        Cursor trackCursor = db.rawQuery("SELECT * FROM " + TRACK_TABLE_NAME + " WHERE " +
                KEY_SHORT_SOUND_ID + "=" + shortSoundId, null);
        if (trackCursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                for( int j=0; j < trackCursor.getColumnCount(); j++ ) {
                    map.put(trackCursor.getColumnName(j), trackCursor.getString(j));
                }
                ShortSoundTrack track = new ShortSoundTrack( map );
                tracks.add( track );
            } while (trackCursor.moveToNext());
        }
        trackCursor.close();
        return tracks;
    }

    /**
     * Inserts a ShortSoundTrack into the short_sound_track table with the associated
     * ShortSound (so we know which ShortSound it belongs to).
     * @precondition Assumes a new ShortSoundTrack which has no Effects yet.
     * @param track The track to enter
     * @param id ShortSound id that this track belongs to
     * @return The new entry id
     */
    public long insertShortSoundTrack(ShortSoundTrack track, long id) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSoundTrack[file:" + track.getFileName() + "]");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( KEY_SHORT_SOUND_ID, id );
        values.put( VOLUME_PARAMS, track.getVolume());
        values.put( SOLO_PARAMS, track.getSQLSolo());
        values.put( EQ_EFFECT_PARAMS , track.getEQEffectString());
        values.put( REVERB_EFFECT_PARAMS, track.getReverbEffectString());
        values.put( KEY_TRACK_FILENAME_MODIFIED, track.getFileName() );
        values.put( TRACK_LENGTH, track.getLengthInBytes() );
        return db.insert( TRACK_TABLE_NAME, null, values );  // Returns the new entry id
    }

    /**
     * Update an existing ShortSoundTrack.
     * @param track The track to update
     */
    public void updateShortSoundTrack( ShortSoundTrack track ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:updateShortSoundTrack[file:" + track.getFileName()+"]");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( VOLUME_PARAMS, track.getVolume());
        values.put( SOLO_PARAMS, track.getSQLSolo());
        values.put( EQ_EFFECT_PARAMS , track.getEQEffectString());
        values.put( REVERB_EFFECT_PARAMS, track.getReverbEffectString());
        values.put( KEY_TRACK_FILENAME_MODIFIED, track.getFileName() );
        values.put( TRACK_LENGTH, track.getLengthInBytes() );
        db.update( TRACK_TABLE_NAME, values, "id=" + track.getId(), null );  // Returns the new entry id
    }

    /**
     * Remove an entry for the given ShortSoundTrack in the database.
     * @param track The track to remove
     * @return Boolean, whether a row was deleted.
     */
    public boolean removeShortSoundTrack( ShortSoundTrack track ) {
        return db.delete(TRACK_TABLE_NAME, KEY_ID + "=" + track.getId(), null) > 0;
    }

    /**
     * Gets the time
     * @return A formatted string of the time
     */
    public String getCurrentTimestamp() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 2015-04-20 22:27:19
        return formatter.format( now );
    }

    /**
     * This method is called on the VERY first time our DB is created (and never again).
     * Here we setup the tables and seed the database.
     * @param db The database to create tables for
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_TEST", "ShortSoundSQLHelper:onCreate()");
        // Create the tables
        db.execSQL(SHORT_SOUND_TABLE_CREATE);
        db.execSQL(SHORT_SOUND_TRACK_TABLE_CREATE);
    }

    /**
     * Seeds a sample audio file for testing
     * @param rawId id of the seeded file
     * @param outputFileName name of file
     */
    private void seedSampleAudioFile( int rawId, String outputFileName ) {
        Context context = ShortSoundsApplication.getAppContext();
        InputStream inputStream = context.getResources().openRawResource( rawId );
        try {
            File outputFile = new File( context.getFilesDir(), outputFileName );
            OutputStream outputStream = new FileOutputStream( outputFile );
            try {
                byte[] buf = new byte[4096];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                inputStream.close();
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the database on upgrade.  This was not necessary to implement
     * @param db The database
     * @param oldVersion Old version number
     * @param newVersion New version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not necessary
    }

}

