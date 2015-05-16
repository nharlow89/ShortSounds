package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.AudioPlayer;
import com.sloths.speedy.shortsounds.model.ShortSound;

import junit.framework.TestCase;

/**
 * Created by jbusc_000 on 5/15/2015.
 * Tests general Functionality of the Audio Player Class
 */
public class AudioPlayerTest extends TestCase {

    public void testConstructor() {
        ShortSound s = new ShortSound();
        AudioPlayer player = new AudioPlayer(s);
        assertNotNull("Constructor Failure, created null audio player", player);
    }
}
