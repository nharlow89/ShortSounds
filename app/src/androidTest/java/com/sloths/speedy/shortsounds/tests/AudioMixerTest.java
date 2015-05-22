package com.sloths.speedy.shortsounds.tests;

import com.sloths.speedy.shortsounds.model.AudioMixer;
import com.sloths.speedy.shortsounds.model.ShortSound;

import junit.framework.TestCase;

/**
 * Created by jbusc_000 on 5/21/2015.
 */
public class AudioMixerTest extends TestCase {

    public void testConstructor() {
        ShortSound ss = new ShortSound();
        AudioMixer am = new AudioMixer(ss);
        assertNotNull("AudioMixer Constructor fail", am);
        assertEquals("AudioMixer constructor made wrong" +
                "ShortSound", ss, am.getShortSound());
    }
}
