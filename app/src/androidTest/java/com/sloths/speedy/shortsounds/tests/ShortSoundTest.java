package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Created by jbusc_000 on 5/15/2015.
 */
public class ShortSoundTest extends TestCase {

    private static final String TEST_ID = "1";
    private static final String TEST_TITLE = "Test";

    /*
     * Constructor
     */

    /**
     * tests to confirm constructor makes a non-null shortsound and
     * that it contains an empty list of tracks.
     */
    public void testConstructor() {
        ShortSound ss = new ShortSound();
        assertNotNull(ss);
        assertEquals(0, ss.getTracks().size());
    }

    /**
     * Tests that an empty map passed to the constructor results in an AssertionError.
     */
    public void testConstructFromEmptyMapCausesAssertionError() {
        try {
            HashMap<String, String> empty = new HashMap<String, String>();
            ShortSound ss = new ShortSound(empty);
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }

    /**
     * Tests that a map with only the ShortSound ID causes an AssertionError.
     */
    public void testConstructFromMapWithOnlyIDCausesAssertionError() {
        try {
            HashMap<String, String> idOnly = new HashMap<String, String>();
            idOnly.put(ShortSoundSQLHelper.KEY_ID, "1");
            ShortSound ss = new ShortSound(idOnly);
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }

    /**
     * Tests that a map with one field of the ShortSound class and one random entry
     * causes an AssertionError.
     */
    public void testConstructFromMapWithOneFieldAndOneRandomEntryCausesAssertionError() {
        try {
            HashMap<String, String> idAndRandomMap = new HashMap<String, String>();
            idAndRandomMap.put(ShortSoundSQLHelper.KEY_ID, "1");
            idAndRandomMap.put("Random Key", "Random Value");
            ShortSound ss = new ShortSound(idAndRandomMap);
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }

    /**
     * Tests that constructing with a map with all fields results in a non-null ShortSound with the
     * given title and an empty tracklist.
     */
    public void testConstructorWithMap() {
        HashMap<String, String> testMap = makeTestMap();
        ShortSound ss = new ShortSound(testMap);

        assertNotNull(ss);
        assertEquals(TEST_TITLE, ss.getTitle());
        assertEquals(0, ss.getTracks().size());
    }

    /**
     * Produce a default HashMap of values for testing purposes.
     *
     * @return A HashMap to be used in constructing ShortSounds for testing.
     */
    private HashMap<String, String> makeTestMap() {
        HashMap<String, String> testMap = new HashMap<String, String>();
        testMap.put(ShortSoundSQLHelper.KEY_ID, TEST_ID);
        testMap.put(ShortSoundSQLHelper.KEY_TITLE, TEST_TITLE);

        return testMap;
    }
}
