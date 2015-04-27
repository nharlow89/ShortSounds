package com.sloths.speedy.shortsounds;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * TODO: make singleton
 */
public class ShortSoundSQLHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ShortSounds.db";
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

    public ShortSoundSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //  context.deleteDatabase(DATABASE_NAME);  // TODO: remove, only for clearing the db
        db = getWritableDatabase();
    }

    /**
     * Get a list of all ShortSounds. This bad boy fetches from the database
     * and instantiates all the objects we need to represent our data.
     * @return List<ShortSound>
     */
    public List<ShortSound> queryAllShortSounds() {
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
        // We now have a List of ShortSound objects populated from the database.
        for (int i = 0; i < shortSounds.size(); i++) {
            ShortSound ss = shortSounds.get( i );
            List<ShortSoundTrack> tracks = new ArrayList<ShortSoundTrack>();
            // Query for all tracks related to this ShortSound
            Cursor trackCursor = db.rawQuery("SELECT * FROM " + TRACK_TABLE_NAME + " WHERE " +
                                             KEY_SHORT_SOUND_ID + "=" + ss.getId(), null);
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
            // We now have all the ShortSoundTracks associated with this ShortSound
            ss.setTracks( tracks );
        }
        return shortSounds;
    }

    /**
     * Insert a ShortSound into the DB
     * @param ss
     * @return {long} The id of the new ShortSound
     */
    public long insertShortSound( ShortSound ss ) {
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
        String strFilter = KEY_ID + "=" + ss.getId();  // Query for the specific row with SS id.
        ContentValues args = new ContentValues();
        args.put( KEY_TITLE, ss.getTitle() );
        args.put( KEY_UPDATED_AT, this.getCurrentTimestamp() );
        db.update( TABLE_NAME, args, strFilter, null );
        Log.d("DB_TEST", "Updated ShortSound: " + ss.toString() );
    }

    public void removeShortSound( ShortSound ss ) {
        // TODO:
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
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSoundTrack("+id+")");
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( KEY_SHORT_SOUND_ID, id );
        values.put( KEY_TRACK_FILENAME_ORIGINAL, "" );
        values.put( KEY_TRACK_FILENAME_MODIFIED, "" );
        return db.insert( TRACK_TABLE_NAME, null, values );  // Returns the new entry id
    }

    /**
     * Update an existing ShortSoundTrack.
     * @param track
     */
    public void updateShortSoundTrack( ShortSoundTrack track ) {
        // TODO
    }

    /**
     * Removes a ShortSoundTrack from the database.
     * @param track
     */
    public void removeShortSoundTrack( ShortSoundTrack track ) {
        // TODO
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
        String ssSeed1 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(1,\"Sample ShortSound\")";
        String trackSeed1 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + ") VALUES(1,\"Guitar\",1)";
        String trackSeed2 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + ") VALUES(2,\"Vocals\",1)";
        String trackSeed3 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + ") VALUES(3,\"Drums\",1)";
        String ssSeed2 = "INSERT INTO " + TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + ") VALUES(2,\"My First ShortSound\")";
        String trackSeed4 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + ") VALUES(4,\"Guitar\",2)";
        String trackSeed5 = "INSERT INTO " + TRACK_TABLE_NAME + "(" + KEY_ID + "," + KEY_TITLE + "," + KEY_SHORT_SOUND_ID  + ") VALUES(5,\"Vocals\",2)";
        db.execSQL( ssSeed1 );
        db.execSQL( ssSeed2 );
        db.execSQL( trackSeed1 );
        db.execSQL( trackSeed2 );
        db.execSQL( trackSeed3 );
        db.execSQL( trackSeed4 );
        db.execSQL( trackSeed5 );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Shouldn't have to worry about this??
    }
}

