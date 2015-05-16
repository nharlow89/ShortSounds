package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.ShortSound;

import junit.framework.TestCase;

/**
 * Created by jbusc_000 on 5/15/2015.
 */
public class ShortSoundTest extends TestCase {

    /**
     * tests to confirm constructor makes a non-null shortsound.
     */
    public void testConstructor() {
        ShortSound ss = new ShortSound();
        assertNotNull(ss);
    }
}
