package com.sloths.speedy.shortsounds;

import android.test.AndroidTestCase;

import com.sloths.speedy.shortsounds.model.ShortSound;
import com.sloths.speedy.shortsounds.model.ShortSoundTrack;

import org.junit.Test;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Justin Yoon on 5/15/2015.
 */
public class ShortSoundTrackTest extends AndroidTestCase {

/*
 * Constructor
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

    public void testEmptyMapCausesAssertionError() {
        try {
            ShortSoundTrack test = new ShortSoundTrack(new HashMap<String, String>());
            assert(false);
        } catch (AssertionError error) {
            assert(true);
        }
    }
}
