package com.sloths.speedy.shortsounds;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.List;

/**
 * Created by neilharlow on 4/27/15.
 */
public class SQLHelperTest extends AndroidTestCase {

    private ShortSoundSQLHelper db;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        RenamingDelegatingContext context = new RenamingDelegatingContext(getContext(), "test_");
        db = new ShortSoundSQLHelper(context);
    }

    @Override
    public void tearDown() throws Exception {
        db.close();
        super.tearDown();
    }

    public void testDBSeed(){
        List<ShortSound> sounds = db.queryAllShortSounds();
        assertEquals( sounds.size(), 2 );
    }
}
