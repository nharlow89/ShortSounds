package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundSQLHelper;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jbusc_000 on 5/15/2015.
 * Tests ShortSounds
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
/*    public void testConstructor() {
        ShortSound ss = new ShortSound();
        assertNotNull(ss);
        assertEquals(0, ss.getTracks().size());

        // So that the fact that ss is inserted into the database doesn't affect the other tests.
        ShortSoundSQLHelper.getInstance().removeShortSound(ss);
    }*/

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
     * Tests that constructing with a map with all fields and an extra one results in the same
     * result as the test with a map of both fields.
     */
    public void testConstructorWithMapWithExtraValues() {
        HashMap<String, String> testMap = makeTestMap();
        testMap.put("Random key", "Random value");
        ShortSound ss = new ShortSound(testMap);

        assertNotNull(ss);
        assertEquals(TEST_TITLE, ss.getTitle());
        assertEquals(0, ss.getTracks().size());
    }

    /*
     * getAll
     */

    /**
     * Tests that getAll returns an empty List when no ShortSounds have been constructed.
     */
/*    public void testGetAllWithNoShortSounds() {
        List<ShortSound> shortSounds = ShortSound.getAll();

        assertNotNull(shortSounds);
        assertEquals(0, shortSounds.size());
    }
*/
    /**
     * Tests that getAll returns a List with the proper number of ShortSounds when 1 ShortSound
     * has been constructed and that it is the same as the constructed ShortSound.
     */
/*    public void testGetAllWithOneShortSound() {
        ShortSound ss = new ShortSound();
        List<ShortSound> shortSounds = ShortSound.getAll();

        assertNotNull(shortSounds);
        assertEquals(1, shortSounds.size());

        ShortSound retreivedSS = shortSounds.get(0);
        assertEquals(ss.getTitle(), retreivedSS.getTitle());
        assertEquals(ss.getTracks().size(), retreivedSS.getTracks().size());
        assertEquals(ss.getId(), retreivedSS.getId());

        ShortSoundSQLHelper.getInstance().removeShortSound(ss);
    }
*/
    /*
     * getById
     */

    /**
     * Tests that calling getById with an ID that doesn't exist returns null.
     */
    public void testGetByIdWithNoMatchingId() {
        assertNull(ShortSound.getById(-1));
    }

    /**
     * Tests that calling getById with an ID that exists returns the correct ShortSound.
     */
