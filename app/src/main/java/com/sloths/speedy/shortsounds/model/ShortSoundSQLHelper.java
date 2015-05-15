package com.sloths.speedy.shortsounds.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sloths.speedy.shortsounds.R;
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
    public static final String KEY_TRACK_FILENAME_ORIGINAL = "filename_original";
    public static final String KEY_TRACK_FILENAME_MODIFIED = "filename_modified";


    // Table Create Query
    private static final String SHORT_SOUND_TABLE_CREATE =
                    "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String SHORT_SOUND_TRACK_TABLE_CREATE =
                    "CREATE TABLE " + TRACK_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    KEY_SHORT_SOUND_ID + " INTEGER, " +
                    KEY_TRACK_FILENAME_ORIGINAL + " TEXT, " +
                    KEY_TRACK_FILENAME_MODIFIED + " TEXT, " +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private ShortSoundSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }


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
     * @param id
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
     * @param ss
     * @return {long} The id of the new ShortSound
     */
    public long insertShortSound( ShortSound ss ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSound()");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, ss.getTitle() );
        return db.insert( TABLE_NAME, null, values );  // Returns the new entry id
    }

    /**
     * Update an existing ShortSound.
     * @param ss
     */
    public void updateShortSound( ShortSound ss ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        String strFilter = KEY_ID + "=" + ss.getId();  // Query for the specific row with SS id.
        ContentValues args = new ContentValues();
        args.put( KEY_TITLE, ss.getTitle() );
        args.put( KEY_UPDATED_AT, this.getCurrentTimestamp() );
        db.update( TABLE_NAME, args, strFilter, null );
        Log.d("DB_TEST", "Updated ShortSound: " + ss.toString() );
    }

    /**
     * Remove a ShortSound from the database and removes any associated ShortSoundTracks.
     * @param ss
     */
    public boolean removeShortSound( ShortSound ss ) {
        return db.delete(TABLE_NAME, KEY_ID + "=" + ss.getId(), null) > 0;
    }

    /**
     * Get a list of ShortSoundTracks associated with a given ShortSound.
     * @param shortSoundId
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
     * @pre Assumes a new ShortSoundTrack which has no Effects yet.
     * @param track
     * @param id ShortSound id that this track belongs to
     * @return The
     */
    public long insertShortSoundTrack(ShortSoundTrack track, long id) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSoundTrack[file:"+track.getFile()+"][originalFile:"+track.getOriginalFile()+"]");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( KEY_SHORT_SOUND_ID, id );
        values.put( KEY_TRACK_FILENAME_ORIGINAL, track.getOriginalFile() );
        values.put( KEY_TRACK_FILENAME_MODIFIED, track.getFile() );
        return db.insert( TRACK_TABLE_NAME, null, values );  // Returns the new entry id
    }

    /**
     * Update an existing ShortSoundTrack.
     * @param track
     */
    public void updateShortSoundTrack( ShortSoundTrack track ) {
        if ( !db.isOpen() ) db = getWritableDatabase();
        Log.d("DB_TEST", "ShortSoundSQLHelper:updateShortSoundTrack[file:"+track.getFile()+"][originalFile:"+track.getOriginalFile()+"]");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( KEY_TRACK_FILENAME_ORIGINAL, track.getOriginalFile() );
        values.put( KEY_TRACK_FILENAME_MODIFIED, track.getFile() );
        db.update( TRACK_TABLE_NAME, values, "id=" + track.getId(), null );  // Returns the new entry id
    }

    /**
     * Remove an entry for the given ShortSoundTrack in the database.
     * @param track
     * @return Boolean, whether a row was deleted.
     */
    public boolean removeShortSoundTrack( ShortSoundTrack track ) {
        return db.delete(TRACK_TABLE_NAME, KEY_ID + "=" + track.getId(), null) > 0;
    }

    public String getCurrentTimestamp() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 2015-04-20 22:27:19
        return formatter.format( now );
    }

    /**
     * This method is called on the VERY first time our DB is created (and never again).
     * Here we setup the tables and seed the database.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_TEST", "ShortSoundSQLHelper:onCreate()");
        // Create the tables
        db.execSQL(SHORT_SOUND_TABLE_CREATE);
        db.execSQL(SHORT_SOUND_TRACK_TABLE_CREATE);
        // Seed the DB
        String ssSeed1 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(1,\"Fury of the Storm\")";
        String trackSeed1 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(1,\"Guitar\",1,\"ss1-track1\",\"ss1-track1-modified\")";
        String trackSeed2 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(2,\"Rhythm\",1,\"ss1-track2\",\"ss1-track2-modified\")";
        String trackSeed3 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(3,\"Drums\",1,\"ss1-track3\",\"ss1-track3-modified\")";

        String ssSeed2 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(2,\"Here Comes the Sun\")";
        String trackSeed4 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(4,\"track 1\",2,\"ss2-track1\",\"ss2-track1-modified\")";
        String trackSeed5 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(5,\"track 2\",2,\"ss2-track2\",\"ss2-track2-modified\")";
        String trackSeed6 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(6,\"track 3\",2,\"ss2-track3\",\"ss2-track3-modified\")";
        String trackSeed7 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(7,\"track 4\",2,\"ss2-track4\",\"ss2-track4-modified\")";
        String trackSeed8 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(8,\"track 5\",2,\"ss2-track5\",\"ss2-track5-modified\")";

        String ssSeed3 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(3,\"I Want You (She's so Heavy)\")";
        String trackSeed9 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(9,\"track 1\",3,\"ss3-track1\",\"ss3-track1-modified\")";
        String trackSeed10 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(10,\"track 2\",3,\"ss3-track2\",\"ss3-track2-modified\")";
        String trackSeed11 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(11,\"track 3\",3,\"ss3-track3\",\"ss3-track3-modified\")";
        String trackSeed12 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(12,\"track 4\",3,\"ss3-track4\",\"ss3-track4-modified\")";
        String trackSeed13 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(13,\"track 5\",3,\"ss3-track5\",\"ss3-track5-modified\")";

        String ssSeed4 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(4,\"Abbey Road Medley\")";
        String trackSeed14 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(14,\"inst aux\",4,\"ss4-track1\",\"ss4-track1-modified\")";
        String trackSeed15 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(15,\"clean bass\",4,\"ss4-track2\",\"ss4-track2-modified\")";
        String trackSeed16 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(16,\"fuzz bass\",4,\"ss4-track3\",\"ss4-track3-modified\")";
        String trackSeed17 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(17,\"overhead drums\",4,\"ss4-track4\",\"ss4-track4-modified\")";
        String trackSeed18 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(18,\"drum tubs\",4,\"ss4-track5\",\"ss4-track5-modified\")";
        String trackSeed19 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + "," + KEY_TRACK_FILENAME_ORIGINAL + "," + KEY_TRACK_FILENAME_MODIFIED + ") VALUES(19,\"john lennon\",4,\"ss4-track6\",\"ss4-track6-modified\")";

        db.execSQL( ssSeed1 );
        db.execSQL( ssSeed2 );
        db.execSQL( ssSeed3 );
        db.execSQL( ssSeed4 );
        db.execSQL( trackSeed1 );
        db.execSQL( trackSeed2 );
        db.execSQL( trackSeed3 );
        db.execSQL( trackSeed4 );
        db.execSQL(trackSeed5);
        db.execSQL(trackSeed6);
        db.execSQL(trackSeed7);
        db.execSQL(trackSeed8);
        db.execSQL( trackSeed9);
        db.execSQL(trackSeed10);
        db.execSQL(trackSeed11);
        db.execSQL(trackSeed12);
        db.execSQL(trackSeed13);
        db.execSQL( trackSeed14);
        db.execSQL( trackSeed15);
        db.execSQL( trackSeed16);
        db.execSQL( trackSeed17);
        db.execSQL(trackSeed18);
        db.execSQL(trackSeed19);
        // Seed the internal storage with given audio files
        seedSampleAudioFile(R.raw.guitar, "ss1-track1");
        seedSampleAudioFile(R.raw.guitar, "ss1-track1-modified");
        seedSampleAudioFile(R.raw.rhythm, "ss1-track2");
        seedSampleAudioFile(R.raw.rhythm, "ss1-track2-modified");
        seedSampleAudioFile(R.raw.drums, "ss1-track3");
        seedSampleAudioFile(R.raw.drums, "ss1-track3-modified");
        seedSampleAudioFile(R.raw.sun01, "ss2-track1");
        seedSampleAudioFile(R.raw.sun01, "ss2-track1-modified");
        seedSampleAudioFile(R.raw.sun02, "ss2-track2");
        seedSampleAudioFile(R.raw.sun02, "ss2-track2-modified");
        seedSampleAudioFile(R.raw.sun03, "ss2-track3");
        seedSampleAudioFile(R.raw.sun03, "ss2-track3-modified");
        seedSampleAudioFile(R.raw.sun04, "ss2-track4");
        seedSampleAudioFile(R.raw.sun04, "ss2-track4-modified");
        seedSampleAudioFile(R.raw.sun05, "ss2-track5");
        seedSampleAudioFile(R.raw.sun05, "ss2-track5-modified");
        seedSampleAudioFile(R.raw.shessoheavy01, "ss3-track1");
        seedSampleAudioFile(R.raw.shessoheavy01, "ss3-track1-modified");
        seedSampleAudioFile(R.raw.shessoheavy02, "ss3-track2");
        seedSampleAudioFile(R.raw.shessoheavy02, "ss3-track2-modified");
        seedSampleAudioFile(R.raw.shessoheavy03, "ss3-track3");
        seedSampleAudioFile(R.raw.shessoheavy03, "ss3-track3-modified");
        seedSampleAudioFile(R.raw.shessoheavy04, "ss3-track4");
        seedSampleAudioFile(R.raw.shessoheavy04, "ss3-track4-modified");
        seedSampleAudioFile(R.raw.shessoheavy05, "ss3-track5");
        seedSampleAudioFile(R.raw.shessoheavy05, "ss3-track5-modified");
        seedSampleAudioFile(R.raw.medleyaux, "ss4-track1");
        seedSampleAudioFile(R.raw.medleyaux, "ss4-track1-modified");
        seedSampleAudioFile(R.raw.medleybassclean, "ss4-track2");
        seedSampleAudioFile(R.raw.medleybassclean, "ss4-track2-modified");
        seedSampleAudioFile(R.raw.medleybassfuzz, "ss4-track3");
        seedSampleAudioFile(R.raw.medleybassfuzz, "ss4-track3-modified");
        seedSampleAudioFile(R.raw.medleydrumsoverhead, "ss4-track4");
        seedSampleAudioFile(R.raw.medleydrumsoverhead, "ss4-track4-modified");
        seedSampleAudioFile(R.raw.medleydrumstubs, "ss5-track5");
        seedSampleAudioFile(R.raw.medleydrumstubs, "ss5-track5-modified");
        seedSampleAudioFile(R.raw.medleyjohn, "ss6-track6");
        seedSampleAudioFile(R.raw.medleyjohn, "ss6-track6-modified");

    }

    private void seedSampleAudioFile( int rawId, String outputFileName ) {
        Context context = ShortSoundsApplication.getAppContext();
        InputStream inputStream = context.getResources().openRawResource( rawId );
        try {
            File outputFile = new File( context.getFilesDir(), outputFileName );
            OutputStream outputStream = new FileOutputStream( outputFile );
            int i;
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Shouldn't have to worry about this??
    }

}

