package com.sloths.speedy.shortsounds.tests;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;

import java.io.File;
import java.util.List;

/**
 * Tests the ShortSoundSQLHelper
 * Created by neilharlow on 4/27/15.
 */
public class SQLHelperTest extends AndroidTestCase {

    private ShortSoundSQLHelper db;
    private RenamingDelegatingContext context;

    /**
     * Sets up the database
     * @throws Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = new RenamingDelegatingContext(getContext(), "test-");
        db = ShortSoundSQLHelper.getTestInstance( context );
    }

    /**
     * Tears down the database
     * @throws Exception
     */
    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }


    /**
     * Test that our singleton is implemented properly.
     */
    public void testSingleton() {
        // Lets try to grab another instance and compare to the existing one
        ShortSoundSQLHelper newHelper = ShortSoundSQLHelper.getInstance();
        assertEquals( db, newHelper );
    }

    /**
     * Test that the seed works as expected.
     *
    public void testDBSeed(){
        List<ShortSound> sounds = db.queryAllShortSounds();
        assertEquals(sounds.toString(), 2, sounds.size() );
        // Check that the audio files were seeded properly
        File dir = context.getFilesDir();
        String[] filenames = dir.list();
        assertTrue("Expected seeded file [ss1-track1.mp3]", isInArray(filenames, "ss1-track1.mp3"));
        assertTrue("Expected seeded file [ss1-track1-modified.mp3]", isInArray( filenames, "ss1-track1-modified.mp3" ) );
        assertTrue("Expected seeded file [ss1-track2.mp3]", isInArray( filenames, "ss1-track2.mp3" ) );
        assertTrue("Expected seeded file [ss1-track2-modified.mp3]", isInArray( filenames, "ss1-track2-modified.mp3" ) );
        assertTrue("Expected seeded file [ss1-track3.mp3]", isInArray( filenames, "ss1-track3.mp3" ) );
        assertTrue("Expected seeded file [ss1-track3-modified.mp3]", isInArray( filenames, "ss1-track3-modified.mp3" ) );
    }

    /**
     * Simple helper for checking if a particular filename is in an array.
     */
    private boolean isInArray(String[] filenames, String s) {
        for (int i = 0; i < filenames.length; i++) {
            Log.d("DEBUG", "filename: " + filenames[i] );
            if (filenames[i].equals(s))
                return true;
        }
        return false;
    }

    /**
     * Test creating a ShortSound updates the database.
     *
    public void testCreateShortSound() {
        ShortSound ss = new ShortSound();
        List<ShortSound> sounds = db.queryAllShortSounds();
        Boolean found = false;
        // Loop through and make sure its there
        for (int i = 0; i < sounds.size(); i++) {
            ShortSound current = sounds.get(i);
            if ( current.getId() == ss.getId() ) {
                found = true;
            }
        }
        assertTrue( found );
    }/*

/*    *//**
     * Test that we can create a ShortSound and then remove it from the database.
     *//*
    public void testCreateShortSoundAndRemove() {
        assertTrue("TODO", false);
    }

    *//**
     * Test that we can create a ShortSoundTrack and fetch it from the database.
     *//*
    public void testCreateShortSoundTrack() {
        assertTrue("TODO", false);
    }*/

}
