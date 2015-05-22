package com.sloths.speedy.shortsounds.tests;

import android.graphics.PointF;
import android.test.AndroidTestCase;

import com.sloths.speedy.shortsounds.model.EqEffect;
import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;
import com.sloths.speedy.shortsounds.view.MainActivity;

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
    private static final String TEST_EFFECT_PARAMETERS = "NULL";

    /*
     * Constructor
     */

    /**
     * Tests that a new ShortSoundTrack that was constructed with the
     * two parameter constructor has non-null fields and ids that are non-negative.
     */
    public void testNewShortSoundTrackHasProperInitialFields() {
        ShortSoundTrack test = new ShortSoundTrack(makeTestFile(), 0);

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
     * Tests that an AssertionError is thrown if the ID is provided but nothing else.
     */
    public void testMapWithJustTrackIdCausesAssertionError() {
        try {
            HashMap<String, String> mapWithOnlyId = new HashMap<String, String>();
            mapWithOnlyId.put(ShortSoundSQLHelper.KEY_ID, "1");
            ShortSoundTrack test = new ShortSoundTrack(mapWithOnlyId);
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }

    /**
     * Tests that an AssertionError is thrown if there are missing fields in the Map
     * but it also for some reason holds other values.
     */
    public void testMapWithMissingFieldsButExtraKeysInMapCausesAssertionError() {
        try {
            HashMap<String, String> incompleteMap = makeTestValues();
            incompleteMap.remove(ShortSoundSQLHelper.KEY_ID);
            incompleteMap.put("Random Key", "Random Value");
            ShortSoundTrack test = new ShortSoundTrack(incompleteMap);
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

    /**
     * Tests that a new ShortSoundTrack that was constructed with the single parameter
     * constructor gets properly constructed even if the HashMap passed for some reason
     * contains additional entries.
     */
    public void testNewShortSoundTrackWithMapWithMoreFieldsThanNecessary() {
        HashMap<String, String> testValues = makeTestValues();
        testValues.put("Random Key", "Random Value");

        ShortSoundTrack test = new ShortSoundTrack(testValues);

        assert(test.getFileName().equals(TEST_FILE_NAME));
        assert(test.getId() == 0);
        assert(test.getTitle().equals(TEST_TITLE));
        assert(test.getParentId() == 0);
        assert(test.getmEqEffect() != null);
        assert(test.getmReverbEffect() != null);
    }

    /*
     * addEffect TODO: Currently this method does nothing. Come back to this when it works
     */

    /*
     * getEffectVals
     */

    /**
     * Tests whether the effect values match the default values when using the two parameter
     * constructor.
     */
    public void testEQEffectValsFromNewTrackMadeWithTwoParameterConstructor() {
        ShortSoundTrack test = new ShortSoundTrack(makeTestFile(), 1);
        PointF[] effectVals = test.getEffectVals(MainActivity.EQ);

        PointF lo = effectVals[0];
        PointF hi = effectVals[1];

        assert(lo.x == EqEffect.DEFAULT_X1);
        assert(lo.y == EqEffect.DEFAULT_Y);
        assert(hi.x == EqEffect.DEFAULT_X2);
        assert(hi.y == EqEffect.DEFAULT_Y);
    }

    /*
     * getTitle
     */

    /**
     * Tests that when constructed with the two parameter constructor, getTitle returns the default
     * title.
     */
    public void testDefaultTitle() {
        ShortSoundTrack test = new ShortSoundTrack(makeTestFile(), 0);

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
        ShortSoundTrack test = new ShortSoundTrack(makeTestFile(), 0);

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
        ShortSoundTrack test = new ShortSoundTrack(makeTestFile(), parentId);

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

        testValues.put(ShortSoundSQLHelper.KEY_ID, "1");
        testValues.put(ShortSoundSQLHelper.KEY_TRACK_FILENAME_MODIFIED, TEST_FILE_NAME);
        testValues.put(ShortSoundSQLHelper.KEY_TITLE, TEST_TITLE);
        testValues.put(ShortSoundSQLHelper.KEY_SHORT_SOUND_ID, "0");
        testValues.put(ShortSoundSQLHelper.EQ_EFFECT_PARAMS, TEST_EFFECT_PARAMETERS);
        testValues.put(ShortSoundSQLHelper.REVERB_EFFECT_PARAMS, TEST_EFFECT_PARAMETERS);
        testValues.put(ShortSoundSQLHelper.VOLUME_PARAMS, "0");
        testValues.put(ShortSoundSQLHelper.SOLO_PARAMS, "false");

        return testValues;
    }

    /**
     * Returns a File to be used in tests. Obviously does not actually point to a file.
     *
     * @return A File object that exists only for testing purposes
     */
    private File makeTestFile() {
        return new File("");
    }
}
