package com.sloths.speedy.shortsounds.tests;

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

    private static final String TEST_TITLE = "TestTrack";
    private static final String TEST_FILE_NAME = "test-file-modified";

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
        assert(test.getTitle().equals(ShortSoundTrack.DEFAULT_TITLE));
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
        HashMap<String, String> testValues = makeTestValues();

        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getFileName().equals(TEST_FILE_NAME));
        assert(test.getId() == 0);
        assert(test.getTitle().equals(TEST_TITLE));
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

    /**
     * Tests that when constructed with the single parameter constructor, getTitle returns the
     * title stored in the database.
     */
    public void testTitleFromSQL() {
        HashMap<String, String> testValues = makeTestValues();
        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getTitle().equals(TEST_TITLE));
    }

    /*
     * toString
     */

    /**
     * Tests that when constructed with the two parameter constructor, toString returns the default
     * title.
     */
    public void testDefaultTitleToString() {
        ShortSoundTrack test = new ShortSoundTrack(new File(""), 0);

        assert(test.toString().equals(ShortSoundTrack.DEFAULT_TITLE));
    }

    /**
     * Tests that when constructed with the single parameter constructor, toString returns the
     * title stored in the database.
     */
    public void testTitleFromSQLToString() {
        HashMap<String, String> testValues = makeTestValues();
        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.toString().equals(TEST_TITLE));
    }

    /*
     * getParentId
     */

    /**
     * Tests that when the parent id is passed as a parameter in the constructor,
     * getParentId returns the same one.
     */
    public void testParentIdPassedAsParameter() {
        long parentId = 7;
        ShortSoundTrack test = new ShortSoundTrack(new File(""), parentId);

        assert(test.getParentId() == parentId);
    }

    /**
     * Tests that when the ShortSoundTrack is loaded from the database, getParentId gets the correct
     * ID.
     */
    public void testParentIdFromSQL() {
        HashMap<String, String> testValues = makeTestValues();
        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getParentId() == 0);
    }

    /*
     * getFileName
     */

    /**
     * Tests that when the ShortSoundTrack is loaded from the database, getFileName returns the
     * correct name.
     */
    public void testFileNameFromSQL() {
        HashMap<String, String> testValues = makeTestValues();
        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getFileName().equals(TEST_FILE_NAME));
    }

    /*
     * getId
     */

    /**
     * Tests that when the ShortSoundTrack is loaded from the database, getId returns the correct
     * ID.
     */
    public void testGetIdFromSQL() {
        HashMap<String, String> testValues = makeTestValues();
        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getId() == 0);
    }

    /**
     * Returns a HashMap representing the data stored in the database for a ShortSoundTrack.
     *
     * @return HashMap representing sample data stored by the database for ShortSoundTracks.
     */
    private HashMap<String, String> makeTestValues() {
        HashMap<String, String> testValues = new HashMap<String, String>();

        testValues.put(ShortSoundSQLHelper.KEY_ID, "0");
        testValues.put(ShortSoundSQLHelper.KEY_TRACK_FILENAME_MODIFIED, TEST_FILE_NAME);
        testValues.put(ShortSoundSQLHelper.KEY_TITLE, TEST_TITLE);
        testValues.put(ShortSoundSQLHelper.KEY_SHORT_SOUND_ID, "0");
        testValues.put(ShortSoundSQLHelper.EQ_EFFECT_PARAMS, null);
        testValues.put(ShortSoundSQLHelper.REVERB_EFFECT_PARAMS, null);

        return testValues;
    }
}
