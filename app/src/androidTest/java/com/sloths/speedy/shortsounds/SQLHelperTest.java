package com.sloths.speedy.shortsounds;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;

import java.util.List;

/**
 * Created by neilharlow on 4/27/15.
 */
public class SQLHelperTest extends AndroidTestCase {

    private ShortSoundSQLHelper db;
    private RenamingDelegatingContext context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = new RenamingDelegatingContext(getContext(), "test-");
        db = ShortSoundSQLHelper.getTestInstance( context );
    }

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
     */
    public void testDBSeed(){
        List<ShortSound> sounds = db.queryAllShortSounds();
        assertEquals(sounds.toString(), 2, sounds.size() );
    }

    /**
     * Test creating a ShortSound updates the database.
     */
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
    }

    /**
     * Test that we can create a ShortSound and then remove it from the database.
     */
    public void testCreateShortSoundAndRemove() {
        assertTrue("TODO", false);
    }

    /**
     * Test that we can create a ShortSoundTrack and fetch it from the database.
     */
    public void testCreateShortSoundTrack() {
        assertTrue("TODO", false);
    }

}
