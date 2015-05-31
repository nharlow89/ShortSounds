package com.sloths.speedy.shortsounds.test;

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


}
