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
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";
    private static final String KEY_SHORT_SOUND_ID = "short_sound_id";
    private static final String KEY_TRACK_FILENAME_ORIGINAL = "filename_original";
    private static final String KEY_TRACK_FILENAME_MODIFIED = "filename_modified";


    // Table Create Query
    private static final String SHORT_SOUND_TABLE_CREATE =
                    "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    private static final String SHORT_SOUND_TRACK_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TRACK_TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY," +
                    KEY_TITLE + " TEXT, " +
                    KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

    public ShortSoundSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    /**
     * Get a list of all ShortSounds
     */
    public void queryAllShortSounds() {
        Log.d("DB_TEST", "ShortSoundSQLHelper:queryAllShortSounds()");
        ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                String log = ".\n";
                for( int i=0; i< cursor.getColumnCount(); i++ ) {
                    map.put(cursor.getColumnName(i), cursor.getString(i));
                    log += cursor.getColumnName(i) + "[" + cursor.getString(i) + "]\n";
                }
                Log.d("DB_TEST", log);
                maplist.add(map);
            } while (cursor.moveToNext());
        }
        // TODO: build up the ShortSound objects...
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
        long new_id;
        new_id = db.insert( TABLE_NAME, null, values );
        return new_id;
    }

    /**
     * Update an existing ShortSound in the DB
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





    /**
     * Inserts a ShortSoundTrack into the short_sound_track table with the associated
     * ShortSound (so we know which ShortSound it belongs to).
     * @pre Assumes a new ShortSoundTrack which has no Effects yet.
     * @param track
     * @param id
     * @return The
     */
    public long insertShortSoundTrack(ShortSoundTrack track, long id) {
        Log.d("DB_TEST", "ShortSoundSQLHelper:insertShortSoundTrack("+id+")");
        // TODO: save the original track to disk?

        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, track.getTitle() );
        values.put( KEY_SHORT_SOUND_ID, id );
        values.put( KEY_TRACK_FILENAME_ORIGINAL, "" );
        values.put( KEY_TRACK_FILENAME_MODIFIED, "" );
        long new_id;
        new_id = db.insert( TRACK_TABLE_NAME, null, values );
        return new_id;
    }






    public String getCurrentTimestamp() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // 2015-04-20 22:27:19
        return formatter.format( now );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_TEST", "ShortSoundSQLHelper:onCreate()");
        db.execSQL(SHORT_SOUND_TABLE_CREATE);
        db.execSQL(SHORT_SOUND_TRACK_TABLE_CREATE);
        // TODO: This is where we would seed the DB with a sample ShortSound
        // Note, this is only called ONCE when the DB is first created.
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Shouldn't have to worry about this??
    }
}

