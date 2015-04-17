package com.sloths.speedy.shortsounds;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


/**
 * TODO: make singleton
 */
public class ShortSoundSQLHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "ShortSounds.db";
    private static final String TABLE_NAME = "short_sounds";

    // Table columns
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Table Create Query
    private static final String DICTIONARY_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTO_INCREMENT," +
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
    public static void queryAllShortSounds() {
        // TODO
    }

    /**
     * Insert a ShortSound into the DB
     * @param ss
     */
    public void insertShortSound( ShortSound ss ) {
        ContentValues values = new ContentValues();
        values.put( KEY_TITLE, ss.getTitle() );
        long new_id;
        new_id = db.insert( TABLE_NAME, KEY_TITLE, values);
        // TODO: return the primary key
        // TODO: may not need this method?? should be able to use an Update or Insert command
    }

    public static void updateShortSound( ShortSound ss ) {
        // TODO
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DICTIONARY_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Shouldn't have to worry about this??
    }
}

