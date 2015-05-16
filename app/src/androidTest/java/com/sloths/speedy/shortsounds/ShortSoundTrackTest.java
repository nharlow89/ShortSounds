package com.sloths.speedy.shortsounds;

import android.test.AndroidTestCase;

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
}
