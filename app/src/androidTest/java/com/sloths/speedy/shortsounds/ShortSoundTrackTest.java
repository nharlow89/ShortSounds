package com.sloths.speedy.shortsounds;

import android.test.AndroidTestCase;

import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Justin Yoon on 5/15/2015.
 *
 * An AndroidTestCase that tests the ShortSoundTrack class's methods.
 */
public class ShortSoundTrackTest extends AndroidTestCase {

    /*
     * Constructor
     */

    /**
     * Tests that a new ShortSoundTrack that was constructed with the
     * two parameter constructor has non-null fields and ids that are non-negative.
     */
    public void testNewShortSoundTrackHasProperInitialFields() {
        ShortSoundTrack test = new ShortSoundTrack(new File(""), 0);

        assert(test.getFileName() != null);
        assert(test.getId() >= 0);
        assert(test.getTitle() != null);
        assert(test.getParentId() >= 0);
        assert(test.getmEqEffect() != null);
        assert(test.getmReverbEffect() != null);
    }

    /**
     * Tests that when a new ShortSoundTrack is constructed with an empty HashMap
     * that an AssertionError is thrown.
     */
    public void testEmptyMapCausesAssertionError() {
        try {
            ShortSoundTrack test = new ShortSoundTrack(new HashMap<String, String>());
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }

    /**
     * Tests that a new ShortSoundTrack that was constructed with the single parameter
     * constructor gets the correct values for its fields.
     */
    public void testNewShortSoundTrackGetsFieldsFromSQL() {
        HashMap<String, String> testValues = new HashMap<String, String>();

        testValues.put(ShortSoundSQLHelper.KEY_ID, "0");
        testValues.put(ShortSoundSQLHelper.KEY_TRACK_FILENAME_MODIFIED, "test-file-modified");
        testValues.put(ShortSoundSQLHelper.KEY_TITLE, "TestTrack");
        testValues.put(ShortSoundSQLHelper.KEY_SHORT_SOUND_ID, "0");
        testValues.put(ShortSoundSQLHelper.EQ_EFFECT_PARAMS, null);
        testValues.put(ShortSoundSQLHelper.REVERB_EFFECT_PARAMS, null);

        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getFileName().equals("test-file-modified"));
        assert(test.getId() == 0);
        assert(test.getTitle().equals("TestTrack"));
        assert(test.getParentId() == 0);
        assert(test.getmEqEffect() != null);
        assert(test.getmReverbEffect() != null);
    }

    /*
     * getTitle
     */

    /**
     * Tests that when constructed with the two parameter constructor, getTitle returns the default
     * title.
     */
    public void testDefaultTitle() {
        ShortSoundTrack test = new ShortSoundTrack(new File(""), 0);

        assert(test.getTitle().equals(ShortSoundTrack.DEFAULT_TITLE));
    }
}