/*    public void testGetByIdWithMatchingId() {
        ShortSound ss = new ShortSound(makeTestMap());
        ShortSound retreivedSS = ShortSound.getById(ss.getId());

        assertNotNull(retreivedSS);
        assertEquals(ss.getTitle(), retreivedSS.getTitle());
        assertEquals(ss.getTracks().size(), retreivedSS.getTracks().size());
        assertEquals(ss.getId(), retreivedSS.getId());
    }*/

    /*
     * getLongestTrack
     */

    /**
     * Tests that the returned track is null if there are no tracks
     */
    public void testLongestTrackWhenThereAreNoTracksIsNull() {
        ShortSound ss = new ShortSound(makeTestMap());
        assertNull(ss.getLongestTrack());
    }

    /**
     * Tests that when the ShortSound has only one track that getLongestTrack returns the one track.
     */
    public void testLongestTrackWithOneTrackReturnsTheOneTrack() {
        ShortSound ss = new ShortSound(makeTestMap());
        HashMap<String, String> shortSoundTrackTestMap = ShortSoundTrackTest.makeTestValues();
        ShortSoundTrack sst = new ShortSoundTrack(shortSoundTrackTestMap);
        ss.addTrack(sst);

        ShortSoundTrack retreivedTrack = ss.getLongestTrack();
        assertNotNull(retreivedTrack);
        assertTrue(sst == retreivedTrack);
    }

    /**
     * Tests that when the ShortSound has two tracks, getLongestTrack picks the longer of the two.
     */
    public void testLongestTrackBetweenTwoTracksIsTheCorrectOne() {
        ShortSound ss = new ShortSound(makeTestMap());
        HashMap<String, String> shortSoundTrackTestMap = ShortSoundTrackTest.makeTestValues();
        ShortSoundTrack shortTrack = new ShortSoundTrack(shortSoundTrackTestMap);
        shortSoundTrackTestMap.put(ShortSoundSQLHelper.TRACK_LENGTH, "10");
        ShortSoundTrack longTrack = new ShortSoundTrack(shortSoundTrackTestMap);
        ss.addTrack(shortTrack);
        ss.addTrack(longTrack);
        ShortSoundTrack returnedTrack = ss.getLongestTrack();

        assertNotNull(returnedTrack);
        assertTrue(longTrack == returnedTrack);
    }

    /*
     * getTracks
     */

    /**
     * Tests that getTracks returns an empty List when the ShortSound has no tracks.
     */
    public void testGetTracksWithNoTracks() {
        ShortSound ss = new ShortSound(makeTestMap());
        List<ShortSoundTrack> ssTracks = ss.getTracks();

        assertNotNull(ssTracks);
        assertEquals(0, ssTracks.size());
    }

    /**
     * Tests that getTracks returns a List with one track when the ShortSound has one track.
     * Also makes sure that the track returned in the List refers to the same track as the one
     * in the ShortSound.
     */
    public void testGetTracksWithOneTrack() {
        ShortSound ss = new ShortSound(makeTestMap());
        ShortSoundTrack sst = new ShortSoundTrack(ShortSoundTrackTest.makeTestValues());
        ss.addTrack(sst);
        List<ShortSoundTrack> ssTracks = ss.getTracks();

        assertNotNull(ssTracks);
        assertEquals(1, ssTracks.size());
        assertTrue(sst == ssTracks.get(0));
    }

    /*
     * addTrack
     */

    /**
     * Tests that adding a track to a track list with one track appends the bew track to the end.
     */
    public void testAddTrackToListWithOneTrack() {
        ShortSound ss = new ShortSound(makeTestMap());
        ShortSoundTrack firstTrack = new ShortSoundTrack(ShortSoundTrackTest.makeTestValues());
        ss.addTrack(firstTrack);
        ShortSoundTrack secondTrack = new ShortSoundTrack(ShortSoundTrackTest.makeTestValues());
        ss.addTrack(secondTrack);
        List<ShortSoundTrack> ssTracks = ss.getTracks();

        assertNotNull(ssTracks);
        assertEquals(2, ssTracks.size());
        assertTrue(firstTrack == ssTracks.get(0));
        assertTrue(secondTrack == ssTracks.get(1));
    }

    /*
     * removeTrack
     */

    /**
     * Tests that removing a ShortSoundTrack that is not in the ShortSound causes no change to the
     * ShortSound.
     */
    public void testRemoveTrackWhenTrackIsNotInShortSound() {
        HashMap<String, String> testMap = makeTestMap();
        ShortSound ss = new ShortSound(testMap);
        ShortSoundTrack shortSoundTrack = new ShortSoundTrack(new File(""), 1);
        ShortSoundTrack trackToTryToRemove = new ShortSoundTrack(new File(""), 1);
        ss.addTrack(shortSoundTrack);

        ss.removeTrack(trackToTryToRemove);
        List<ShortSoundTrack> shortSoundTracks = ss.getTracks();

        assertNotNull(shortSoundTracks);
        assertEquals(1, shortSoundTracks.size());
        assertTrue(shortSoundTrack == shortSoundTracks.get(0));
    }

    /**
     * Tests that removing a ShortSoundTrack that is in the ShortSound removes it from the
     * ShortSound.
     */
    public void testRemoveTrackWhenTrackIsInShortSoundAndThereIsOnlyOneTrack() {
        ShortSound ss = new ShortSound(makeTestMap());
        ShortSoundTrack shortSoundTrack = new ShortSoundTrack(ShortSoundTrackTest.makeTestValues());
        ss.addTrack(shortSoundTrack);

        ss.removeTrack(shortSoundTrack);
        List<ShortSoundTrack> shortSoundTracks = ss.getTracks();

        assertNotNull(shortSoundTracks);
        assertEquals(0, shortSoundTracks.size());
    }

    /**
     * Tests that removing a ShortSoundTrack that is in a ShortSound with multiple tracks
     * only removes the ShortSoundTrack passed in.
     */
    public void testRemoveTrackWhenTrackIsInShortSoundAndThereAreMultipleTracks() {
        HashMap<String, String> testMap = makeTestMap();
        ShortSound ss = new ShortSound(testMap);
        ShortSoundTrack shortSoundTrack = new ShortSoundTrack(new File(""), 1);
        ShortSoundTrack trackToTryToRemove = new ShortSoundTrack(new File(""), 1);
        ss.addTrack(shortSoundTrack);
        ss.addTrack(trackToTryToRemove);

        ss.removeTrack(trackToTryToRemove);
        List<ShortSoundTrack> shortSoundTracks = ss.getTracks();

        assertNotNull(shortSoundTracks);
        assertEquals(1, shortSoundTracks.size());
        assertTrue(shortSoundTrack == shortSoundTracks.get(0));
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
